package jens.chat.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jens.chat.vo.ChatGptChoices;
import jens.chat.vo.ChatGptRequestParams;
import jens.chat.vo.ChatGptResponseParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.AbstractCharResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Component
@Slf4j
public class ChatGptModel {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String url;

    private static final Charset charset = StandardCharsets.UTF_8;

    @Resource(name = "httpAsyncClient")
    private CloseableHttpAsyncClient httpAsyncClient;

    public String getAnswer(Consumer<String> resConsumer, ChatGptRequestParams chatGptRequestParams, String question) {
//        System.out.println("进入getAnswer方法");
        //启动异步请求
        httpAsyncClient.start();
        //创建post请求
        AsyncRequestBuilder asyncRequest = AsyncRequestBuilder.post(url);
        //设置请求参数
        chatGptRequestParams.addMessage("user", question);
        String requestJson = null;
        try {
            requestJson = new ObjectMapper().writeValueAsString(chatGptRequestParams);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        asyncRequest.setHeader("Authorization", "Bearer " + apiKey);
        asyncRequest.setHeader("Content-Type", "application/json");
        asyncRequest.setEntity(requestJson, ContentType.create("text/json", charset));

        //创建一个闭锁
        CountDownLatch countDownLatch = new CountDownLatch(1);

        //记录返回的结果
        StringBuilder sb = new StringBuilder();
        //消费者
        AbstractCharResponseConsumer<HttpResponse> abstractCharResponseConsumer = new AbstractCharResponseConsumer<HttpResponse>() {

            HttpResponse response;

            //释放资源
            @Override
            public void releaseResources() {

            }


            @Override
            protected int capacityIncrement() {
                return Integer.MAX_VALUE;
            }


            @Override
            protected void data(CharBuffer charBuffer, boolean b) throws IOException {
                //将数据写入到StringBuilder中
                String bufferString = charBuffer.toString();

//                log.debug("bufferString: {}", bufferString);

                for (String s : bufferString.split("data:")) {
                    if(s.contains("data:")){
                        s = s.substring(5);
                    }
                    log.debug("{}",s);
                    s = toJsonString(s);
                    if(s.length() > 8 && !s.contains("[DONE]") && !s.contains("stop")){
                        log.debug("{}",s);
                        ChatGptResponseParams chatResponse = null;
                        try {
                            chatResponse = new ObjectMapper().readValue(s, ChatGptResponseParams.class);
                            for (ChatGptChoices choice : chatResponse.getChoices()) {
                                String content = choice.getDelta().getContent();
                                if(content!=null && !"".equals(content)){
                                    sb.append(content);
                                    resConsumer.accept(content);
                                }
                            }
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }

            //接收到响应时执行
            @Override
            protected void start(HttpResponse httpResponse, ContentType contentType) throws HttpException, IOException {
                setCharset(charset);
                this.response= httpResponse;
            }

            @Override
            protected HttpResponse buildResult() throws IOException {
                return response;
            }
        };

        //发送异步请求
        httpAsyncClient.execute(asyncRequest.build(), abstractCharResponseConsumer, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse)  {
                countDownLatch.countDown();
                log.info("请求完成 {}",sb.toString());
                chatGptRequestParams.addMessage("assistant", sb.toString());
            }

            @Override
            public void failed(Exception e) {
                countDownLatch.countDown();
                log.error("请求失败: {}", e.getMessage());
            }

            @Override
            public void cancelled() {
                countDownLatch.countDown();
                log.error("请求取消");
            }
        });
        try {
            //等待异步请求完成
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }

    // 处理json字符串中value多余的双引号， 将多余的双引号替换为中文双引号
    private static String toJsonString(String s) {
        char[] tempArr = s.toCharArray();
        int tempLength = tempArr.length;
        for (int i = 0; i < tempLength; i++) {
            if (tempArr[i] == ':' && tempArr[i + 1] == '"') {
                for (int j = i + 2; j < tempLength; j++) {
                    if (tempArr[j] == '"') {
                        if (tempArr[j + 1] != ',' && tempArr[j + 1] != '}') {
                            tempArr[j] = '\"'; // 将value中的 双引号替换为中文双引号
                        }else if (tempArr[j + 1] == ',' || tempArr[j + 1] == '}') {
                            break;
                        }
                    }
                }
            }
        }
        return new String(tempArr);
    }
}

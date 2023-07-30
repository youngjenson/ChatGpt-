package jens.chat.ws;

import jens.chat.model.ChatGptModel;
import jens.chat.vo.ChatGptRequestParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@ServerEndpoint("/chatgpt/{username}")
@Slf4j
public class ChatGptServer {
    //记录当前在线连接数
    private static int onlineCount = 0;

    private static ArrayList<Session> sessions = new ArrayList<>();

    //记录当前会话
    private static ConcurrentHashMap<String, ArrayList<Session>> webSocketMap = new ConcurrentHashMap<>();

    //当前会话
    private Session session;

    private String username;

    private ChatGptRequestParams chatGptRequestParams = new ChatGptRequestParams();

    private static ChatGptModel chatGptModel;

    @Resource
    public void setChatGptModel(ChatGptModel chatGptModel) {
        ChatGptServer.chatGptModel = chatGptModel;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.session = session;
        this.username = username;

        ArrayList<Session> sessions = webSocketMap.get(username);
        if(sessions == null){
            sessions = new ArrayList<>();
            sessions.add(session);
            onlineCount++;
            log.info("有新连接加入！当前在线人数为" + onlineCount);
            webSocketMap.put(username, sessions);
        }else{
            sessions.add(session);
            webSocketMap.put(username, sessions);
            onlineCount++;
            log.info("有新连接加入！当前在线人数为" + onlineCount);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message == null || message.equals("")) {
            try {
                session.getBasicRemote().sendText("请输入内容,不要发送空消息");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        log.info("来自客户端的消息:" + message);
        long startTime = System.currentTimeMillis();
        //将结果返回给客户端
        Consumer<String> consumer = res -> {
            try {
                session.getBasicRemote().sendText(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        String answer = chatGptModel.getAnswer(consumer, chatGptRequestParams, message);
        long endTime = System.currentTimeMillis();
        log.info("answer: {} ,time: {}", answer, endTime - startTime);

    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }


    @OnClose
    public void onClose() {
        webSocketMap.remove(username);
        onlineCount--;
        log.info("有一连接关闭！当前在线人数为" + onlineCount);
    }
}

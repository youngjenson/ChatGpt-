package jens.chat.ws;

import jens.chat.model.ChatGptModel;
import jens.chat.vo.ChatGptRequestParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@Slf4j
public class WebSocketHandlerImpl extends TextWebSocketHandler {
    //记录当前在线连接数
    private static int onlineCount = 0;

    private static ArrayList<Session> sessions = new ArrayList<>();

    //记录当前会话
    private static ConcurrentHashMap<String, ArrayList<WebSocketSession>> webSocketMap = new ConcurrentHashMap<>();

    //当前会话
    private Session session;

    private String username;

    private ChatGptRequestParams chatGptRequestParams = new ChatGptRequestParams();

    private static ChatGptModel chatGptModel;

    @Resource
    public void setChatGptModel(ChatGptModel chatGptModel) {
        WebSocketHandlerImpl.chatGptModel = chatGptModel;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.username = (String) session.getAttributes().get("username");
        ArrayList<WebSocketSession> socketSessions = webSocketMap.get(username);
        if(socketSessions!= null) {
            socketSessions.add(session);
            webSocketMap.put(username, socketSessions);
            onlineCount++;
            log.debug("用户{}加入，当前在线人数为{}", username, webSocketMap.size());
        }else {
            socketSessions = new ArrayList<>();
            socketSessions.add(session);
            webSocketMap.put(username, socketSessions);
            onlineCount++;
            log.debug("用户{}加入，当前在线人数为{}", username, webSocketMap.size());
        }

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.isEmpty()) {
            try {
                session.sendMessage(new TextMessage("消息不能为空"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        log.info("来自客户端的消息:" + payload);
        long startTime = System.currentTimeMillis();
        //将结果返回给客户端
        Consumer<String> consumer = res -> {
            try {
                session.sendMessage(new TextMessage(res));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        String answer = chatGptModel.getAnswer(consumer, chatGptRequestParams, payload);
        long endTime = System.currentTimeMillis();
        log.info("answer: {} ,time: {}", answer, endTime - startTime);


    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        webSocketMap.get(username).remove(session);
        onlineCount--;
        log.info("有一连接关闭！当前在线人数为" + onlineCount);
    }
}

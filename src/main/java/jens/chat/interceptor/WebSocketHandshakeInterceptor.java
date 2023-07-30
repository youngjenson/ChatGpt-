package jens.chat.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jens.chat.domain.User;
import jens.chat.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    UserMapper userMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
        log.debug("token: {}", token);
        if(token==null||token.isEmpty()){
            log.error("token is empty");
            return false;
        }

        boolean validToken = isValidToken(token);
        if(validToken){
            String username = getUsernameFromToken(token);
            attributes.put("username", username);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    /**
     * 验证token
     * @param token
     * @return
     */
    private boolean isValidToken(String token) {
        String userId = null;
        try{
            userId = JWT.decode(token).getAudience().get(0);
            User user = userMapper.selectById(userId);
            if(user==null){
                log.error("user not exist");
                return false;
            }
            //用户密码加签验证
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
            try {
                jwtVerifier.verify(token);
            }catch (JWTVerificationException jwtVerificationException){
                log.error("token verify error", jwtVerificationException);
                return false;
            }
        }catch (Exception e){
            log.error("token error", e);
            return false;
        }
        return true;
    }

    private String getUsernameFromToken(String token) {
        // 在这里实现从 Token 中提取用户名的逻辑
        // 根据 Token 结构解析出用户名等信息
        String userid = JWT.decode(token).getAudience().get(0);
        User user = userMapper.selectById(userid);
        return user.getUsername();
    }
}

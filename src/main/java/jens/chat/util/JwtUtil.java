package jens.chat.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jens.chat.domain.User;
import jens.chat.dto.UserDto;
import jens.chat.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Resource
    private UserMapper userMapper;

    private static UserMapper staticUserMapper;

    @PostConstruct
    public void init(){
        staticUserMapper = userMapper;
    }

    private static final long EXPIRATION = 3600L;

    /**
     * 生成token
     * @param userDto
     * @return
     */
    public static String generateToken(UserDto userDto) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
        String token = JWT.create()
                .withAudience(String.valueOf(userDto.getId()))
                .withExpiresAt(expireDate)
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC256(userDto.getPassword()));
        return token;
    }

    public static UserDto getUser(String token){
        try{
            String aud = JWT.decode(token).getAudience().get(0);
            Integer userId = Integer.valueOf(aud);
            User user = staticUserMapper.selectById(userId);
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            return userDto;
        }catch (Exception e){
            log.error("getUser error", e);
            return null;
        }
    }
}

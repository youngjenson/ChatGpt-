package jens.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jens.chat.domain.User;
import jens.chat.dto.UserDto;
import jens.chat.mapper.UserMapper;
import jens.chat.service.UserService;
import jens.chat.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 23144
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-07-27 19:04:12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired
    private UserMapper userMapper;


    @Override
    public UserDto selectByUsername(UserDto userDto) {
        // TODO Auto-generated method stub
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getUsername, userDto.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        if(user!=null && user.getPassword().equals(userDto.getPassword())){
            BeanUtils.copyProperties(user, userDto);
            String token = JwtUtil.generateToken(userDto);
            userDto.setPassword("");
            userDto.setToken(token);
            return userDto;
        }
        return null;
    }
}

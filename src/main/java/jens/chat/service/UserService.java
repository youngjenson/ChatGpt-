package jens.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jens.chat.domain.User;
import jens.chat.dto.UserDto;

/**
* @author 23144
* @description 针对表【user】的数据库操作Service
* @createDate 2023-07-27 19:04:12
*/
public interface UserService extends IService<User> {

    UserDto selectByUsername(UserDto userDto);
}

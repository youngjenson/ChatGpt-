package jens.chat.controller;

import jens.chat.dto.UserDto;
import jens.chat.model.RestResponse;
import jens.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public RestResponse login(@RequestBody @Validated UserDto userDto) {

        System.out.println(userDto.toString());

        UserDto dbUser = userService.selectByUsername(userDto);
        if (dbUser != null) {
            return RestResponse.success(dbUser);
        }
        return RestResponse.validfail("用户名或密码错误");
    }
}

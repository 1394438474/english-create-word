package com.smartvocab.module.user.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.user.entity.User;
import com.smartvocab.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public R<User> me() {
        return R.ok(userService.getById(SecurityContext.getUserId()));
    }

    @PutMapping("/me")
    public R<User> update(@RequestBody Map<String, String> body) {
        return R.ok(userService.updateProfile(SecurityContext.getUserId(),
                body.get("nickname"), body.get("bio"), body.get("avatar")));
    }

    @PostMapping("/password")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        userService.changePassword(SecurityContext.getUserId(), body.get("oldPwd"), body.get("newPwd"));
        return R.ok();
    }
}

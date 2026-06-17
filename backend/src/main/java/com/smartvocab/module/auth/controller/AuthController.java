package com.smartvocab.module.auth.controller;

import com.smartvocab.common.BizException;
import com.smartvocab.common.R;
import com.smartvocab.module.auth.dto.LoginDTO;
import com.smartvocab.module.auth.dto.RegisterDTO;
import com.smartvocab.module.user.entity.User;
import com.smartvocab.module.user.service.UserService;
import com.smartvocab.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public R<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        User u = userService.register(dto.getEmail(), dto.getPassword(), dto.getNickname());
        String token = jwtUtil.issue(u.getId(), u.getEmail());
        return R.ok(Map.of("token", token, "user", u));
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        User u = userService.getByEmail(dto.getEmail());
        if (u == null) throw new BizException("邮箱或密码错误");
        if (!passwordEncoder.matches(dto.getPassword(), u.getPassword())) {
            throw new BizException("邮箱或密码错误");
        }
        String token = jwtUtil.issue(u.getId(), u.getEmail());
        return R.ok(Map.of("token", token, "user", u));
    }
}

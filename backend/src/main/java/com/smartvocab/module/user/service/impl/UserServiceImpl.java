package com.smartvocab.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartvocab.common.BizException;
import com.smartvocab.module.user.entity.User;
import com.smartvocab.module.user.mapper.UserMapper;
import com.smartvocab.module.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder encoder;

    public UserServiceImpl(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public User getById(Long id) {
        User u = getOne(new LambdaQueryWrapper<User>().eq(User::getId, id));
        if (u == null) throw new BizException("用户不存在");
        return u;
    }

    @Override
    public User getByEmail(String email) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    @Override
    public User register(String email, String password, String nickname) {
        if (getByEmail(email) != null) throw new BizException("邮箱已被注册");
        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setNickname(nickname == null ? email.split("@")[0] : nickname);
        u.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + email);
        u.setStreakDays(0);
        u.setTotalDays(0);
        u.setStatus(1);
        save(u);
        return u;
    }

    @Override
    public User updateProfile(Long id, String nickname, String bio, String avatar) {
        User u = getById(id);
        if (nickname != null) u.setNickname(nickname);
        if (bio != null) u.setBio(bio);
        if (avatar != null) u.setAvatar(avatar);
        updateById(u);
        return u;
    }

    @Override
    public void changePassword(Long id, String oldPwd, String newPwd) {
        User u = getById(id);
        if (!encoder.matches(oldPwd, u.getPassword())) throw new BizException("原密码错误");
        u.setPassword(encoder.encode(newPwd));
        updateById(u);
    }

    @Override
    public boolean updateById(User u) {
        return super.updateById(u);
    }
}

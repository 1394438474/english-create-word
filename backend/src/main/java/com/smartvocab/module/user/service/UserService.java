package com.smartvocab.module.user.service;

import com.smartvocab.module.user.entity.User;

public interface UserService {
    User getById(Long id);
    User getByEmail(String email);
    User register(String email, String password, String nickname);
    User updateProfile(Long id, String nickname, String bio, String avatar);
    void changePassword(Long id, String oldPwd, String newPwd);
    boolean updateById(User u);
}

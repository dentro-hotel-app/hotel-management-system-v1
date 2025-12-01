package com.dentro.dentrohills.service;

import com.dentro.dentrohills.model.User;

import java.util.List;

public interface IUserService {
    User registerUser(User user);
    List<User> getUsers();
    void deleteUser(String email);
    User getUser(String email);

    User registerAdmin(User user);
}

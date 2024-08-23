package com.finpro.roomio_backend.users.service;

import com.finpro.roomio_backend.users.entity.Users;

import java.util.Optional;

public interface UserService {
    Optional<Users> getUserByEmail(String email);
}

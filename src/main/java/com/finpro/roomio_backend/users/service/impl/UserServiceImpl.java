package com.finpro.roomio_backend.users.service;

import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository userRepository;

    public UserServiceImpl(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

package com.finpro.roomio_backend.users.controller;

import com.finpro.roomio_backend.responses.Response;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/check-email")
    public ResponseEntity<Response<Object>> checkEmail(@RequestParam String email) {
        Optional<Users> userOptional = userService.getUserByEmail(email);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            Map<String, Object> data = new HashMap<>();
            data.put("exists", true);
            data.put("method", user.getMethod());
            return Response.successfulResponse("Email found", data);
        } else {
            return Response.failedResponse("Email does not exist");
        }
    }
}
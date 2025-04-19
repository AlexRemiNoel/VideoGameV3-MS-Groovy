package com.example.apigatewayservice.presentationlayer;

import com.example.apigatewayservice.businesslogiclayer.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/user") // Added /gateway prefix
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<UserResponseModel>> getUsers() {
        log.info("Gateway: Received GET request for all users");
        List<UserResponseModel> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping(value = "{uuid}", produces = "application/json")
    public ResponseEntity<UserResponseModel> getUserById(@PathVariable String uuid) {
        log.info("Gateway: Received GET request for user UUID: {}", uuid);
        UserResponseModel user = userService.getUserById(uuid);
        return ResponseEntity.ok(user);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseModel> addUser(@RequestBody UserRequestModel userRequestModel) {
        log.info("Gateway: Received POST request to add user");
        UserResponseModel addedUser = userService.addUser(userRequestModel);
        return new ResponseEntity<>(addedUser, HttpStatus.CREATED);
    }

    @PutMapping(value = "{uuid}", consumes = "application/json")
    public ResponseEntity<Void> updateUser(@RequestBody UserRequestModel userRequestModel, @PathVariable String uuid) {
        log.info("Gateway: Received PUT request to update user UUID: {}", uuid);
        userService.updateUser(userRequestModel, uuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable String uuid) {
        log.info("Gateway: Received DELETE request for user UUID: {}", uuid);
        userService.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }

    // Note: Path variable names MUST match exactly in @PathVariable("...")
    @PutMapping(value = "uuid/{user_id}/balance/{balance}", produces = "application/json")
    public ResponseEntity<UserResponseModel> updateUserBalance(@PathVariable("user_id") String userId, @PathVariable("balance") double balance) {
        log.info("Gateway: Received PUT request to update balance for user ID: {} to {}", userId, balance);
        UserResponseModel updatedUser = userService.updateUserBalance(userId, balance);
        return ResponseEntity.ok(updatedUser);
    }
}
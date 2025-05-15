package com.champsoft.profiledashboardmanagement.Presentation;


import com.champsoft.profiledashboardmanagement.BusinessLogic.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<UserResponseModel>> getUsers() {
        List<UserResponseModel> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("{uuid}")
    public ResponseEntity<UserResponseModel> getUserById(@PathVariable String uuid) {
        UserResponseModel user = userService.getUserById(uuid);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    public ResponseEntity<UserResponseModel> addUser(@RequestBody UserRequestModel userRequestModel) {
        UserResponseModel addedUser = userService.addUser(userRequestModel);
        return new ResponseEntity<>(addedUser, HttpStatus.CREATED);
    }

    @PutMapping("{uuid}")
    public ResponseEntity<UserResponseModel> updateUser(@RequestBody UserRequestModel userRequestModel, @PathVariable String uuid) {
        UserResponseModel updatedUser = userService.updateUser(userRequestModel, uuid);
        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable String uuid) {
        userService.deleteUser(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("uuid/{user_id}/balance/{balance}")
    public ResponseEntity<UserResponseModel> updateUserBalance(@PathVariable String user_id, @PathVariable double balance) {
        userService.updateUserBalance(user_id, balance);
        return new ResponseEntity<>(userService.updateUserBalance(user_id, balance),  HttpStatus.OK);
    }
}
package com.champsoft.usermanagement.BusinessLogic;


import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserRepository;
import com.champsoft.usermanagement.DataMapper.UserRequestMapper;
import com.champsoft.usermanagement.DataMapper.UserResponseMapper;
import com.champsoft.usermanagement.Presentation.UserRequestModel;
import com.champsoft.usermanagement.Presentation.UserResponseModel;
import com.champsoft.usermanagement.utils.exceptions.InvalidUserInputException;
import com.champsoft.usermanagement.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final UserRequestMapper userRequestMapper;

    public UserService(UserRepository userRepository, UserResponseMapper userResponseMapper, UserRequestMapper userRequestMapper) {
        this.userRepository = userRepository;
        this.userResponseMapper = userResponseMapper;
        this.userRequestMapper = userRequestMapper;
    }

    public List<UserResponseModel> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userResponseMapper.userToUserResponseModel(users);
    }

    public UserResponseModel getUserById(String uuid) {
        User user = findUserByUuidOrThrow(uuid);
        return userResponseMapper.userToUserResponseModel(user);
    }

    public UserResponseModel addUser(UserRequestModel userRequestModel) {
        User user = userRequestMapper.userRequestModelToUser(userRequestModel);
        if (user.getBalance() < 0) {
            throw new InvalidUserInputException("Invalid negative balance: " + user.getBalance());
        }
        userRepository.save(user);
        return userResponseMapper.userToUserResponseModel(user);
    }

    public UserResponseModel updateUser(UserRequestModel userRequestModel, String uuid) {
        User user = findUserByUuidOrThrow(uuid);

        user.setUsername(userRequestModel.getUsername());
        user.setEmail(userRequestModel.getEmail());
        user.setPassword(userRequestModel.getPassword());
        userRepository.save(user);
        return userResponseMapper.userToUserResponseModel(user);
    }

    public void deleteUser(String uuid) {
        User user = findUserByUuidOrThrow(uuid);
        userRepository.delete(user);
    }

    public UserResponseModel updateUserBalance(String uuid, double newBalance) {
        User user = findUserByUuidOrThrow(uuid);


        if (newBalance < 0) {
            throw new InvalidUserInputException("Invalid negative balance: " + newBalance);
        }

        user.setBalance(newBalance); // Assuming User entity has setBalance method
        User updatedUser = userRepository.save(user);
        return userResponseMapper.userToUserResponseModel(updatedUser);
    }

    private User findUserByUuidOrThrow(String uuid) {
        User user = userRepository.findUserByUserId_uuid(uuid);
        if (user == null) {
            throw new NotFoundException("Unknown userId: " + uuid);
        }
        return user;
    }
}


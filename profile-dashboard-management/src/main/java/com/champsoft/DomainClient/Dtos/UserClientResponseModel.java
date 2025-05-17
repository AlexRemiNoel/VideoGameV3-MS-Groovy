package com.champsoft.DomainClient.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClientResponseModel {
    private String userId;
    private String username;
    private String email;
    private double balance;
    private List<String> games; // Crucial: List of game IDs
}
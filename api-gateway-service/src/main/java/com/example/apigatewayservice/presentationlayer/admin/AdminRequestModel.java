package com.example.apigatewayservice.presentationlayer.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequestModel {
    private String username;
    private String password;
}

package com.example.apigatewayservice.presentationlayer.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AdminRequestModel {
    private String username;
    private String password;
}

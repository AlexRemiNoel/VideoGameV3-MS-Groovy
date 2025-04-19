package com.example.apigatewayservice.businesslogiclayer;


import com.example.apigatewayservice.presentationlayer.AdminRequestModel;
import com.example.apigatewayservice.presentationlayer.AdminResponseModel;

import java.util.List;

public interface AdminService {
    List<AdminResponseModel> getAllAdmins();
    AdminResponseModel getAdminById(String uuid);
    AdminResponseModel addAdmin(AdminRequestModel adminRequestModel);
    void updateAdmin(AdminRequestModel adminRequestModel, String uuid); // Changed to void due to RestTemplate.put limitation
    void deleteAdmin(String uuid);
}
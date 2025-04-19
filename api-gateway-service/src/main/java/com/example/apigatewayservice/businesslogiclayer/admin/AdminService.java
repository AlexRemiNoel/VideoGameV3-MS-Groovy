package com.example.apigatewayservice.businesslogiclayer.admin;


import com.example.apigatewayservice.presentationlayer.admin.AdminRequestModel;
import com.example.apigatewayservice.presentationlayer.admin.AdminResponseModel;

import java.util.List;

public interface AdminService {
    List<AdminResponseModel> getAllAdmins();
    AdminResponseModel getAdminById(String uuid);
    AdminResponseModel addAdmin(AdminRequestModel adminRequestModel);
    void updateAdmin(AdminRequestModel adminRequestModel, String uuid);
    void deleteAdmin(String uuid);
}
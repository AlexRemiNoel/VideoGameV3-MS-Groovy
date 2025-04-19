package com.example.apigatewayservice.businesslogiclayer;
import com.example.apigatewayservice.DomainClientLayer.AdminServiceClient;
import com.example.apigatewayservice.presentationlayer.AdminRequestModel;
import com.example.apigatewayservice.presentationlayer.AdminResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // Creates constructor for final fields
public class AdminServiceImpl implements AdminService {

    private final AdminServiceClient adminServiceClient; // Inject the client

    @Override
    public List<AdminResponseModel> getAllAdmins() {
        return adminServiceClient.getAllAdmins();
    }

    @Override
    public AdminResponseModel getAdminById(String uuid) {
        return adminServiceClient.getAdminById(uuid);
    }

    @Override
    public AdminResponseModel addAdmin(AdminRequestModel adminRequestModel) {
        return adminServiceClient.addAdmin(adminRequestModel);
    }

    @Override
    public void updateAdmin(AdminRequestModel adminRequestModel, String uuid) {
        adminServiceClient.updateAdmin(adminRequestModel, uuid);
    }

    @Override
    public void deleteAdmin(String uuid) {
        adminServiceClient.deleteAdmin(uuid);
    }
}
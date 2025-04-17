package com.champsoft.usermanagement.BusinessLogic;

import com.champsoft.usermanagement.DataAccess.Admin;
import com.champsoft.usermanagement.DataAccess.AdminRepository;
import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataMapper.AdminRequestMapper;
import com.champsoft.usermanagement.DataMapper.AdminResponseMapper;
import com.champsoft.usermanagement.Presentation.AdminRequestModel;
import com.champsoft.usermanagement.Presentation.AdminResponseModel;
import com.champsoft.usermanagement.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final AdminResponseMapper adminResponseMapper;
    private final AdminRequestMapper adminRequestMapper;

    public AdminService(AdminRepository adminRepository, AdminResponseMapper adminResponseMapper, AdminRequestMapper adminRequestMapper) {
        this.adminRepository = adminRepository;
        this.adminResponseMapper = adminResponseMapper;
        this.adminRequestMapper = adminRequestMapper;
    }

    public List<AdminResponseModel> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        return adminResponseMapper.adminToAdminResponseModel(admins);
    }

    public AdminResponseModel getAdminById(String uuid) {
        Admin admin = adminRepository.findAdminByAdminId_uuid(uuid);
        return adminResponseMapper.adminToAdminResponseModel(admin);
    }

    public AdminResponseModel addAdmin(AdminRequestModel adminRequestModel) {
        Admin admin = adminRequestMapper.adminRequestModelToAdmin(adminRequestModel);
        adminRepository.save(admin);
        return adminResponseMapper.adminToAdminResponseModel(admin);
    }

    public AdminResponseModel updateAdmin(AdminRequestModel adminRequestModel, String uuid) {
        Admin admin = findAdminByUuidOrThrow(uuid);

        admin.setUsername(adminRequestModel.getUsername());
        admin.setPassword(adminRequestModel.getPassword());
        adminRepository.save(admin);
        return adminResponseMapper.adminToAdminResponseModel(admin);
    }

    public void deleteAdmin(String uuid) {
        Admin admin = findAdminByUuidOrThrow(uuid);

        adminRepository.delete(admin);
    }

    private Admin findAdminByUuidOrThrow(String uuid) {
        Admin admin = adminRepository.findAdminByAdminId_uuid(uuid);
        if (admin == null) {
            throw new NotFoundException("Unknown adminId: " + uuid);
        }
        return admin;
    }
}

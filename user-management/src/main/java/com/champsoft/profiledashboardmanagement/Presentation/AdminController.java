package com.champsoft.profiledashboardmanagement.Presentation;


import com.champsoft.profiledashboardmanagement.BusinessLogic.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("api/v1/admin")
public class AdminController {

    private AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping()
    public ResponseEntity<List<AdminResponseModel>> getAdmins() {
        List<AdminResponseModel> admins = adminService.getAllAdmins();
        return new ResponseEntity<>(admins, HttpStatus.OK);
    }

    @GetMapping("{uuid}")
    public ResponseEntity<AdminResponseModel> getAdminById(@PathVariable String uuid) {
        AdminResponseModel admin = adminService.getAdminById(uuid);
        if (admin != null) {
            return new ResponseEntity<>(admin, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    public ResponseEntity<AdminResponseModel> addAdmin(@RequestBody AdminRequestModel adminRequestModel) {
        AdminResponseModel addedAdmin = adminService.addAdmin(adminRequestModel);
        return new ResponseEntity<>(addedAdmin, HttpStatus.CREATED);
    }

    @PutMapping("{uuid}")
    public ResponseEntity<AdminResponseModel> updateAdmin(@RequestBody AdminRequestModel adminRequestModel, @PathVariable String uuid) {
        AdminResponseModel updatedAdmin = adminService.updateAdmin(adminRequestModel, uuid);
        if (updatedAdmin != null) {
            return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String uuid) {
        adminService.deleteAdmin(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
}

package com.example.apigatewayservice.presentationlayer;

import com.example.apigatewayservice.businesslogiclayer.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService; // Inject the gateway service

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<AdminResponseModel>> getAdmins() {
        log.info("Gateway: Received GET request for all admins");
        List<AdminResponseModel> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins); // Status OK
    }

    @GetMapping(value = "{uuid}", produces = "application/json")
    public ResponseEntity<AdminResponseModel> getAdminById(@PathVariable String uuid) {
        log.info("Gateway: Received GET request for admin UUID: {}", uuid);
        // Let exceptions (NotFoundException) propagate to GlobalExceptionHandler
        AdminResponseModel admin = adminService.getAdminById(uuid);
        return ResponseEntity.ok(admin);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AdminResponseModel> addAdmin(@RequestBody AdminRequestModel adminRequestModel) {
        log.info("Gateway: Received POST request to add admin");
        AdminResponseModel addedAdmin = adminService.addAdmin(adminRequestModel);
        // Let exceptions (InvalidInputException) propagate
        return new ResponseEntity<>(addedAdmin, HttpStatus.CREATED); // Status CREATED
    }

    @PutMapping(value = "{uuid}", consumes = "application/json")
    public ResponseEntity<Void> updateAdmin(@RequestBody AdminRequestModel adminRequestModel, @PathVariable String uuid) {
        log.info("Gateway: Received PUT request to update admin UUID: {}", uuid);
        // Service layer method returns void because client returns void/null
        adminService.updateAdmin(adminRequestModel, uuid);
        // Let exceptions propagate
        return ResponseEntity.noContent().build(); // Status NO_CONTENT or OK() if preferred
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String uuid) {
        log.info("Gateway: Received DELETE request for admin UUID: {}", uuid);
        adminService.deleteAdmin(uuid);
        // Let exceptions propagate
        return ResponseEntity.noContent().build(); // Status NO_CONTENT
    }
}
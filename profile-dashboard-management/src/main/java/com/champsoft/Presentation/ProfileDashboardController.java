package com.champsoft.Presentation;


import com.champsoft.BusinessLogic.ProfileDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/profile-dashboards", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProfileDashboardController {

    private final ProfileDashboardService dashboardService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDashboardResponseDto> getProfileDashboard(@PathVariable String userId) {
        UserProfileDashboardResponseDto dashboard = dashboardService.getOrCreateProfileDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping
    public ResponseEntity<List<UserProfileDashboardResponseDto>> getAllProfileDashboards() {
        List<UserProfileDashboardResponseDto> dashboards = dashboardService.getAllPersistedDashboards();
        return ResponseEntity.ok(dashboards);
    }

    // POST to explicitly create/refresh a dashboard
    // Could take a request body if more params are needed, e.g. DashboardCreationRequestDto with userId
    @PostMapping("/{userId}") // Or just @PostMapping and expect userId in a body
    public ResponseEntity<UserProfileDashboardResponseDto> createOrRefreshDashboard(@PathVariable String userId) {
        UserProfileDashboardResponseDto dashboard = dashboardService.createOrRefreshDashboard(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dashboard);
    }

    // PUT by AggregateId to explicitly update/refresh a dashboard
    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileDashboardResponseDto> updateProfileDashboard(@PathVariable String userId) {

        UserProfileDashboardResponseDto dashboard = dashboardService.updateDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    // DELETE by AggregateId to remove the persisted dashboard
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteProfileDashboard(@PathVariable String userId) {
        dashboardService.deletePersistedDashboard(userId);
        return ResponseEntity.noContent().build();
    }
}
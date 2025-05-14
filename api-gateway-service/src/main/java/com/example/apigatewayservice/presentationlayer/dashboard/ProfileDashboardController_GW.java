package com.example.apigatewayservice.presentationlayer.dashboard;

import com.example.apigatewayservice.businesslogiclayer.dashboard.ProfileDashboardService_GW;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/profile-dashboards") // Matching the aggregator's base path
@RequiredArgsConstructor
public class ProfileDashboardController_GW {

    private final ProfileDashboardService_GW dashboardService;

    @GetMapping(value = "/{userId}", produces = "application/hal+json")
    public ResponseEntity<UserProfileDashboardResponseDTO_GW> getProfileDashboard(@PathVariable String userId) {
        log.info("API Gateway: Received GET request for profile dashboard with userId: {}", userId);
        UserProfileDashboardResponseDTO_GW dashboard = dashboardService.getProfileDashboardByUserId(userId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping(produces = "application/hal+json")
    public ResponseEntity<List<UserProfileDashboardResponseDTO_GW>> getAllProfileDashboards(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        log.info("API Gateway: Received GET request for all profile dashboards");
        List<UserProfileDashboardResponseDTO_GW> dashboards = dashboardService.getAllProfileDashboards();

        

        return ResponseEntity.ok(dashboards);
    }

    @PostMapping(value = "/{userId}", produces = "application/hal+json")
    public ResponseEntity<UserProfileDashboardResponseDTO_GW> createOrRefreshDashboard(@PathVariable String userId) {
        log.info("API Gateway: Received POST request to create/refresh dashboard for userId: {}", userId);
        UserProfileDashboardResponseDTO_GW dashboard = dashboardService.createOrRefreshDashboard(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dashboard);
    }

    @PutMapping(value = "/{userId}", produces = "application/hal+json")
    public ResponseEntity<UserProfileDashboardResponseDTO_GW> updateProfileDashboard(@PathVariable String userId) {
        log.info("API Gateway: Received PUT request to update dashboard for userId: {}", userId);
        UserProfileDashboardResponseDTO_GW dashboard = dashboardService.updateProfileDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteProfileDashboard(@PathVariable String userId) {
        log.info("API Gateway: Received DELETE request for dashboard with userId: {}", userId);
        dashboardService.deleteProfileDashboard(userId);
        return ResponseEntity.noContent().build();
    }
}
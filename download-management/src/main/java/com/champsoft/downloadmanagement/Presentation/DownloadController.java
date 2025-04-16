package com.champsoft.downloadmanagement.Presentation;


import com.champsoft.downloadmanagement.BusinessLogic.DownloadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/downloads", produces = MediaType.APPLICATION_JSON_VALUE)@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping
    public ResponseEntity<?> createDownload(@RequestBody CreateDownloadRequest request) {
        try {
            DownloadDto newDownload = downloadService.createDownload(request.url());
            return ResponseEntity.status(HttpStatus.CREATED).body(newDownload);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DownloadDto> getDownload(@PathVariable UUID id) {
        DownloadDto dto = downloadService.getDownloadStatus(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<DownloadDto>> getAllDownloads() {
        List<DownloadDto> downloads = downloadService.getAllDownloads();
        return ResponseEntity.ok(downloads);
    }

    @PostMapping("/{id}/start") // Use POST for actions/commands
    public ResponseEntity<DownloadDto> startDownload(@PathVariable UUID id) {
        DownloadDto updatedDto = downloadService.startDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<DownloadDto> pauseDownload(@PathVariable UUID id) {
        DownloadDto updatedDto = downloadService.pauseDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<DownloadDto> resumeDownload(@PathVariable UUID id) {
        DownloadDto updatedDto = downloadService.resumeDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/cancel") // Or use DELETE
    public ResponseEntity<DownloadDto> cancelDownload(@PathVariable UUID id) {
        DownloadDto updatedDto = downloadService.cancelDownload(id);
        return ResponseEntity.ok(updatedDto); // Return final state
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDownload(@PathVariable UUID id) {
        downloadService.deleteDownload(id); // Service handles not found exception
        return ResponseEntity.noContent().build();
    }


    // Request body DTO for creation
    record CreateDownloadRequest(String url) {}

    // --- Exception Handlers (can be moved to a @ControllerAdvice) ---
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Set HTTP status code
    public Map<String, String> handleNotFound(EntityNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict for invalid state transitions
    public Map<String, String> handleIllegalState(IllegalStateException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
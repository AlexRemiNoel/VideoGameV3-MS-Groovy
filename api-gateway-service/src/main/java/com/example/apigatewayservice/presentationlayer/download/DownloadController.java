package com.example.apigatewayservice.presentationlayer.download;

import com.example.apigatewayservice.businesslogiclayer.download.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/downloads", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DownloadResponseModel> createDownload(@RequestBody DownloadRequestModel downloadRequestModel) {
        log.debug("1. Received in API-Gateway Download Controller createDownload");
        DownloadResponseModel newDownload = downloadService.createDownload(downloadRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDownload);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DownloadResponseModel> getDownload(@PathVariable String id) {
        log.debug("1. Received in API-Gateway Download Controller getDownload for id: {}", id);
        DownloadResponseModel dto = downloadService.getDownload(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<DownloadResponseModel>> getAllDownloads() {
        log.debug("1. Received in API-Gateway Download Controller getAllDownloads");
        List<DownloadResponseModel> downloads = downloadService.getAllDownloads();
        return ResponseEntity.ok(downloads);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<DownloadResponseModel> startDownload(@PathVariable String id) {
        log.debug("1. Received in API-Gateway Download Controller startDownload for id: {}", id);
        DownloadResponseModel updatedDto = downloadService.startDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<DownloadResponseModel> pauseDownload(@PathVariable String id) {
        log.debug("1. Received in API-Gateway Download Controller pauseDownload for id: {}", id);
        DownloadResponseModel updatedDto = downloadService.pauseDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<DownloadResponseModel> resumeDownload(@PathVariable String id) {
        log.debug("1. Received in API-Gateway Download Controller resumeDownload for id: {}", id);
        DownloadResponseModel updatedDto = downloadService.resumeDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<DownloadResponseModel> cancelDownload(@PathVariable String id) {
        log.debug("1. Received in API-Gateway Download Controller cancelDownload for id: {}", id);
        DownloadResponseModel updatedDto = downloadService.cancelDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDownload(@PathVariable String id) {
        log.debug("1. Received in API-Gateway Download Controller deleteDownload for id: {}", id);
        downloadService.deleteDownload(id);
        return ResponseEntity.noContent().build();
    }
}
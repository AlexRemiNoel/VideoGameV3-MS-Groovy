package com.example.videogamev3.DownloadManagement.Presentation;

import com.example.videogamev3.DownloadManagement.BusinessLogic.DownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/downloads", produces = MediaType.APPLICATION_JSON_VALUE)@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping
    public ResponseEntity<DownloadResponseModel> createDownload(@RequestBody DownloadRequestModel downloadRequestModel) {

        DownloadResponseModel newDownload = downloadService.createDownload(downloadRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDownload);

    }

    @GetMapping("/{id}")
    public ResponseEntity<DownloadResponseModel> getDownload(@PathVariable String id) {
        DownloadResponseModel dto = downloadService.getDownload(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<DownloadResponseModel>> getAllDownloads() {
        List<DownloadResponseModel> downloads = downloadService.getAllDownloads();
        return ResponseEntity.ok(downloads);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<DownloadResponseModel> startDownload(@PathVariable String id) {
        DownloadResponseModel updatedDto = downloadService.startDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<DownloadResponseModel> pauseDownload(@PathVariable String id) {
        DownloadResponseModel updatedDto = downloadService.pauseDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<DownloadResponseModel> resumeDownload(@PathVariable String id) {
        DownloadResponseModel updatedDto = downloadService.resumeDownload(id);
        return ResponseEntity.ok(updatedDto);
    }

    @PostMapping("/{id}/cancel") // Or use DELETE
    public ResponseEntity<DownloadResponseModel> cancelDownload(@PathVariable String id) {
        DownloadResponseModel updatedDto = downloadService.cancelDownload(id);
        return ResponseEntity.ok(updatedDto); // Return final state
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDownload(@PathVariable String id) {
        downloadService.deleteDownload(id);
        return ResponseEntity.noContent().build();
    }


}
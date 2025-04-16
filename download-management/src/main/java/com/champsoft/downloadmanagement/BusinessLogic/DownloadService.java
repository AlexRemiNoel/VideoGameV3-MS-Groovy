package com.champsoft.downloadmanagement.BusinessLogic;


import com.champsoft.downloadmanagement.DataAccess.DownloadManager;
import com.champsoft.downloadmanagement.DataAccess.DownloadManagerRepository;
import com.champsoft.downloadmanagement.DataAccess.DownloadStatus;
import com.champsoft.downloadmanagement.Presentation.DownloadDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private final DownloadManagerRepository downloadManagerRepository;

    @Transactional
    public DownloadDto createDownload(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalArgumentException("Source URL cannot be blank");
        }
        DownloadManager newDownload = new DownloadManager(
                UUID.randomUUID(),   // Generate ID here
                sourceUrl,
                DownloadStatus.PENDING // Initial status
        );
        DownloadManager saved = downloadManagerRepository.save(newDownload);
        System.out.println("Created download " + saved.getId() + " for URL: " + sourceUrl);
        return DownloadDto.fromEntity(saved);
    }

    @Transactional
    public DownloadDto startDownload(UUID id) {
        DownloadManager download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.PENDING || download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.DOWNLOADING); // Direct status update
            downloadManagerRepository.save(download);
            System.out.println("Started download " + id);
        } else {
            System.err.println("Cannot start download " + id + " from state: " + download.getDownloadStatus());
        }
        return DownloadDto.fromEntity(download);
    }

    // UPDATE: Pause the download
    @Transactional
    public DownloadDto pauseDownload(UUID id) {
        DownloadManager download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            download.setDownloadStatus(DownloadStatus.PAUSED); // Direct status update
            downloadManagerRepository.save(download);
            System.out.println("Paused download " + id);
        } else {
            System.err.println("Cannot pause download " + id + " from state: " + download.getDownloadStatus());
        }
        return DownloadDto.fromEntity(download);
    }

    @Transactional
    public DownloadDto resumeDownload(UUID id) {
        DownloadManager download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.DOWNLOADING); // Direct status update
            downloadManagerRepository.save(download);
            System.out.println("Resumed download " + id);
        } else {
            System.err.println("Cannot resume download " + id + " from state: " + download.getDownloadStatus());
        }
        return DownloadDto.fromEntity(download);
    }

    // UPDATE: Cancel the download
    @Transactional
    public DownloadDto cancelDownload(UUID id) {
        DownloadManager download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() != DownloadStatus.COMPLETED && download.getDownloadStatus() != DownloadStatus.CANCELLED) {
            download.setDownloadStatus(DownloadStatus.CANCELLED); // Direct status update
            downloadManagerRepository.save(download);
            System.out.println("Cancelled download " + id);
        } else {
            System.err.println("Cannot cancel download " + id + " from state: " + download.getDownloadStatus());
        }
        return DownloadDto.fromEntity(download);
    }

    public DownloadDto getDownloadStatus(UUID id) {
        DownloadManager download = findDownloadManagerOrFail(id);
        return DownloadDto.fromEntity(download);
    }

    // READ: Get all downloads
    public List<DownloadDto> getAllDownloads() {
        return downloadManagerRepository.findAll()
                .stream()
                .map(DownloadDto::fromEntity)
                .collect(Collectors.toList());
    }

    // DELETE: Remove a download record (Optional - might not be needed if you keep history)
    @Transactional
    public void deleteDownload(UUID id) {
        if (downloadManagerRepository.existsById(id)) {
            downloadManagerRepository.deleteById(id);
            System.out.println("Deleted download " + id);
        } else {
            throw new EntityNotFoundException("DownloadManager not found with id: " + id);
        }
    }


    // --- Helper Method ---
    private DownloadManager findDownloadManagerOrFail(UUID id) {
        return downloadManagerRepository.findById(id) // Find by UUID directly
                .orElseThrow(() -> new EntityNotFoundException("DownloadManager not found with id: " + id));
    }

    // --- Methods to simulate completion/failure (called externally) ---
    @Transactional
    public DownloadDto markCompleted(UUID id) {
        DownloadManager download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            download.setDownloadStatus(DownloadStatus.COMPLETED);
            downloadManagerRepository.save(download);
            System.out.println("Marked download " + id + " as COMPLETED");
        } else {
            System.err.println("Cannot mark completed download " + id + " from state: " + download.getDownloadStatus());
        }
        return DownloadDto.fromEntity(download);
    }

    @Transactional
    public DownloadDto markFailed(UUID id) {
        DownloadManager download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING || download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.FAILED);
            downloadManagerRepository.save(download);
            System.out.println("Marked download " + id + " as FAILED");
        } else {
            System.err.println("Cannot mark failed download " + id + " from state: " + download.getDownloadStatus());
        }
        return DownloadDto.fromEntity(download);
    }
}
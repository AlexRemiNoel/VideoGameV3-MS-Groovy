package com.example.videogamev3.DownloadManagement.BusinessLogic;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadRequestMapper;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadResponseMapper;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DuplicateDownloadIDException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DownloadNotFoundException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.InvalidDownloadDataException; // Import added
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import Slf4j
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // For checking empty strings

import java.net.MalformedURLException; // Import added
import java.net.URL; // Import added
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j // Added for logging
public class DownloadService {

    private final DownloadRepository downloadRepository;
    private final DownloadResponseMapper downloadResponseMapper;
    private final DownloadRequestMapper downloadRequestMapper;

    @Transactional
    public DownloadResponseModel createDownload(DownloadRequestModel downloadRequestModel) {
        log.info("Attempting to create new download from request: {}", downloadRequestModel);

        // --- Input Validation ---
        validateDownloadRequestData(downloadRequestModel); // Throw InvalidDownloadDataException if invalid

        // --- Proceed with creation ---
        Download download = downloadRequestMapper.downloadRequestModelToDownload(downloadRequestModel);
        download.setDownloadStatus(DownloadStatus.PENDING);

        // Generate and check ID (Handling potential extremely rare collision)
        DownloadId downloadId = new DownloadId(UUID.randomUUID().toString());
        if (downloadRepository.existsDownloadById_Uuid(downloadId.getUuid())) {
            // THROW DuplicateDownloadIDException (Correctly placed)
            log.warn("Duplicate Download ID generated (collision): {}", downloadId.getUuid());
            throw new DuplicateDownloadIDException("Duplicate Download ID generated: " + downloadId.getUuid());
        }
        download.setId(downloadId);

        Download savedDownload = downloadRepository.save(download);
        log.info("Successfully created download with ID: {}", savedDownload.getId().getUuid());
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(savedDownload);
    }

    // No annotation needed if read-only and not modifying state within transaction
    public DownloadResponseModel getDownload(String downloadId) {
        log.debug("Attempting to retrieve download with ID: {}", downloadId);
        // findDownloadManagerOrFail handles the DownloadNotFoundException
        Download download = findDownloadManagerOrFail(downloadId);
        log.debug("Found download: {}", downloadId);
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel startDownload(String id) {
        log.info("Attempting to start download with ID: {}", id);
        Download download = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if not found

        // Add state validation logic if needed (e.g., throw InvalidDownloadStateException)
        if (download.getDownloadStatus() == DownloadStatus.PENDING || download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.DOWNLOADING);
            downloadRepository.save(download);
            log.info("Started download {}", id);
        } else {
            // Consider throwing InvalidDownloadStateException instead of just logging error
            log.warn("Cannot start download {} from state: {}", id, download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel pauseDownload(String id) {
        log.info("Attempting to pause download with ID: {}", id);
        Download download = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if not found

        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            download.setDownloadStatus(DownloadStatus.PAUSED);
            downloadRepository.save(download);
            log.info("Paused download {}", id);
        } else {
            log.warn("Cannot pause download {} from state: {}", id, download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel resumeDownload(String id) {
        log.info("Attempting to resume download with ID: {}", id);
        Download download = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if not found

        if (download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.DOWNLOADING);
            downloadRepository.save(download);
            log.info("Resumed download {}", id);
        } else {
            log.warn("Cannot resume download {} from state: {}", id, download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel cancelDownload(String id) {
        log.info("Attempting to cancel download with ID: {}", id);
        Download download = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if not found

        if (download.getDownloadStatus() != DownloadStatus.COMPLETED && download.getDownloadStatus() != DownloadStatus.CANCELLED) {
            download.setDownloadStatus(DownloadStatus.CANCELLED);
            downloadRepository.save(download);
            log.info("Cancelled download {}", id);
        } else {
            log.warn("Cannot cancel download {} from state: {}", id, download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    // No annotation needed for read-only
    public List<DownloadResponseModel> getAllDownloads() {
        log.debug("Retrieving all downloads");
        // Remove the broad try-catch. Let repository exceptions propagate
        // to be handled by the global handler (e.g., database down -> 500).
        List<Download> downloads = downloadRepository.findAll();
        log.debug("Found {} downloads", downloads.size());
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(downloads);
    }

    @Transactional
    public void deleteDownload(String id) {
        log.info("Attempting to delete download with ID: {}", id);
        // Use findDownloadManagerOrFail to ensure it exists before deleting
        Download downloadToDelete = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if not found

        // If findDownloadManagerOrFail succeeds, the download exists.
        downloadRepository.delete(downloadToDelete); // Use delete(entity) for clarity
        log.info("Deleted download {}", id);

        /* // Alternative using existsById/deleteById - keep only one approach
        if (downloadRepository.existsDownloadById_Uuid(id)) { // Assuming existsById checks UUID string
            downloadRepository.deleteById(id); // Assuming deleteById works with String UUID
            log.info("Deleted download {}", id);
        } else {
            // THROW DownloadNotFoundException (Handled by findDownloadManagerOrFail above)
            log.warn("Cannot delete download {}, it was not found.", id);
            throw new DownloadNotFoundException("Cannot delete. Download not found with id: " + id);
        }
        */
    }

    // Helper method to find download or throw DownloadNotFoundException
    private Download findDownloadManagerOrFail(String id) {
        log.debug("Finding download entity with ID: {}", id);
        Download download = downloadRepository.findDownloadById_Uuid(id);
        if (download == null) {
            // THROW DownloadNotFoundException (Primary location)
            log.warn("Download not found with ID: {}", id);
            throw new DownloadNotFoundException("Download not found with ID: " + id);
        }
        return download;
    }

    @Transactional
    public DownloadResponseModel markCompleted(String id) {
        log.info("Attempting to mark download {} as COMPLETED", id);
        Download download = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if not found

        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            download.setDownloadStatus(DownloadStatus.COMPLETED);
            downloadRepository.save(download);
            log.info("Marked download {} as COMPLETED", id);
        } else {
            log.warn("Cannot mark completed download {} from state: {}", id, download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel markFailed(String id) {
        log.info("Attempting to mark download {} as FAILED", id);
        Download download = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if not found

        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING || download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.FAILED);
            downloadRepository.save(download);
            log.info("Marked download {} as FAILED", id);
        } else {
            log.warn("Cannot mark failed download {} from state: {}", id, download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional // Added annotation as it modifies data
    public DownloadResponseModel updateDownload(String id, DownloadRequestModel downloadRequestModel) {
        log.info("Attempting to update download {} with data: {}", id, downloadRequestModel);
        Download download = findDownloadManagerOrFail(id); // Throws DownloadNotFoundException if needed

        // --- Input Validation for Update ---
        validateDownloadRequestData(downloadRequestModel); // Throw InvalidDownloadDataException if invalid

        // --- Apply Updates ---
        // Only update allowed fields from the request model
        download.setSourceUrl(downloadRequestModel.getSourceUrl());
        // Potentially update other fields here if added to RequestModel and allowed

        Download updatedDownload = downloadRepository.save(download); // Save validated changes
        log.info("Successfully updated download with ID: {}", id);
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(updatedDownload);
    }

    // --- Helper method for validation ---
    private void validateDownloadRequestData(DownloadRequestModel requestModel) {
        if (requestModel == null) {
            throw new InvalidDownloadDataException("Download request data cannot be null.");
        }
        // Use StringUtils for robust null/empty check
        if (!StringUtils.hasText(requestModel.getSourceUrl())) {
            // THROW InvalidDownloadDataException
            throw new InvalidDownloadDataException("Source URL cannot be null or empty.");
        }
        // Basic URL format check
        try {
            new URL(requestModel.getSourceUrl());
        } catch (MalformedURLException e) {
            // THROW InvalidDownloadDataException
            throw new InvalidDownloadDataException("Source URL is malformed: " + requestModel.getSourceUrl());
        }
        // Add other validation checks here (e.g., target path format, user details if applicable)
    }
}
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j // Added for logging
public class DownloadService {

    private final DownloadRepository downloadRepository;
    private final DownloadResponseMapper downloadResponseMapper;
    private final DownloadRequestMapper downloadRequestMapper;

    public DownloadResponseModel createDownload(DownloadRequestModel downloadRequestModel) {
        log.info("Attempting to create new download from request: {}", downloadRequestModel);


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

    public void deleteDownload(String id) {
        log.info("Attempting to delete download with ID: {}", id);
        Download downloadToDelete = findDownloadManagerOrFail(id);

        downloadRepository.delete(downloadToDelete);
        log.info("Deleted download {}", id);

    }

    private Download findDownloadManagerOrFail(String id) {
        log.debug("Finding download entity with ID: {}", id);
        Download download = downloadRepository.findDownloadById_Uuid(id);
        if (download == null) {
            log.warn("Download not found with ID: {}", id);
            throw new DownloadNotFoundException("Download not found with ID: " + id);
        }
        return download;
    }
    @Autowired
    MongoOperations mongoTemplate;
    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty>
                mappingContext =
                mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        IndexOperations indexOps = mongoTemplate.indexOps(Download.class);
        resolver.resolveIndexFor(Download.class).forEach(indexOps::ensureIndex);
    }

    @Bean
    CommandLineRunner initData(DownloadRepository downloadRepository) {
        return args -> {
            downloadRepository.deleteAll();
            if (downloadRepository.count() == 0) {
                downloadRepository.saveAll(List.of(
                        new Download(
                                new DownloadId(), "http://pooper.com", DownloadStatus.DOWNLOADING
                        ),
                        new Download(
                                new DownloadId(), "http://tsn.com", DownloadStatus.DOWNLOADING
                        )
                ));
            }
        };
    }

    public DownloadResponseModel updateDownload(String id, DownloadRequestModel downloadRequestModel) {
        Download download = findDownloadManagerOrFail(id);

        download.setSourceUrl(downloadRequestModel.getSourceUrl());
        downloadRepository.save(download);
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }
}
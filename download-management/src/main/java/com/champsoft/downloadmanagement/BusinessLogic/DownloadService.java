package com.example.videogamev3.DownloadManagement.BusinessLogic;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadRequestMapper;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadResponseMapper;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DownloadService {

    private final DownloadRepository downloadRepository;
    private final DownloadResponseMapper downloadResponseMapper;
    private final DownloadRequestMapper downloadRequestMapper;

    @Transactional
    public DownloadResponseModel createDownload(DownloadRequestModel downloadRequestModel) {
        Download download = downloadRequestMapper.downloadRequestModelToDownload(downloadRequestModel);
        download.setDownloadStatus(DownloadStatus.PENDING);
        download.setId(new DownloadId(UUID.randomUUID().toString()));
        System.out.println(download.toString());
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadRepository.save(download));
    }

    @Transactional
    public DownloadResponseModel getDownload(String downloadId) {
        Download download = downloadRepository.findDownloadById_Uuid(downloadId);
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel startDownload(String id) {
        Download download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.PENDING || download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.DOWNLOADING);
            downloadRepository.save(download);
            System.out.println("Started download " + id);
        } else {
            System.err.println("Cannot start download " + id + " from state: " + download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel pauseDownload(String id) {
        Download download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            download.setDownloadStatus(DownloadStatus.PAUSED); // Direct status update
            downloadRepository.save(download);
            System.out.println("Paused download " + id);
        } else {
            System.err.println("Cannot pause download " + id + " from state: " + download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel resumeDownload(String id) {
        Download download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.DOWNLOADING);
            downloadRepository.save(download);
            System.out.println("Resumed download " + id);
        } else {
            System.err.println("Cannot resume download " + id + " from state: " + download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel cancelDownload(String id) {
        Download download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() != DownloadStatus.COMPLETED && download.getDownloadStatus() != DownloadStatus.CANCELLED) {
            download.setDownloadStatus(DownloadStatus.CANCELLED);
            downloadRepository.save(download);
            System.out.println("Cancelled download " + id);
        } else {
            System.err.println("Cannot cancel download " + id + " from state: " + download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    public DownloadResponseModel getDownloadStatus(String id) {
        Download download = findDownloadManagerOrFail(id);
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    public List<DownloadResponseModel> getAllDownloads() {
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadRepository.findAll());
    }

    @Transactional
    public void deleteDownload(String id) {
        if (downloadRepository.existsById(id)) {
            downloadRepository.deleteById(id);
            System.out.println("Deleted download " + id);
        } else {
            throw new EntityNotFoundException("DownloadManager not found with id: " + id);
        }
    }

    private Download findDownloadManagerOrFail(String id) {
        Download download = downloadRepository.findDownloadById_Uuid(id);
        if (download == null) {
            throw new EntityNotFoundException("DownloadManager not found with id: " + id);
        }
        return download;
    }

    @Transactional
    public DownloadResponseModel markCompleted(String id) {
        Download download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            download.setDownloadStatus(DownloadStatus.COMPLETED);
            downloadRepository.save(download);
            System.out.println("Marked download " + id + " as COMPLETED");
        } else {
            System.err.println("Cannot mark completed download " + id + " from state: " + download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }

    @Transactional
    public DownloadResponseModel markFailed(String id) {
        Download download = findDownloadManagerOrFail(id);
        if (download.getDownloadStatus() == DownloadStatus.DOWNLOADING || download.getDownloadStatus() == DownloadStatus.PAUSED) {
            download.setDownloadStatus(DownloadStatus.FAILED);
            downloadRepository.save(download);
            System.out.println("Marked download " + id + " as FAILED");
        } else {
            System.err.println("Cannot mark failed download " + id + " from state: " + download.getDownloadStatus());
        }
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);
    }
}
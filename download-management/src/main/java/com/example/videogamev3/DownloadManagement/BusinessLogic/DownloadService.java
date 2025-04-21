package com.example.videogamev3.DownloadManagement.BusinessLogic;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadRequestMapper;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadResponseMapper;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import com.example.videogamev3.DownloadManagement.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        if (download == null) {
            throw new NotFoundException ("Download not found");
        }
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



    public List<DownloadResponseModel> getAllDownloads() {
        try{
            List<DownloadResponseModel> list = downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadRepository.findAll());
            return list;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ArrayList<DownloadResponseModel>();
        }

    }

    @Transactional
    public void deleteDownload(String id) {
        if (downloadRepository.existsById(id)) {
            downloadRepository.deleteById(id);
            System.out.println("Deleted download " + id);
        } else {
            throw new NotFoundException("DownloadManager not found with id: " + id);
        }

    }

    private Download findDownloadManagerOrFail(String id) {
        Download download = downloadRepository.findDownloadById_Uuid(id);
        if (download == null) {
            throw new NotFoundException("DownloadManager not found with id: " + id);
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

    public DownloadResponseModel updateDownload(String id, DownloadRequestModel downloadRequestModel) {
        Download download = findDownloadManagerOrFail(id);

        download.setSourceUrl(downloadRequestModel.getSourceUrl());
        downloadRepository.save(download);
        return downloadResponseMapper.downloadEntityToDownloadResponseModel(download);

    }
}
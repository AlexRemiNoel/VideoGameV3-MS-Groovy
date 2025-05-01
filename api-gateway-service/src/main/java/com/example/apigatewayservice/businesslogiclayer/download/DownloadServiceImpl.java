package com.example.apigatewayservice.businesslogiclayer.download;

import com.example.apigatewayservice.DomainClientLayer.download.DownloadServiceClient;
import com.example.apigatewayservice.presentationlayer.download.DownloadRequestModel;
import com.example.apigatewayservice.presentationlayer.download.DownloadResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadServiceImpl implements DownloadService {

    private final DownloadServiceClient downloadServiceClient;

    @Override
    public DownloadResponseModel createDownload(DownloadRequestModel downloadRequestModel) {
        log.debug("2. Delegating createDownload to client");
        return downloadServiceClient.createDownload(downloadRequestModel);
    }

    @Override
    public DownloadResponseModel getDownload(String id) {
        log.debug("2. Delegating getDownload for id {} to client", id);
        return downloadServiceClient.getDownload(id);
    }

    @Override
    public List<DownloadResponseModel> getAllDownloads() {
        log.debug("2. Delegating getAllDownloads to client");
        return downloadServiceClient.getAllDownloads();
    }

    @Override
    public DownloadResponseModel startDownload(String id) {
        log.debug("2. Delegating startDownload for id {} to client", id);
        return downloadServiceClient.startDownload(id);
    }

    @Override
    public DownloadResponseModel pauseDownload(String id) {
        log.debug("2. Delegating pauseDownload for id {} to client", id);
        return downloadServiceClient.pauseDownload(id);
    }

    @Override
    public DownloadResponseModel resumeDownload(String id) {
        log.debug("2. Delegating resumeDownload for id {} to client", id);
        return downloadServiceClient.resumeDownload(id);
    }

    @Override
    public DownloadResponseModel cancelDownload(String id) {
        log.debug("2. Delegating cancelDownload for id {} to client", id);
        return downloadServiceClient.cancelDownload(id);
    }

    @Override
    public void deleteDownload(String id) {
        log.debug("2. Delegating deleteDownload for id {} to client", id);
        downloadServiceClient.deleteDownload(id);
    }

    @Override
    public DownloadResponseModel updateDownload(String id, DownloadRequestModel downloadRequestModel) {
        return downloadServiceClient.updateDownload(id, downloadRequestModel);
    }
}

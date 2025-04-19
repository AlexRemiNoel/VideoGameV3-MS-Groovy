package com.example.apigatewayservice.businesslogiclayer;

import com.example.apigatewayservice.presentationlayer.DownloadRequestModel;
import com.example.apigatewayservice.presentationlayer.DownloadResponseModel;

import java.util.List;

public interface DownloadService {
    DownloadResponseModel createDownload(DownloadRequestModel downloadRequestModel);
    DownloadResponseModel getDownload(String id);
    List<DownloadResponseModel> getAllDownloads();
    DownloadResponseModel startDownload(String id);
    DownloadResponseModel pauseDownload(String id);
    DownloadResponseModel resumeDownload(String id);
    DownloadResponseModel cancelDownload(String id);
    void deleteDownload(String id);
}
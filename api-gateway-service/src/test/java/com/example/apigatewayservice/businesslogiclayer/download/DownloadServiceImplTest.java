package com.example.apigatewayservice.businesslogiclayer.download;

import com.example.apigatewayservice.DomainClientLayer.download.DownloadServiceClient;
import com.example.apigatewayservice.presentationlayer.download.DownloadRequestModel;
import com.example.apigatewayservice.presentationlayer.download.DownloadResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceImplTest {

    @Mock
    private DownloadServiceClient downloadServiceClient;

    @InjectMocks
    private DownloadServiceImpl downloadService;

    private final String DOWNLOAD_ID = UUID.randomUUID().toString();

    private DownloadResponseModel buildSampleDownloadResponseModel() {
        return DownloadResponseModel.builder().id(DOWNLOAD_ID).status("PENDING").build();
    }

    private DownloadRequestModel buildSampleDownloadRequestModel() {
        return DownloadRequestModel.builder().sourceUrl("http://example.com/file").build();
    }

    @Test
    void createDownload_callsClient() {
        DownloadRequestModel requestModel = buildSampleDownloadRequestModel();
        DownloadResponseModel expectedResponse = buildSampleDownloadResponseModel();
        when(downloadServiceClient.createDownload(requestModel)).thenReturn(expectedResponse);
        DownloadResponseModel actualResponse = downloadService.createDownload(requestModel);
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).createDownload(requestModel);
    }

    @Test
    void getDownload_callsClient() {
        DownloadResponseModel expectedResponse = buildSampleDownloadResponseModel();
        when(downloadServiceClient.getDownload(DOWNLOAD_ID)).thenReturn(expectedResponse);
        DownloadResponseModel actualResponse = downloadService.getDownload(DOWNLOAD_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).getDownload(DOWNLOAD_ID);
    }

    @Test
    void getAllDownloads_callsClient() {
        List<DownloadResponseModel> expectedResponse = List.of(buildSampleDownloadResponseModel());
        when(downloadServiceClient.getAllDownloads()).thenReturn(expectedResponse);
        List<DownloadResponseModel> actualResponse = downloadService.getAllDownloads();
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).getAllDownloads();
    }

    @Test
    void startDownload_callsClient() {
        DownloadResponseModel expectedResponse = buildSampleDownloadResponseModel();
        when(downloadServiceClient.startDownload(DOWNLOAD_ID)).thenReturn(expectedResponse);
        DownloadResponseModel actualResponse = downloadService.startDownload(DOWNLOAD_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).startDownload(DOWNLOAD_ID);
    }

    @Test
    void pauseDownload_callsClient() {
        DownloadResponseModel expectedResponse = buildSampleDownloadResponseModel();
        when(downloadServiceClient.pauseDownload(DOWNLOAD_ID)).thenReturn(expectedResponse);
        DownloadResponseModel actualResponse = downloadService.pauseDownload(DOWNLOAD_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).pauseDownload(DOWNLOAD_ID);
    }

    @Test
    void resumeDownload_callsClient() {
        DownloadResponseModel expectedResponse = buildSampleDownloadResponseModel();
        when(downloadServiceClient.resumeDownload(DOWNLOAD_ID)).thenReturn(expectedResponse);
        DownloadResponseModel actualResponse = downloadService.resumeDownload(DOWNLOAD_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).resumeDownload(DOWNLOAD_ID);
    }

    @Test
    void cancelDownload_callsClient() {
        DownloadResponseModel expectedResponse = buildSampleDownloadResponseModel();
        when(downloadServiceClient.cancelDownload(DOWNLOAD_ID)).thenReturn(expectedResponse);
        DownloadResponseModel actualResponse = downloadService.cancelDownload(DOWNLOAD_ID);
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).cancelDownload(DOWNLOAD_ID);
    }

    @Test
    void deleteDownload_callsClient() {
        doNothing().when(downloadServiceClient).deleteDownload(DOWNLOAD_ID);
        downloadService.deleteDownload(DOWNLOAD_ID);
        verify(downloadServiceClient, times(1)).deleteDownload(DOWNLOAD_ID);
    }
    
    @Test
    void updateDownload_callsClient() {
        DownloadRequestModel requestModel = buildSampleDownloadRequestModel();
        DownloadResponseModel expectedResponse = buildSampleDownloadResponseModel();
        when(downloadServiceClient.updateDownload(DOWNLOAD_ID, requestModel)).thenReturn(expectedResponse);
        DownloadResponseModel actualResponse = downloadService.updateDownload(DOWNLOAD_ID, requestModel);
        assertEquals(expectedResponse, actualResponse);
        verify(downloadServiceClient, times(1)).updateDownload(DOWNLOAD_ID, requestModel);
    }
} 
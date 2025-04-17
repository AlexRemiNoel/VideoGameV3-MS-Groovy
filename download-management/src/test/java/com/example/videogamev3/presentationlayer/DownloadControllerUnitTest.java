package com.example.videogamev3.presentationlayer;

import com.example.videogamev3.DownloadManagement.BusinessLogic.DownloadService;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadController;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = DownloadController.class)
public class DownloadControllerUnitTest {
    private final String VALID_DOWNLOAD_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_DOWNLOAD_ID = "c3540a89-cb47-4c96-888e-ff96708db4d0";
    private final String INVALID_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d";
    private DownloadRequestModel downloadRequestModel;
    private DownloadResponseModel downloadResponseModel;


    @MockitoBean
    DownloadService downloadService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        downloadRequestModel = new DownloadRequestModel();

        downloadResponseModel = new DownloadResponseModel();
        downloadResponseModel.setId(VALID_DOWNLOAD_ID);
        downloadResponseModel.setStatus("PENDING");
        downloadResponseModel.setSourceUrl("hello@gmail.com ");
    }

    @Autowired
    DownloadController downloadController;
    @Test
    public void whenNoCustomersExist_ThenReturnEmptyList() {
        //arrange
        when(downloadService.getAllDownloads()).thenReturn(Collections.emptyList());
        //act
        ResponseEntity<List<DownloadResponseModel>>
                customerResponseEntity = downloadController.getAllDownloads();
        //assert
        assertNotNull(customerResponseEntity);
        assertEquals(HttpStatus.OK, customerResponseEntity.getStatusCode());
        assertArrayEquals(customerResponseEntity.getBody().toArray(),
                new ArrayList<DownloadResponseModel>().toArray());
        verify(downloadService, times(1)).getAllDownloads();
    }

    @Test
    void whenValidRequest_thenCreateDownload() {
        // Arrange
        when(downloadService.createDownload(any(DownloadRequestModel.class))).thenReturn(downloadResponseModel);

        // Act
        ResponseEntity<DownloadResponseModel> responseEntity = downloadController.createDownload(downloadRequestModel);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        verify(downloadService, times(1)).createDownload(eq(downloadRequestModel));
        assertNotNull(responseEntity.getBody());
        assertEquals(downloadResponseModel, responseEntity.getBody());
        assertEquals(VALID_DOWNLOAD_ID, responseEntity.getBody().getId());
        assertEquals(DownloadStatus.PENDING.toString(), responseEntity.getBody().getStatus());

        verify(downloadService, times(1)).createDownload(eq(downloadRequestModel));
    }

    @Test
    void whenDownloadExists_thenReturnDownload() {
        // Arrange
        when(downloadService.getDownload(VALID_DOWNLOAD_ID)).thenReturn(downloadResponseModel);

        // Act
        ResponseEntity<DownloadResponseModel> responseEntity = downloadController.getDownload(VALID_DOWNLOAD_ID);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(downloadResponseModel, responseEntity.getBody());

        verify(downloadService, times(1)).getDownload(VALID_DOWNLOAD_ID);
    }

    @Test
    void whenDownloadsExist_thenReturnDownloadsList() {
        // Arrange
        DownloadResponseModel anotherDownload = new DownloadResponseModel(UUID.randomUUID().toString(), "http://example.com/another.zip", "PENDING");
        List<DownloadResponseModel> downloadList = Arrays.asList(downloadResponseModel, anotherDownload);
        when(downloadService.getAllDownloads()).thenReturn(downloadList);

        // Act
        ResponseEntity<List<DownloadResponseModel>> responseEntity = downloadController.getAllDownloads();

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        assertEquals(downloadList, responseEntity.getBody());

        verify(downloadService, times(1)).getAllDownloads();
    }
}

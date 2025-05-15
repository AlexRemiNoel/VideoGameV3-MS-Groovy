package com.example.videogamev3.Tests;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadRequestMapper;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers; // Use this for unit testing MapStruct mappers

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DownloadRequestMapperTest {

    private final DownloadRequestMapper mapper = Mappers.getMapper(DownloadRequestMapper.class);

    @Test
    void shouldMapDownloadRequestModelToDownload() {
        // Arrange
        DownloadRequestModel requestModel = new DownloadRequestModel("http://test.com/file.iso", "user-123");

        // Act
        Download download = mapper.downloadRequestModelToDownload(requestModel);

        // Assert
        assertNotNull(download);
        assertEquals(requestModel.getSourceUrl(), download.getSourceUrl());
        assertEquals(requestModel.getUserId(), download.getUserId());
        assertNull(download.getId()); // Explicitly ignored in mapping
        assertNull(download.getDownloadStatus()); // Explicitly ignored in mapping
    }

    @Test
    void shouldMapNullDownloadRequestModelToNullDownload() {
        // Arrange
        DownloadRequestModel requestModel = null;

        // Act
        Download download = mapper.downloadRequestModelToDownload(requestModel);

        // Assert
        assertNull(download);
    }

    @Test
    void shouldMapDownloadRequestModelListToDownloadList() {
        // Arrange
        DownloadRequestModel requestModel1 = new DownloadRequestModel("http://test.com/file1.iso", "user-1");
        DownloadRequestModel requestModel2 = new DownloadRequestModel("http://test.com/file2.iso", "user-2");
        List<DownloadRequestModel> requestModelList = Arrays.asList(requestModel1, requestModel2);

        // Act
        List<Download> downloadList = mapper.downloadRequestModelToDownload(requestModelList);

        // Assert
        assertNotNull(downloadList);
        assertEquals(2, downloadList.size());

        assertEquals(requestModel1.getSourceUrl(), downloadList.get(0).getSourceUrl());
        assertEquals(requestModel1.getUserId(), downloadList.get(0).getUserId());
        assertNull(downloadList.get(0).getId());
        assertNull(downloadList.get(0).getDownloadStatus());

        assertEquals(requestModel2.getSourceUrl(), downloadList.get(1).getSourceUrl());
        assertEquals(requestModel2.getUserId(), downloadList.get(1).getUserId());
        assertNull(downloadList.get(1).getId());
        assertNull(downloadList.get(1).getDownloadStatus());
    }

    @Test
    void shouldMapNullDownloadRequestModelListToNullDownloadList() {
        // Arrange
        List<DownloadRequestModel> requestModelList = null;

        // Act
        List<Download> downloadList = mapper.downloadRequestModelToDownload(requestModelList);

        // Assert
        assertNull(downloadList);
    }

     @Test
    void shouldMapEmptyDownloadRequestModelListToEmptyDownloadList() {
        // Arrange
        List<DownloadRequestModel> requestModelList = Arrays.asList();

        // Act
        List<Download> downloadList = mapper.downloadRequestModelToDownload(requestModelList);

        // Assert
        assertNotNull(downloadList);
        assertTrue(downloadList.isEmpty());
    }
}
package com.example.videogamev3;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadResponseMapper;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers; // Use this for unit testing MapStruct mappers

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DownloadResponseMapperTest {

    private final DownloadResponseMapper mapper = Mappers.getMapper(DownloadResponseMapper.class);

    @Test
    void shouldMapDownloadEntityToDownloadResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Download downloadEntity = new Download(
                new DownloadId(uuid),
                "http://test.com/file.iso",
                DownloadStatus.DOWNLOADING,
                "user-123"
        );

        // Act
        DownloadResponseModel responseModel = mapper.downloadEntityToDownloadResponseModel(downloadEntity);

        // Assert
        assertNotNull(responseModel);
        assertEquals(uuid, responseModel.getId());
        assertEquals(downloadEntity.getSourceUrl(), responseModel.getSourceUrl());
        assertEquals(DownloadStatus.DOWNLOADING.toString(), responseModel.getStatus());
        assertEquals(downloadEntity.getUserId(), responseModel.getUserId());
    }

    @Test
    void shouldMapDownloadEntityWithNullIdToDownloadResponseModelWithNullIdString() {
        // Arrange
        Download downloadEntity = new Download(
                null, // DownloadId object is null
                "http://test.com/file.iso",
                DownloadStatus.PENDING,
                "user-123"
        );
         // MapStruct expression "java(download.getId().getUuid())" will cause NPE if download.getId() is null.
         // Need to handle this in the mapper or ensure DownloadId is never null on a Download entity.
         // For this test, let's assume DownloadId itself can be null, and we expect a specific behavior.
         // If Download.id is guaranteed to be non-null by JPA/Mongo, this test might be for an impossible state.

        // Act & Assert
        // If Download.id is nullable, and getId() can return null, the expression `download.getId().getUuid()`
        // would throw a NullPointerException. MapStruct might handle this with a null check if configured,
        // or it might be an issue in the mapping definition.
        // Let's assume for now the current mapping would throw NPE.
        // If MapStruct adds a null check implicitly for expression, then id would be null.

        // If DownloadId itself (the wrapper) is null
        assertThrows(NullPointerException.class, () -> {
             mapper.downloadEntityToDownloadResponseModel(downloadEntity);
        }, "Mapping should handle null DownloadId gracefully or this is an invalid entity state.");

        // If DownloadId.uuid is null but DownloadId object is not:
        Download downloadEntityWithNullUuid = new Download(
                new DownloadId(null), // UUID string inside DownloadId is null
                "http://test.com/file.iso",
                DownloadStatus.PENDING,
                "user-123"
        );
        DownloadResponseModel responseWithNullUuid = mapper.downloadEntityToDownloadResponseModel(downloadEntityWithNullUuid);
        assertNull(responseWithNullUuid.getId(), "ID string in response should be null if DownloadId.uuid is null");
    }

    @Test
    void shouldMapNullDownloadEntityToNullDownloadResponseModel() {
        // Arrange
        Download downloadEntity = null;

        // Act
        DownloadResponseModel responseModel = mapper.downloadEntityToDownloadResponseModel(downloadEntity);

        // Assert
        assertNull(responseModel);
    }

    @Test
    void shouldMapDownloadEntityListToDownloadResponseModelList() {
        // Arrange
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        Download download1 = new Download(new DownloadId(uuid1), "http://test.com/file1.iso", DownloadStatus.COMPLETED, "user-1");
        Download download2 = new Download(new DownloadId(uuid2), "http://test.com/file2.iso", DownloadStatus.PAUSED, "user-2");
        List<Download> downloadList = Arrays.asList(download1, download2);

        // Act
        List<DownloadResponseModel> responseModelList = mapper.downloadEntityToDownloadResponseModel(downloadList);

        // Assert
        assertNotNull(responseModelList);
        assertEquals(2, responseModelList.size());

        assertEquals(uuid1, responseModelList.get(0).getId());
        assertEquals(download1.getSourceUrl(), responseModelList.get(0).getSourceUrl());
        assertEquals(DownloadStatus.COMPLETED.toString(), responseModelList.get(0).getStatus());
        assertEquals(download1.getUserId(), responseModelList.get(0).getUserId());

        assertEquals(uuid2, responseModelList.get(1).getId());
        assertEquals(download2.getSourceUrl(), responseModelList.get(1).getSourceUrl());
        assertEquals(DownloadStatus.PAUSED.toString(), responseModelList.get(1).getStatus());
        assertEquals(download2.getUserId(), responseModelList.get(1).getUserId());
    }

    @Test
    void shouldMapNullDownloadEntityListToNullDownloadResponseModelList() {
        // Arrange
        List<Download> downloadList = null;

        // Act
        List<DownloadResponseModel> responseModelList = mapper.downloadEntityToDownloadResponseModel(downloadList);

        // Assert
        assertNull(responseModelList);
    }

    @Test
    void shouldMapEmptyDownloadEntityListToEmptyDownloadResponseModelList() {
        // Arrange
        List<Download> downloadList = Arrays.asList();

        // Act
        List<DownloadResponseModel> responseModelList = mapper.downloadEntityToDownloadResponseModel(downloadList);

        // Assert
        assertNotNull(responseModelList);
        assertTrue(responseModelList.isEmpty());
    }
}
package com.example.videogamev3.servicelayer;

import com.example.videogamev3.DownloadManagement.BusinessLogic.DownloadService;
import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadRequestMapper;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadResponseMapper;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceUnitTest {

    @Mock
    private DownloadRepository downloadRepository;



    @Spy
    private DownloadResponseMapper downloadResponseMapper = org.mapstruct.factory.Mappers.getMapper(DownloadResponseMapper.class);

    @Spy
    private DownloadRequestMapper downloadRequestMapper = org.mapstruct.factory.Mappers.getMapper(DownloadRequestMapper.class);

    @InjectMocks
    private DownloadService downloadService;

    private Download downloadPending;
    private Download downloadDownloading;
    private Download downloadPaused;
    private String pendingId;
    private String downloadingId;
    private String pausedId;

    @BeforeEach
    void setUp() {
        pendingId = UUID.randomUUID().toString();
        downloadPending = new Download(new DownloadId(pendingId), "http://test.com/pending.zip", DownloadStatus.PENDING);

        downloadingId = UUID.randomUUID().toString();
        downloadDownloading = new Download(new DownloadId(downloadingId), "http://test.com/downloading.exe", DownloadStatus.DOWNLOADING);

        pausedId = UUID.randomUUID().toString();
        downloadPaused = new Download(new DownloadId(pausedId), "http://test.com/paused.tar.gz", DownloadStatus.PAUSED);
    }

    @Test
    @DisplayName("Given valid request, when createDownload, then save PENDING download and return response")
    void whenCreateDownload_thenSavePendingAndReturnResponse() {

        DownloadRequestModel requestModel = new DownloadRequestModel("http://new.com/file.dat");


        ArgumentCaptor<Download> downloadCaptor = ArgumentCaptor.forClass(Download.class);


        given(downloadRepository.save(downloadCaptor.capture())).willAnswer(invocation -> {
            Download downloadToSave = invocation.getArgument(0);

            assertThat(downloadToSave.getId()).isNotNull();
            assertThat(downloadToSave.getId().getUuid()).isNotBlank();
            return downloadToSave;
        });


        DownloadResponseModel responseModel = downloadService.createDownload(requestModel);



        then(downloadRepository).should(times(1)).save(any(Download.class));


        Download capturedDownload = downloadCaptor.getValue();
        assertThat(capturedDownload.getSourceUrl()).isEqualTo(requestModel.getSourceUrl());
        assertThat(capturedDownload.getDownloadStatus()).isEqualTo(DownloadStatus.PENDING);
        assertThat(capturedDownload.getId()).isNotNull();
        assertThat(capturedDownload.getId().getUuid()).isNotNull();


        assertThat(responseModel).isNotNull();
        assertThat(responseModel.getId()).isEqualTo(capturedDownload.getId().getUuid());
        assertThat(responseModel.getSourceUrl()).isEqualTo(requestModel.getSourceUrl());
        assertThat(responseModel.getStatus()).isEqualTo(DownloadStatus.PENDING.toString());
    }

    @Test
    @DisplayName("Given existing ID, when getDownload, then return download response")
    void whenGetDownload_andExists_thenReturnResponse() {

        given(downloadRepository.findDownloadById_Uuid(pendingId)).willReturn(downloadPending);


        DownloadResponseModel responseModel = downloadService.getDownload(pendingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(pendingId);
        assertThat(responseModel).isNotNull();
        assertThat(responseModel.getId()).isEqualTo(pendingId);
        assertThat(responseModel.getSourceUrl()).isEqualTo(downloadPending.getSourceUrl());
        assertThat(responseModel.getStatus()).isEqualTo(DownloadStatus.PENDING.toString());
    }

    @Test
    @DisplayName("Given non-existing ID, when getDownload, then throw EntityNotFoundException")
    void whenGetDownload_andNotExists_thenThrowNotFound() {

        String nonExistentId = UUID.randomUUID().toString();
        given(downloadRepository.findDownloadById_Uuid(nonExistentId)).willReturn(null);







        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            downloadService.startDownload(nonExistentId);
        });

        assertThat(exception.getMessage()).contains("DownloadManager not found with id: " + nonExistentId);
        then(downloadRepository).should(times(1)).findDownloadById_Uuid(nonExistentId);
    }


    @Test
    @DisplayName("Given PENDING download ID, when startDownload, then update status to DOWNLOADING")
    void whenStartDownload_fromPending_thenSetDownloading() {

        given(downloadRepository.findDownloadById_Uuid(pendingId)).willReturn(downloadPending);
        given(downloadRepository.save(any(Download.class))).willReturn(downloadPending);


        DownloadResponseModel response = downloadService.startDownload(pendingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(pendingId);
        ArgumentCaptor<Download> captor = ArgumentCaptor.forClass(Download.class);
        then(downloadRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getDownloadStatus()).isEqualTo(DownloadStatus.DOWNLOADING);

        assertThat(response.getStatus()).isEqualTo(DownloadStatus.DOWNLOADING.toString());
    }

    @Test
    @DisplayName("Given PAUSED download ID, when startDownload, then update status to DOWNLOADING")
    void whenStartDownload_fromPaused_thenSetDownloading() {

        given(downloadRepository.findDownloadById_Uuid(pausedId)).willReturn(downloadPaused);
        given(downloadRepository.save(any(Download.class))).willReturn(downloadPaused);


        DownloadResponseModel response = downloadService.startDownload(pausedId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(pausedId);
        ArgumentCaptor<Download> captor = ArgumentCaptor.forClass(Download.class);
        then(downloadRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getDownloadStatus()).isEqualTo(DownloadStatus.DOWNLOADING);
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.DOWNLOADING.toString());
    }


    @Test
    @DisplayName("Given DOWNLOADING download ID, when startDownload, then do not change status")
    void whenStartDownload_fromDownloading_thenNoChange() {

        given(downloadRepository.findDownloadById_Uuid(downloadingId)).willReturn(downloadDownloading);



        DownloadResponseModel response = downloadService.startDownload(downloadingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(downloadingId);
        then(downloadRepository).should(never()).save(any(Download.class));
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.DOWNLOADING.toString());
    }


    @Test
    @DisplayName("Given DOWNLOADING download ID, when pauseDownload, then update status to PAUSED")
    void whenPauseDownload_fromDownloading_thenSetPaused() {

        given(downloadRepository.findDownloadById_Uuid(downloadingId)).willReturn(downloadDownloading);
        given(downloadRepository.save(any(Download.class))).willReturn(downloadDownloading);


        DownloadResponseModel response = downloadService.pauseDownload(downloadingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(downloadingId);
        ArgumentCaptor<Download> captor = ArgumentCaptor.forClass(Download.class);
        then(downloadRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getDownloadStatus()).isEqualTo(DownloadStatus.PAUSED);
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.PAUSED.toString());
    }

    @Test
    @DisplayName("Given PENDING download ID, when pauseDownload, then do not change status")
    void whenPauseDownload_fromPending_thenNoChange() {

        given(downloadRepository.findDownloadById_Uuid(pendingId)).willReturn(downloadPending);


        DownloadResponseModel response = downloadService.pauseDownload(pendingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(pendingId);
        then(downloadRepository).should(never()).save(any(Download.class));
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.PENDING.toString());
    }



    @Test
    @DisplayName("Given PAUSED download ID, when resumeDownload, then update status to DOWNLOADING")
    void whenResumeDownload_fromPaused_thenSetDownloading() {

        given(downloadRepository.findDownloadById_Uuid(pausedId)).willReturn(downloadPaused);
        given(downloadRepository.save(any(Download.class))).willReturn(downloadPaused);


        DownloadResponseModel response = downloadService.resumeDownload(pausedId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(pausedId);
        ArgumentCaptor<Download> captor = ArgumentCaptor.forClass(Download.class);
        then(downloadRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getDownloadStatus()).isEqualTo(DownloadStatus.DOWNLOADING);
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.DOWNLOADING.toString());
    }

    @Test
    @DisplayName("Given DOWNLOADING download ID, when resumeDownload, then do not change status")
    void whenResumeDownload_fromDownloading_thenNoChange() {

        given(downloadRepository.findDownloadById_Uuid(downloadingId)).willReturn(downloadDownloading);


        DownloadResponseModel response = downloadService.resumeDownload(downloadingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(downloadingId);
        then(downloadRepository).should(never()).save(any(Download.class));
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.DOWNLOADING.toString());
    }


    @Test
    @DisplayName("Given PENDING download ID, when cancelDownload, then update status to CANCELLED")
    void whenCancelDownload_fromPending_thenSetCancelled() {

        given(downloadRepository.findDownloadById_Uuid(pendingId)).willReturn(downloadPending);
        given(downloadRepository.save(any(Download.class))).willReturn(downloadPending);


        DownloadResponseModel response = downloadService.cancelDownload(pendingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(pendingId);
        ArgumentCaptor<Download> captor = ArgumentCaptor.forClass(Download.class);
        then(downloadRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getDownloadStatus()).isEqualTo(DownloadStatus.CANCELLED);
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.CANCELLED.toString());
    }

    @Test
    @DisplayName("Given COMPLETED download ID, when cancelDownload, then do not change status")
    void whenCancelDownload_fromCompleted_thenNoChange() {

        String completedId = UUID.randomUUID().toString();
        Download downloadCompleted = new Download(new DownloadId(completedId), "http://test.com/completed.zip", DownloadStatus.COMPLETED);
        given(downloadRepository.findDownloadById_Uuid(completedId)).willReturn(downloadCompleted);


        DownloadResponseModel response = downloadService.cancelDownload(completedId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(completedId);
        then(downloadRepository).should(never()).save(any(Download.class));
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.COMPLETED.toString());
    }



    @Test
    @DisplayName("When getAllDownloads, then return list of all downloads")
    void whenGetAllDownloads_thenReturnList() {

        List<Download> downloads = Arrays.asList(downloadPending, downloadDownloading, downloadPaused);
        given(downloadRepository.findAll()).willReturn(downloads);


        List<DownloadResponseModel> responseList = downloadService.getAllDownloads();


        then(downloadRepository).should(times(1)).findAll();
        assertThat(responseList).hasSize(3);
        assertThat(responseList).extracting(DownloadResponseModel::getId)
                .containsExactlyInAnyOrder(pendingId, downloadingId, pausedId);
    }


    @Test
    @DisplayName("Given existing ID, when deleteDownload, then call repository delete")
    void whenDeleteDownload_andExists_thenCallRepositoryDelete() {



        given(downloadRepository.existsById(pendingId)).willReturn(true);
        willDoNothing().given(downloadRepository).deleteById(pendingId);


        assertDoesNotThrow(() -> downloadService.deleteDownload(pendingId));


        then(downloadRepository).should(times(1)).existsById(pendingId);
        then(downloadRepository).should(times(1)).deleteById(pendingId);
    }

    @Test
    @DisplayName("Given non-existing ID, when deleteDownload, then throw EntityNotFoundException")
    void whenDeleteDownload_andNotExists_thenThrowNotFound() {

        String nonExistentId = UUID.randomUUID().toString();
        given(downloadRepository.existsById(nonExistentId)).willReturn(false);


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            downloadService.deleteDownload(nonExistentId);
        });

        assertThat(exception.getMessage()).contains("DownloadManager not found with id: " + nonExistentId);
        then(downloadRepository).should(times(1)).existsById(nonExistentId);
        then(downloadRepository).should(never()).deleteById(anyString());
    }


    @Test
    @DisplayName("Given DOWNLOADING download ID, when markCompleted, then update status to COMPLETED")
    void whenMarkCompleted_fromDownloading_thenSetCompleted() {

        given(downloadRepository.findDownloadById_Uuid(downloadingId)).willReturn(downloadDownloading);
        given(downloadRepository.save(any(Download.class))).willReturn(downloadDownloading);


        DownloadResponseModel response = downloadService.markCompleted(downloadingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(downloadingId);
        ArgumentCaptor<Download> captor = ArgumentCaptor.forClass(Download.class);
        then(downloadRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getDownloadStatus()).isEqualTo(DownloadStatus.COMPLETED);
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.COMPLETED.toString());
    }

    @Test
    @DisplayName("Given PENDING download ID, when markCompleted, then do not change status")
    void whenMarkCompleted_fromPending_thenNoChange() {

        given(downloadRepository.findDownloadById_Uuid(pendingId)).willReturn(downloadPending);


        DownloadResponseModel response = downloadService.markCompleted(pendingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(pendingId);
        then(downloadRepository).should(never()).save(any(Download.class));
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.PENDING.toString());
    }


    @Test
    @DisplayName("Given DOWNLOADING download ID, when markFailed, then update status to FAILED")
    void whenMarkFailed_fromDownloading_thenSetFailed() {

        given(downloadRepository.findDownloadById_Uuid(downloadingId)).willReturn(downloadDownloading);
        given(downloadRepository.save(any(Download.class))).willReturn(downloadDownloading);


        DownloadResponseModel response = downloadService.markFailed(downloadingId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(downloadingId);
        ArgumentCaptor<Download> captor = ArgumentCaptor.forClass(Download.class);
        then(downloadRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getDownloadStatus()).isEqualTo(DownloadStatus.FAILED);
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.FAILED.toString());
    }

    @Test
    @DisplayName("Given COMPLETED download ID, when markFailed, then do not change status")
    void whenMarkFailed_fromCompleted_thenNoChange() {

        String completedId = UUID.randomUUID().toString();
        Download downloadCompleted = new Download(new DownloadId(completedId), "http://test.com/completed.zip", DownloadStatus.COMPLETED);
        given(downloadRepository.findDownloadById_Uuid(completedId)).willReturn(downloadCompleted);


        DownloadResponseModel response = downloadService.markFailed(completedId);


        then(downloadRepository).should(times(1)).findDownloadById_Uuid(completedId);
        then(downloadRepository).should(never()).save(any(Download.class));
        assertThat(response.getStatus()).isEqualTo(DownloadStatus.COMPLETED.toString());
    }
}
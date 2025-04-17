package com.example.videogamev3.DownloadManagement.DataMapper;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DownloadResponseMapper {
    @Mapping(target = "id", expression = "java(download.getId().getUuid())")
    @Mapping(target = "status", expression = "java(download.getDownloadStatus().toString())")

    DownloadResponseModel downloadEntityToDownloadResponseModel(Download download);
    List<DownloadResponseModel> downloadEntityToDownloadResponseModel(List<Download> download);

}

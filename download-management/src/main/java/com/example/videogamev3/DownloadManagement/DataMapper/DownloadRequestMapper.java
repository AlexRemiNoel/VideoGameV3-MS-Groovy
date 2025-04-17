package com.example.videogamev3.DownloadManagement.DataMapper;


import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DownloadRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "downloadStatus", ignore = true)

    Download downloadRequestModelToDownload(DownloadRequestModel downloadRequestModel);
    List<Download> downloadRequestModelToDownload(List<DownloadRequestModel> downloadRequestModelList);
}

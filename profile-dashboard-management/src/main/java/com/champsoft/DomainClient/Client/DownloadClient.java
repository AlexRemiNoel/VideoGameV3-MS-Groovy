package com.champsoft.DomainClient.Client;

import com.champsoft.DomainClient.Dtos.DownloadClientResponseModel;
import com.champsoft.Exceptions.DownstreamServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class DownloadClient {

    private final RestTemplate restTemplate;
    private final String downloadServiceUrl;

    public DownloadClient(RestTemplate restTemplate,
                          @Value("${microservices.download.url}") String downloadServiceUrl) {
        this.restTemplate = restTemplate;
        this.downloadServiceUrl = downloadServiceUrl; // e.g., http://localhost:8083/api/v1/downloads
    }

    public List<DownloadClientResponseModel> getDownloadsByUserId(String userId) {
        String url = downloadServiceUrl + "/user/" + userId;
        log.info("Fetching downloads for userId: {} from URL: {}", userId, url);



        try {
            ResponseEntity<List<DownloadClientResponseModel>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<DownloadClientResponseModel>>() {});

            List<DownloadClientResponseModel> downloads = response.getBody();
            if (downloads == null) {
                 log.warn("Received null body for downloads for userId: {}", userId);
                 return Collections.emptyList();
            }
            log.info("Successfully fetched {} downloads for userId: {}", downloads.size(), userId);
            return downloads;

        } catch (ResourceAccessException ex) {
            log.error("Error accessing download service for userId {}: {}", userId, ex.getMessage(), ex);
            throw new DownstreamServiceUnavailableException("Download service is unavailable or network issue.", ex);
        } catch (Exception ex) {

            log.error("Unexpected error while fetching downloads for userId {}: {}", userId, ex.getMessage(), ex);
            throw new DownstreamServiceUnavailableException("Unexpected error communicating with download service.", ex);
        }
    }
}
package com.example.apigatewayservice.DomainClientLayer.dashboard;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import com.example.apigatewayservice.presentationlayer.dashboard.UserProfileDashboardResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileDashboardServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper; // Renamed from 'mapper' to 'objectMapper' for clarity

    @InjectMocks
    private ProfileDashboardServiceClient client;

    private final String BASE_URL = "http://dashboard-management:8080/api/v1/profile-dashboards";
    private final String USER_ID = "user123";
    private UserProfileDashboardResponseModel sampleDashboardDTO;

    @BeforeEach
    void setUp() {
        // Assuming UserProfileDashboardResponseDTO_GW has a builder or appropriate constructor
        sampleDashboardDTO = UserProfileDashboardResponseModel.builder()
                .userId(USER_ID).username("test").email("test@example.com").balance(10.0)
                .games(Collections.emptyList()).downloads(Collections.emptyList()).build();
         client = new ProfileDashboardServiceClient(restTemplate, objectMapper); // Re-initialize with mocks
    }

    // --- Success Cases ---
    @Test
    void getProfileDashboardByUserId_success() {
        String url = BASE_URL + "/" + USER_ID;
        when(restTemplate.getForObject(eq(url), eq(UserProfileDashboardResponseModel.class))).thenReturn(sampleDashboardDTO);
        UserProfileDashboardResponseModel result = client.getProfileDashboardByUserId(USER_ID);
        assertEquals(sampleDashboardDTO, result);
        verify(restTemplate).getForObject(eq(url), eq(UserProfileDashboardResponseModel.class));
    }

    @Test
    void getAllProfileDashboards_success() {
        ResponseEntity<List<UserProfileDashboardResponseModel>> responseEntity =
                new ResponseEntity<>(List.of(sampleDashboardDTO), HttpStatus.OK);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        List<UserProfileDashboardResponseModel> result = client.getAllProfileDashboards();
        assertFalse(result.isEmpty());
        assertEquals(sampleDashboardDTO, result.get(0));
        verify(restTemplate).exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void createOrRefreshDashboard_success() {
        String url = BASE_URL + "/" + USER_ID;
        when(restTemplate.postForObject(eq(url), isNull(), eq(UserProfileDashboardResponseModel.class))).thenReturn(sampleDashboardDTO);
        UserProfileDashboardResponseModel result = client.createOrRefreshDashboard(USER_ID);
        assertEquals(sampleDashboardDTO, result);
        verify(restTemplate).postForObject(eq(url), isNull(), eq(UserProfileDashboardResponseModel.class));
    }

    @Test
    void updateProfileDashboard_success() {
        String url = BASE_URL + "/" + USER_ID;
        ResponseEntity<UserProfileDashboardResponseModel> responseEntity = new ResponseEntity<>(sampleDashboardDTO, HttpStatus.OK);
        when(restTemplate.exchange(any(RequestEntity.class), eq(UserProfileDashboardResponseModel.class))).thenReturn(responseEntity);

        UserProfileDashboardResponseModel result = client.updateProfileDashboard(USER_ID);
        assertEquals(sampleDashboardDTO, result);
        verify(restTemplate).exchange(any(RequestEntity.class), eq(UserProfileDashboardResponseModel.class));
    }

    @Test
    void deleteProfileDashboard_success() {
        String url = BASE_URL + "/" + USER_ID;
        doNothing().when(restTemplate).delete(eq(url));
        client.deleteProfileDashboard(USER_ID);
        verify(restTemplate).delete(eq(url));
    }

    // --- Exception Handling Cases ---
    private HttpClientErrorException mockHttpClientErrorException(HttpStatus status, String responseBody) {
        byte[] bodyBytes = (responseBody != null) ? responseBody.getBytes(StandardCharsets.UTF_8) : null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Ensure headers are present
        return new HttpClientErrorException(status, status.getReasonPhrase(), headers, bodyBytes, StandardCharsets.UTF_8);
    }

    @Test
    void getProfileDashboardByUserId_notFoundException() throws JsonProcessingException {
        String url = BASE_URL + "/" + USER_ID;
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", "Not found message");
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(errorInfo);
        when(restTemplate.getForObject(eq(url), eq(UserProfileDashboardResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.NOT_FOUND, "{\"message\": \"Not found message\"}"));

        assertThrows(NotFoundException.class, () -> client.getProfileDashboardByUserId(USER_ID));
        verify(objectMapper).readValue(anyString(), eq(HttpErrorInfo.class));
    }

    @Test
    void getProfileDashboardByUserId_invalidInputException_unprocessable() throws JsonProcessingException {
        String url = BASE_URL + "/" + USER_ID;
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/path", "Invalid input");
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(errorInfo);
        when(restTemplate.getForObject(eq(url), eq(UserProfileDashboardResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, "{\"message\": \"Invalid input\"}"));

        assertThrows(InvalidInputException.class, () -> client.getProfileDashboardByUserId(USER_ID));
        verify(objectMapper).readValue(anyString(), eq(HttpErrorInfo.class));
    }
    
    @Test
    void getProfileDashboardByUserId_invalidInputException_badRequest() throws JsonProcessingException {
        String url = BASE_URL + "/" + USER_ID;
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, "/path", "Bad request");
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(errorInfo);
        when(restTemplate.getForObject(eq(url), eq(UserProfileDashboardResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.BAD_REQUEST, "{\"message\": \"Bad request\"}"));

        assertThrows(InvalidInputException.class, () -> client.getProfileDashboardByUserId(USER_ID));
        verify(objectMapper).readValue(anyString(), eq(HttpErrorInfo.class));
    }

    @Test
    void getProfileDashboardByUserId_genericRuntimeException() {
        String url = BASE_URL + "/" + USER_ID;
        when(restTemplate.getForObject(eq(url), eq(UserProfileDashboardResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));
        // No need to mock ObjectMapper for this path if response body isn't JSON or parsing isn't expected to succeed

        assertThrows(RuntimeException.class, () -> client.getProfileDashboardByUserId(USER_ID));
    }

    // --- getErrorMessage specific tests ---
    @Test
    void getErrorMessage_emptyBody_returnsStatusText() throws JsonProcessingException {
        // Test PATH 1: Empty body
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, ""); // Empty body
        // Ensure objectMapper is NOT called for empty body if the primary check catches it.
        // However, the current implementation would still try to parse "" if not caught by the first if.
        // The current implementation of getErrorMessage will use ex.getStatusText() if body is empty.

        when(restTemplate.getForObject(anyString(), any(Class.class))).thenThrow(ex);

        try {
            client.getProfileDashboardByUserId(USER_ID);
            fail("Expected NotFoundException");
        } catch (NotFoundException e) {
            // If response body is empty, ProfileDashboardServiceClient.getErrorMessage should return ex.getStatusText()
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        // objectMapper.readValue should not have been called if the first "if" in getErrorMessage is met.
        verify(objectMapper, never()).readValue(anyString(), any(Class.class)); 
    }

    @Test
    void getErrorMessage_nullBody_returnsStatusText() throws JsonProcessingException {
        // Test PATH 1: Null body
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.BAD_REQUEST, null); // Null body

        when(restTemplate.getForObject(anyString(), any(Class.class))).thenThrow(ex);

        try {
            client.getProfileDashboardByUserId(USER_ID); // This will trigger handleHttpClientException
            fail("Expected InvalidInputException for BAD_REQUEST");
        } catch (InvalidInputException e) {
            // If response body is null, ProfileDashboardServiceClient.getErrorMessage should return ex.getStatusText()
            assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage());
        }
        // objectMapper.readValue should not have been called.
        verify(objectMapper, never()).readValue(anyString(), any(Class.class));
    }

    @Test
    void getErrorMessage_ioExceptionOnParse_returnsRawBody() throws JsonProcessingException { // Renamed for clarity
        // Test PATH 3: IOException during parsing
        String rawBody = "invalid json";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, rawBody);
        when(objectMapper.readValue(eq(rawBody), eq(HttpErrorInfo.class)))
                .thenThrow(new JsonProcessingException("Parse error") {}); // Simulate IOException
        when(restTemplate.getForObject(anyString(), any(Class.class))).thenThrow(ex);

        try {
            client.getProfileDashboardByUserId(USER_ID);
            fail("Expected NotFoundException");
        } catch (NotFoundException e) {
            assertEquals(rawBody, e.getMessage()); // Should return raw body as per implementation
        }
        verify(objectMapper).readValue(rawBody, HttpErrorInfo.class);
    }
} 
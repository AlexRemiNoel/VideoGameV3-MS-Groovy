package com.example.apigatewayservice.DomainClientLayer.user;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import com.example.apigatewayservice.presentationlayer.user.UserRequestModel;
import com.example.apigatewayservice.presentationlayer.user.UserResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private UserServiceClient userServiceClient;

    private final String USER_ID = "user-uuid-123";
    private final String USER_SERVICE_HOST = "user-management";
    private final String USER_SERVICE_PORT = "8080";
    private String BASE_URL;

    private UserResponseModel sampleUserResponse;
    private UserRequestModel sampleUserRequest;

    @BeforeEach
    void setUp() {
        userServiceClient = new UserServiceClient(restTemplate, objectMapper, USER_SERVICE_HOST, USER_SERVICE_PORT);
        BASE_URL = "http://" + USER_SERVICE_HOST + ":" + USER_SERVICE_PORT + "/api/v1/user";

        sampleUserResponse = UserResponseModel.builder()
                .userId(USER_ID).username("testUser").email("test@example.com")
                .balance(100.0).orders(Collections.emptyList()).games(Collections.emptyList()).build();
        sampleUserRequest = UserRequestModel.builder()
                .username("testUser").email("test@example.com").password("password").balance(100.0).build();
    }


    private HttpClientErrorException mockHttpClientErrorException(HttpStatus status, String responseBody, String exceptionMessage) {
        String msg = (exceptionMessage != null && !exceptionMessage.isBlank()) ? exceptionMessage : status.getReasonPhrase();
        byte[] bodyBytes = (responseBody != null) ? responseBody.getBytes(StandardCharsets.UTF_8) : null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpClientErrorException(status, msg, headers, bodyBytes, StandardCharsets.UTF_8);
    }

    private HttpClientErrorException mockHttpClientErrorException(HttpStatus status, String responseBody) {
        return mockHttpClientErrorException(status, responseBody, status.getReasonPhrase());
    }

    // --- Success Cases ---
    @Test
    void findUserByUuidOrThrow_success() {
        String url = BASE_URL + "/" + USER_ID;
        when(restTemplate.getForObject(eq(url), eq(UserResponseModel.class))).thenReturn(sampleUserResponse);
        UserResponseModel result = userServiceClient.findUserByUuidOrThrow(USER_ID);
        assertEquals(sampleUserResponse, result);
        verify(restTemplate).getForObject(eq(url), eq(UserResponseModel.class));
    }

    @Test
    void getAllUsers_success() {
        ResponseEntity<List<UserResponseModel>> responseEntity = new ResponseEntity<>(List.of(sampleUserResponse), HttpStatus.OK);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);
        List<UserResponseModel> result = userServiceClient.getAllUsers();
        assertFalse(result.isEmpty());
        assertEquals(sampleUserResponse, result.get(0));
    }

    @Test
    void getUserById_success() {
        String url = BASE_URL + "/" + USER_ID;
        when(restTemplate.getForObject(eq(url), eq(UserResponseModel.class))).thenReturn(sampleUserResponse);
        UserResponseModel result = userServiceClient.getUserById(USER_ID);
        assertEquals(sampleUserResponse, result);
    }

    @Test
    void addUser_success() {
        when(restTemplate.postForObject(eq(BASE_URL), eq(sampleUserRequest), eq(UserResponseModel.class))).thenReturn(sampleUserResponse);
        UserResponseModel result = userServiceClient.addUser(sampleUserRequest);
        assertEquals(sampleUserResponse, result);
    }

    @Test
    void updateUser_success() {
        String url = BASE_URL + "/" + USER_ID;
        doNothing().when(restTemplate).put(eq(url), eq(sampleUserRequest));
        // updateUser returns null on success in the current implementation, so we just verify the call
        assertNull(userServiceClient.updateUser(sampleUserRequest, USER_ID));
        verify(restTemplate).put(eq(url), eq(sampleUserRequest));
    }

    @Test
    void deleteUser_success() {
        String url = BASE_URL + "/" + USER_ID;
        doNothing().when(restTemplate).delete(eq(url));
        userServiceClient.deleteUser(USER_ID);
        verify(restTemplate).delete(eq(url));
    }

    @Test
    void updateUserBalance_success() {
        String url = BASE_URL + "/uuid/" + USER_ID + "/balance/" + 150.0;
        ResponseEntity<UserResponseModel> responseEntity = new ResponseEntity<>(sampleUserResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), isNull(), eq(UserResponseModel.class))).thenReturn(responseEntity);
        UserResponseModel result = userServiceClient.updateUserBalance(USER_ID, 150.0);
        assertEquals(sampleUserResponse, result);
    }

    // --- Exception Cases ---
    @Test
    void findUserByUuidOrThrow_restTemplateThrowsNotFound() {
        String url = BASE_URL + "/" + USER_ID;
        // This test covers the specific catch block in findUserByUuidOrThrow
        when(restTemplate.getForObject(eq(url), eq(UserResponseModel.class))).thenThrow(new NotFoundException("Direct RestTemplate NF"));
        NotFoundException ex = assertThrows(NotFoundException.class, () -> userServiceClient.findUserByUuidOrThrow(USER_ID));
        assertTrue(ex.getMessage().contains("User not found: " + USER_ID));
    }

    @Test
    void getUserById_notFoundException() throws JsonProcessingException {
        String url = BASE_URL + "/" + USER_ID;
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", "NF message");
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(errorInfo);
        when(restTemplate.getForObject(eq(url), eq(UserResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.NOT_FOUND, "{\"message\": \"NF message\"}"));

        assertThrows(NotFoundException.class, () -> userServiceClient.getUserById(USER_ID));
        verify(objectMapper).readValue(anyString(), eq(HttpErrorInfo.class));
    }

    @Test
    void getUserById_invalidInputException() throws JsonProcessingException {
        String url = BASE_URL + "/" + USER_ID;
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/path", "Invalid input");
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(errorInfo);
        when(restTemplate.getForObject(eq(url), eq(UserResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, "{\"message\": \"Invalid input\"}"));

        assertThrows(InvalidInputException.class, () -> userServiceClient.getUserById(USER_ID));
    }

    @Test
    void getUserById_genericHttpClientErrorException() {
        String url = BASE_URL + "/" + USER_ID;
        HttpClientErrorException originalEx = mockHttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
        when(restTemplate.getForObject(eq(url), eq(UserResponseModel.class))).thenThrow(originalEx);

        HttpClientErrorException thrownEx = assertThrows(HttpClientErrorException.class, () -> userServiceClient.getUserById(USER_ID));
        assertEquals(originalEx, thrownEx); // Should rethrow the original exception
    }
    
    // --- getErrorMessage specific tests ---
    @Test
    void getErrorMessage_success() throws JsonProcessingException {
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, "{\"message\":\"Parsed Message\"}");
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", "Parsed Message");
        when(objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)).thenReturn(errorInfo);
        
        // Test indirectly via handleHttpClientException
        try {
            when(restTemplate.getForObject(anyString(), eq(UserResponseModel.class))).thenThrow(ex);
            userServiceClient.getUserById("some-id");
        } catch (NotFoundException e) {
            assertEquals("Parsed Message", e.getMessage());
        }
        verify(objectMapper).readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
    }

    @Test
    void getErrorMessage_emptyMessageInParsedHttpErrorInfo_fallsBackToExMessage() throws JsonProcessingException {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String responseBody = "{\"message\":\"\"}"; // Parsed message will be blank
        HttpClientErrorException ex = mockHttpClientErrorException(status, responseBody); // ex.getMessage() will be "404 Not Found"

        HttpErrorInfo errorInfoWithBlankMessage = new HttpErrorInfo(status, "/path", "");
        when(objectMapper.readValue(responseBody, HttpErrorInfo.class)).thenReturn(errorInfoWithBlankMessage);

        try {
            when(restTemplate.getForObject(anyString(), eq(UserResponseModel.class))).thenThrow(ex);
            userServiceClient.getUserById("some-id-for-empty-parsed-message");
            fail("NotFoundException expected");
        } catch (NotFoundException e) {
            // Client's getErrorMessage should fall back to ex.getMessage()
            String expectedClientMessage = status.value() + " " + status.getReasonPhrase();
            assertEquals(expectedClientMessage, e.getMessage());
        }
        verify(objectMapper).readValue(responseBody, HttpErrorInfo.class);
    }

    @Test
    void getErrorMessage_objectMapperThrowsIOException_fallsBackToExMessage() throws IOException {
        final String specificExMessage = "User underlying ex message for IOEx";
        HttpStatus status = HttpStatus.NOT_FOUND;
        HttpClientErrorException mockException = mockHttpClientErrorException(status, "user-invalid-json", specificExMessage);
        when(objectMapper.readValue("user-invalid-json", HttpErrorInfo.class)).thenThrow(new JsonProcessingException("User parse fail") {});

        String result = userServiceClient.getErrorMessage(mockException);
        assertEquals(status.value() + " " + specificExMessage, result);
    }

    @Test
    void getErrorMessage_objectMapperThrowsIOException_exMessageIsBlank_fallsBackToStatusCode() throws IOException {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        when(mockException.getResponseBodyAsString()).thenReturn("user-invalid-json-again");
        when(mockException.getMessage()).thenReturn(""); // Ensure ex.getMessage() is blank
        when(mockException.getStatusCode()).thenReturn(status);
        when(mockException.getStatusText()).thenReturn(status.getReasonPhrase());

        when(objectMapper.readValue("user-invalid-json-again", HttpErrorInfo.class)).thenThrow(new JsonProcessingException("User parse fail again") {});

        String result = userServiceClient.getErrorMessage(mockException);
        assertEquals("An error occurred: " + status.value() + " " + status.getReasonPhrase(), result);
    }

    @Test
    void getErrorMessage_emptyResponseBody_fallsBackToExMessage() {
        final String specificExMessage = "User exception message for empty body";
        HttpStatus status = HttpStatus.NOT_FOUND;
        HttpClientErrorException mockException = mockHttpClientErrorException(status, "", specificExMessage);

        String result = userServiceClient.getErrorMessage(mockException);
        assertEquals(status.value() + " " + specificExMessage, result);
    }

    @Test
    void getErrorMessage_nullResponseBody_exMessageIsBlank_fallsBackToStatusCode() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        when(mockException.getResponseBodyAsString()).thenReturn(null);
        when(mockException.getMessage()).thenReturn(null); // Ensure ex.getMessage() is null
        when(mockException.getStatusCode()).thenReturn(status);
        when(mockException.getStatusText()).thenReturn(status.getReasonPhrase());

        String result = userServiceClient.getErrorMessage(mockException);
        assertEquals("An error occurred: " + status.value() + " " + status.getReasonPhrase(), result);
    }

    @Test
    void getErrorMessage_objectMapperThrowsOtherException() throws IOException {
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, "some-user-body");
        when(objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)).thenThrow(new RuntimeException("User unexpected parse fail"));

        try {
            when(restTemplate.getForObject(anyString(), eq(UserResponseModel.class))).thenThrow(ex);
            userServiceClient.getUserById("some-user-id");
        } catch (NotFoundException e) {
            assertEquals(ex.getMessage(), e.getMessage());
        }
        verify(objectMapper).readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
    }
} 
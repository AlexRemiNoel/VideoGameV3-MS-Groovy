package com.example.apigatewayservice.DomainClientLayer.admin;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.presentationlayer.admin.AdminRequestModel;
import com.example.apigatewayservice.presentationlayer.admin.AdminResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// Corrected NotFoundException import for client tests if it differs from the global one
import com.example.apigatewayservice.exception.NotFoundException; 
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private AdminServiceClient adminServiceClient;

    private final String ADMIN_ID = "admin-uuid-123";
    private final String ADMIN_SERVICE_HOST = "user-management"; // As per AdminServiceClient constructor
    private final String ADMIN_SERVICE_PORT = "8080";
    private String BASE_URL;

    private AdminResponseModel sampleAdminResponse;
    private AdminRequestModel sampleAdminRequest;

    @BeforeEach
    void setUp() {
        adminServiceClient = new AdminServiceClient(restTemplate, objectMapper, ADMIN_SERVICE_HOST, ADMIN_SERVICE_PORT);
        BASE_URL = "http://" + ADMIN_SERVICE_HOST + ":" + ADMIN_SERVICE_PORT + "/api/v1/admin";

        sampleAdminResponse = AdminResponseModel.builder().adminId(ADMIN_ID).username("testAdmin").build();
        sampleAdminRequest = AdminRequestModel.builder().username("testAdmin").password("password").build();
    }

    private HttpClientErrorException mockHttpClientErrorException(HttpStatus status, String responseBody, String exceptionMessage) {
        String msg = (exceptionMessage != null && !exceptionMessage.isBlank()) ? exceptionMessage : status.getReasonPhrase();
        byte[] bodyBytes = (responseBody != null) ? responseBody.getBytes(StandardCharsets.UTF_8) : null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpClientErrorException(status, msg, headers, bodyBytes, StandardCharsets.UTF_8);
    }

    private HttpClientErrorException mockHttpClientErrorException(HttpStatus status, String responseBody) {
        // Provide the status reason phrase as the default message if not specified otherwise
        return mockHttpClientErrorException(status, responseBody, status.getReasonPhrase());
    }

    // --- Success Cases ---
    @Test
    void getAllAdmins_success() {
        ResponseEntity<List<AdminResponseModel>> responseEntity = new ResponseEntity<>(List.of(sampleAdminResponse), HttpStatus.OK);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);
        List<AdminResponseModel> result = adminServiceClient.getAllAdmins();
        assertFalse(result.isEmpty());
        assertEquals(sampleAdminResponse, result.get(0));
        verify(restTemplate).exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getAdminById_success() {
        String url = BASE_URL + "/" + ADMIN_ID;
        when(restTemplate.getForObject(eq(url), eq(AdminResponseModel.class))).thenReturn(sampleAdminResponse);
        AdminResponseModel result = adminServiceClient.getAdminById(ADMIN_ID);
        assertEquals(sampleAdminResponse, result);
        verify(restTemplate).getForObject(eq(url), eq(AdminResponseModel.class));
    }

    @Test
    void addAdmin_success() {
        when(restTemplate.postForObject(eq(BASE_URL), eq(sampleAdminRequest), eq(AdminResponseModel.class))).thenReturn(sampleAdminResponse);
        AdminResponseModel result = adminServiceClient.addAdmin(sampleAdminRequest);
        assertEquals(sampleAdminResponse, result);
        verify(restTemplate).postForObject(eq(BASE_URL), eq(sampleAdminRequest), eq(AdminResponseModel.class));
    }

    @Test
    void updateAdmin_success() {
        String url = BASE_URL + "/" + ADMIN_ID;
        doNothing().when(restTemplate).put(eq(url), eq(sampleAdminRequest));
        assertNull(adminServiceClient.updateAdmin(sampleAdminRequest, ADMIN_ID)); // current impl returns null
        verify(restTemplate).put(eq(url), eq(sampleAdminRequest));
    }

    @Test
    void deleteAdmin_success() {
        String url = BASE_URL + "/" + ADMIN_ID;
        doNothing().when(restTemplate).delete(eq(url));
        adminServiceClient.deleteAdmin(ADMIN_ID);
        verify(restTemplate).delete(eq(url));
    }

    // --- Exception Cases ---
    @Test
    void getAdminById_notFoundException() throws JsonProcessingException {
        String url = BASE_URL + "/" + ADMIN_ID;
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", "NF message");
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(errorInfo);
        when(restTemplate.getForObject(eq(url), eq(AdminResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.NOT_FOUND, "{\"message\": \"NF message\"}"));

        assertThrows(NotFoundException.class, () -> adminServiceClient.getAdminById(ADMIN_ID));
        verify(objectMapper).readValue(anyString(), eq(HttpErrorInfo.class));
    }

    @Test
    void getAdminById_invalidInputException() throws JsonProcessingException {
        String url = BASE_URL + "/" + ADMIN_ID;
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/path", "Invalid input");
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class))).thenReturn(errorInfo);
        when(restTemplate.getForObject(eq(url), eq(AdminResponseModel.class)))
                .thenThrow(mockHttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, "{\"message\": \"Invalid input\"}"));

        assertThrows(InvalidInputException.class, () -> adminServiceClient.getAdminById(ADMIN_ID));
    }

    @Test
    void getAdminById_genericHttpClientErrorException() {
        String url = BASE_URL + "/" + ADMIN_ID;
        HttpClientErrorException originalEx = mockHttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
        when(restTemplate.getForObject(eq(url), eq(AdminResponseModel.class))).thenThrow(originalEx);

        HttpClientErrorException thrownEx = assertThrows(HttpClientErrorException.class, () -> adminServiceClient.getAdminById(ADMIN_ID));
        assertEquals(originalEx, thrownEx);
    }

    // --- getErrorMessage specific tests (similar to UserServiceClientTest) ---
    @Test
    void getErrorMessage_success() throws JsonProcessingException {
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, "{\"message\":\"Parsed Message\"}");
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", "Parsed Message");
        when(objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)).thenReturn(errorInfo);
        
        // Test indirectly via handleHttpClientException
        try {
            when(restTemplate.getForObject(anyString(), eq(AdminResponseModel.class))).thenThrow(ex);
            adminServiceClient.getAdminById("some-id");
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
            // This setup will cause getAdminById to throw NotFoundException,
            // whose message comes from getErrorMessage(ex)
            when(restTemplate.getForObject(anyString(), eq(AdminResponseModel.class))).thenThrow(ex);
            adminServiceClient.getAdminById("some-id-for-empty-parsed-message");
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
        final String specificExMessage = "Underlying ex message for admin client IOEx";
        HttpStatus status = HttpStatus.NOT_FOUND;
        HttpClientErrorException mockException = mockHttpClientErrorException(status, "invalid-json", specificExMessage);
        when(objectMapper.readValue("invalid-json", HttpErrorInfo.class)).thenThrow(new JsonProcessingException("Parse fail") {});

        String result = adminServiceClient.getErrorMessage(mockException);
        // ex.getMessage() from mockHttpClientErrorException is "status.value() specificExMessage"
        assertEquals(status.value() + " " + specificExMessage, result);
    }

    @Test
    void getErrorMessage_objectMapperThrowsIOException_exMessageIsBlank_fallsBackToStatusCode() throws IOException {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        // Use Mockito.mock to have full control over getMessage()
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        when(mockException.getResponseBodyAsString()).thenReturn("invalid-json-again");
        when(mockException.getMessage()).thenReturn(""); // Ensure ex.getMessage() is blank
        when(mockException.getStatusCode()).thenReturn(status);
        // Ensure getStatusText() is stubbed as it's used in the final fallback
        when(mockException.getStatusText()).thenReturn(status.getReasonPhrase());


        when(objectMapper.readValue("invalid-json-again", HttpErrorInfo.class)).thenThrow(new JsonProcessingException("Parse fail again") {});

        String result = adminServiceClient.getErrorMessage(mockException);
        assertEquals("An error occurred: " + status.value() + " " + status.getReasonPhrase(), result);
    }

    @Test
    void getErrorMessage_emptyResponseBody_fallsBackToExMessage() {
        final String specificExMessage = "Exception message for empty body in admin client";
        HttpStatus status = HttpStatus.NOT_FOUND;
        HttpClientErrorException mockException = mockHttpClientErrorException(status, "", specificExMessage);
        // No objectMapper interaction expected as body is blank

        String result = adminServiceClient.getErrorMessage(mockException);
        assertEquals(status.value() + " " + specificExMessage, result);
    }

    @Test
    void getErrorMessage_nullResponseBody_exMessageIsBlank_fallsBackToStatusCode() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        // Use Mockito.mock for precise control
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        when(mockException.getResponseBodyAsString()).thenReturn(null);
        when(mockException.getMessage()).thenReturn(null); // Ensure ex.getMessage() is null
        when(mockException.getStatusCode()).thenReturn(status);
        when(mockException.getStatusText()).thenReturn(status.getReasonPhrase());


        String result = adminServiceClient.getErrorMessage(mockException);
        assertEquals("An error occurred: " + status.value() + " " + status.getReasonPhrase(), result);
    }

    @Test
    void getErrorMessage_objectMapperThrowsOtherException() throws IOException {
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, "some-body");
        when(objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)).thenThrow(new RuntimeException("Unexpected parse fail"));

        try {
            when(restTemplate.getForObject(anyString(), eq(AdminResponseModel.class))).thenThrow(ex);
            adminServiceClient.getAdminById("some-id");
        } catch (NotFoundException e) {
            assertEquals(ex.getMessage(), e.getMessage());
        }
        verify(objectMapper).readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
    }
} 
package uk.gov.companieshouse.pscstatementdataapi.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.util.ResourceChangedRequestMapper;

@ExtendWith(MockitoExtension.class)
class PscStatementApiServiceTest {

    @InjectMocks
    private PscStatementApiService pscStatementApiService;

    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private ResourceChangedRequestMapper resourceChangedRequestMapper;

    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost privateChangedResourcePost;
    @Mock
    private ApiResponse<Void> response;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;

    @Test
    void invokeChsKafkaEndpoint() throws ApiErrorResponseException {
        // Given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        // When
        ApiResponse<?> apiResponse = pscStatementApiService.invokeChsKafkaApi(resourceChangedRequest);

        // Then
        assertThat(apiResponse).isNotNull();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost).execute();
    }

    @Test
    void invokeChsKafkaEndpointThrowsApiErrorException() throws ApiErrorResponseException {
        // Given
        ApiErrorResponseException exception = new ApiErrorResponseException(
                new HttpResponseException.Builder(408, "Test Request timeout", new HttpHeaders()));
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        // When
        Executable actual = () -> pscStatementApiService.invokeChsKafkaApi(resourceChangedRequest);

        // Then
        assertThrows(ServiceUnavailableException.class, actual);
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost).execute();
    }

    @Test
    void invokeChsKafkaEndpointThrowsRuntimeException() throws ApiErrorResponseException {
        // Given
        RuntimeException exception = new RuntimeException("Test Runtime exception");
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        // When
        Executable actual = () -> pscStatementApiService.invokeChsKafkaApi(resourceChangedRequest);

        // Then
        assertThrows(RuntimeException.class, actual);
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost).execute();
    }

    @Test
    void invokeChsKafkaEndpointWithDelete() throws ApiErrorResponseException {
        // Given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        // When
        ApiResponse<?> apiResponse = pscStatementApiService.invokeChsKafkaApiDelete(resourceChangedRequest);

        // Then
        assertThat(apiResponse).isNotNull();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost).execute();
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteThrowsApiErrorException() throws ApiErrorResponseException {
        // Given
        ApiErrorResponseException exception = new ApiErrorResponseException(
                new HttpResponseException.Builder(408, "Test Request timeout", new HttpHeaders()));
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        // When
        Executable actual = () -> pscStatementApiService.invokeChsKafkaApiDelete(resourceChangedRequest);

        // Then
        assertThrows(ServiceUnavailableException.class, actual);
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost).execute();
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteThrowsRuntimeException() throws ApiErrorResponseException {
        // Given
        RuntimeException exception = new RuntimeException("Test Runtime exception");
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        // When
        Executable actual = () -> pscStatementApiService.invokeChsKafkaApiDelete(resourceChangedRequest);

        // Then
        assertThrows(RuntimeException.class, actual);
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost).execute();
    }

}

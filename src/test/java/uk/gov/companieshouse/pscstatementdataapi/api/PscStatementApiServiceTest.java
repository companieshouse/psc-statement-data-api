package uk.gov.companieshouse.pscstatementdataapi.api;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PscStatementApiServiceTest {

    @Mock
    InternalApiClient internalApiClient;
    @Mock
    PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost privateChangedResourcePost;
    @Mock
    private ApiResponse<Void> response;

    private TestHelper testHelper;
    @InjectMocks
    private PscStatementApiService pscStatementApiService;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper();
    }

    @Test
    void invokeChsKafkaEndpointWithDelete() throws ApiErrorResponseException {
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = pscStatementApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.PSC_STATEMENT_ID, testHelper.getStatement());
        Assertions.assertThat(apiResponse).isNotNull();

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost, times(1)).execute();
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteThrowsException() throws ApiErrorResponseException {
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(RuntimeException.class);

        Assert.assertThrows(RuntimeException.class, () -> pscStatementApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.PSC_STATEMENT_ID, testHelper.getStatement()));

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(), Mockito.any());
        verify(privateChangedResourcePost, times(1)).execute();
    }

}

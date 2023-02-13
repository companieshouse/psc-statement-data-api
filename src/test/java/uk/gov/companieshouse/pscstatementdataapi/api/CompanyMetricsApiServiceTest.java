package uk.gov.companieshouse.pscstatementdataapi.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.pscstatementdataapi.config.ApplicationConfig;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.metrics.PrivateCompanyMetricsResourceHandler;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

@ExtendWith(MockitoExtension.class)
public class CompanyMetricsApiServiceTest {


    private static final String COMPANY_NUMBER = TestHelper.COMPANY_NUMBER;

    private final Logger logger = Mockito.mock(Logger.class);;

    @Mock
    private PrivateCompanyMetricsResourceHandler privateCompanyMetricsResourceHandler;

    @Mock
    private PrivateCompanyMetricsGet privateCompanyMetricsGet;

    @Mock
    private ApiResponse<MetricsApi> response;

    @Mock
    private InternalApiClient internalApiClient;

    @InjectMocks
    private CompanyMetricsApiService companyMetricsApiService;

    @Test
    void shouldInvokeCompanyMetricsEndpointSuccessfully()
            throws ApiErrorResponseException, URIValidationException {

        companyMetricsApiService.internalApiClient = internalApiClient;
        when(internalApiClient.privateCompanyMetricsResourceHandler()).thenReturn(
                privateCompanyMetricsResourceHandler);
        when(privateCompanyMetricsResourceHandler.getCompanyMetrics(any())).thenReturn(
                privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenReturn(response);

        Optional<MetricsApi> apiResponse = companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER);

        assertThat(apiResponse).isNotNull();
        verify(privateCompanyMetricsResourceHandler, times(1)).getCompanyMetrics(any());
        verify(privateCompanyMetricsGet, times(1)).execute();
    }

    @Test
    void should_handle_exception_when_company_metrics_endpoint_throws_exception()
            throws ApiErrorResponseException, URIValidationException {

        companyMetricsApiService.internalApiClient = internalApiClient;
        when(internalApiClient.privateCompanyMetricsResourceHandler()).thenReturn(
                privateCompanyMetricsResourceHandler);
        when(privateCompanyMetricsResourceHandler.getCompanyMetrics(any())).thenReturn(
                privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(ApiErrorResponseException.class);
        assertThrows(ResponseStatusException.class, () -> companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER));
        verify(privateCompanyMetricsResourceHandler, times(1)).getCompanyMetrics(any());
        verify(privateCompanyMetricsGet, times(1)).execute();
    }
}

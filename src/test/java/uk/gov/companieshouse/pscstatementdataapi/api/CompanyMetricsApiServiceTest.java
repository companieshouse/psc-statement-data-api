package uk.gov.companieshouse.pscstatementdataapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException.Builder;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.metrics.PrivateCompanyMetricsResourceHandler;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadGatewayException;

@ExtendWith(MockitoExtension.class)
class CompanyMetricsApiServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String URL = "/company/%s/metrics".formatted(COMPANY_NUMBER);
    private static final ApiResponse<MetricsApi> SUCCESS_RESPONSE = new ApiResponse<>(200, null, new MetricsApi());

    @InjectMocks
    private CompanyMetricsApiService service;

    @Mock
    private Supplier<InternalApiClient> supplier;

    @Mock
    private InternalApiClient client;
    @Mock
    private PrivateCompanyMetricsResourceHandler privateCompanyMetricsResourceHandler;
    @Mock
    private PrivateCompanyMetricsGet privateCompanyMetricsGet;

    @Test
    void shouldGetCompanyMetrics() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(privateCompanyMetricsResourceHandler);
        when(privateCompanyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenReturn(SUCCESS_RESPONSE);

        Optional<MetricsApi> expected = Optional.of(new MetricsApi());

        // when
        Optional<MetricsApi> actual = service.getCompanyMetrics(COMPANY_NUMBER);

        // then
        verify(privateCompanyMetricsResourceHandler).getCompanyMetrics(URL);
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetCompanyMetricsWithNullResponseAndReturnEmptyOptional() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(privateCompanyMetricsResourceHandler);
        when(privateCompanyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenReturn(null);

        Optional<MetricsApi> expected = Optional.empty();

        // when
        Optional<MetricsApi> actual = service.getCompanyMetrics(COMPANY_NUMBER);

        // then
        verify(privateCompanyMetricsResourceHandler).getCompanyMetrics(URL);
        assertEquals(expected, actual);
    }

    @Test
    void shouldContinueProcessingWhenApiRespondsWith404NotFound() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(privateCompanyMetricsResourceHandler);
        when(privateCompanyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(buildApiErrorResponseException(404));

        // when
        Optional<MetricsApi> actual = service.getCompanyMetrics(COMPANY_NUMBER);

        // then
        assertTrue(actual.isEmpty());
        verify(privateCompanyMetricsResourceHandler).getCompanyMetrics(URL);
    }

    @ParameterizedTest
    @CsvSource({
            "400",
            "401",
            "403",
            "405",
            "410",
            "500",
            "503"
    })
    void shouldThrowBadGatewayExceptionWhenApiRespondsWithNon404ErrorCode(final int statusCode) throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(privateCompanyMetricsResourceHandler);
        when(privateCompanyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(buildApiErrorResponseException(statusCode));

        // when
        Executable executable = () -> service.getCompanyMetrics(COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privateCompanyMetricsResourceHandler).getCompanyMetrics(URL);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenURIValidationExceptionCaught() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateCompanyMetricsResourceHandler()).thenReturn(privateCompanyMetricsResourceHandler);
        when(privateCompanyMetricsResourceHandler.getCompanyMetrics(anyString())).thenReturn(privateCompanyMetricsGet);
        when(privateCompanyMetricsGet.execute()).thenThrow(URIValidationException.class);

        // when
        Executable executable = () -> service.getCompanyMetrics(COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privateCompanyMetricsResourceHandler).getCompanyMetrics(URL);
    }

    private static ApiErrorResponseException buildApiErrorResponseException(final int statusCode) {
        Builder builder = new Builder(statusCode, "", new HttpHeaders());
        return new ApiErrorResponseException(builder);
    }
}
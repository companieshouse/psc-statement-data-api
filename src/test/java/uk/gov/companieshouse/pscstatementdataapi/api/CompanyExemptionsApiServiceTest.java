package uk.gov.companieshouse.pscstatementdataapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.exemptions.request.PrivateCompanyExemptionsGetAll;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadGatewayException;

@ExtendWith(MockitoExtension.class)
class CompanyExemptionsApiServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String URL = "/company/%s/exemptions".formatted(COMPANY_NUMBER);
    private static final ApiResponse<CompanyExemptions> SUCCESS_RESPONSE = new ApiResponse<>(200, null,
            new CompanyExemptions());

    @InjectMocks
    private CompanyExemptionsApiService service;

    @Mock
    private Supplier<InternalApiClient> supplier;

    @Mock
    private InternalApiClient client;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateCompanyExemptionsGetAll privateCompanyExemptionsGetAll;

    @Test
    void shouldGetCompanyExemptions() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.getCompanyExemptionsResource(anyString())).thenReturn(
                privateCompanyExemptionsGetAll);
        when(privateCompanyExemptionsGetAll.execute()).thenReturn(SUCCESS_RESPONSE);

        Optional<CompanyExemptions> expected = Optional.of(new CompanyExemptions());

        // when
        Optional<CompanyExemptions> actual = service.getCompanyExemptions(COMPANY_NUMBER);

        // then
        verify(privateDeltaResourceHandler).getCompanyExemptionsResource(URL);
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetCompanyExemptionsWithNullResponseAndReturnEmptyOptional() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.getCompanyExemptionsResource(anyString())).thenReturn(
                privateCompanyExemptionsGetAll);
        when(privateCompanyExemptionsGetAll.execute()).thenReturn(null);

        Optional<CompanyExemptions> expected = Optional.empty();

        // when
        Optional<CompanyExemptions> actual = service.getCompanyExemptions(COMPANY_NUMBER);

        // then
        verify(privateDeltaResourceHandler).getCompanyExemptionsResource(URL);
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnEmptyOptionalWhenApiRespondsWith404NotFound() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.getCompanyExemptionsResource(anyString())).thenReturn(
                privateCompanyExemptionsGetAll);
        when(privateCompanyExemptionsGetAll.execute()).thenThrow(buildApiErrorResponseException(404));

        // when
        final Optional<CompanyExemptions> actual = service.getCompanyExemptions(COMPANY_NUMBER);

        // then
        verify(privateDeltaResourceHandler).getCompanyExemptionsResource(URL);
        assertEquals(Optional.empty(), actual);
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
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.getCompanyExemptionsResource(anyString())).thenReturn(
                privateCompanyExemptionsGetAll);
        when(privateCompanyExemptionsGetAll.execute()).thenThrow(buildApiErrorResponseException(statusCode));

        // when
        Executable executable = () -> service.getCompanyExemptions(COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privateDeltaResourceHandler).getCompanyExemptionsResource(URL);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenURIValidationExceptionCaught() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.getCompanyExemptionsResource(anyString())).thenReturn(
                privateCompanyExemptionsGetAll);
        when(privateCompanyExemptionsGetAll.execute()).thenThrow(URIValidationException.class);

        // when
        Executable executable = () -> service.getCompanyExemptions(COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privateDeltaResourceHandler).getCompanyExemptionsResource(URL);
    }

    private static ApiErrorResponseException buildApiErrorResponseException(final int statusCode) {
        Builder builder = new Builder(statusCode, "", new HttpHeaders());
        return new ApiErrorResponseException(builder);
    }
}
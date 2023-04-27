package uk.gov.companieshouse.pscstatementdataapi.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.exemptions.request.PrivateCompanyExemptionsGetAll;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.utils.TestHelper;

@ExtendWith(MockitoExtension.class)
public class CompanyExemptionsApiServiceTest {

    private static final String COMPANY_NUMBER = TestHelper.COMPANY_NUMBER;

    @Mock
    private PrivateDeltaResourceHandler resourceHandler;

    @Mock
    private PrivateCompanyExemptionsGetAll companyExemptionsGetAll;

    @Mock
    private ApiResponse<CompanyExemptions> response;

    @Mock
    private InternalApiClient internalApiClient;

    private final Logger logger = Mockito.mock(Logger.class);

    @Spy
    @InjectMocks
    private CompanyExemptionsApiService companyExemptionsApiService;

    @BeforeEach
    void setUp() {
        companyExemptionsApiService.internalApiClient = internalApiClient;
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(resourceHandler);
        when(resourceHandler.getCompanyExemptionsResource(any())).thenReturn(companyExemptionsGetAll);
    }

    @Test
    void getCompanyExemptions() throws ApiErrorResponseException, URIValidationException {
        when(companyExemptionsGetAll.execute()).thenReturn(response);

        Optional<CompanyExemptions> apiResponse = companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER);

        assertThat(apiResponse).isNotNull();
        verify(resourceHandler, times(1)).getCompanyExemptionsResource(any());
        verify(companyExemptionsGetAll, times(1)).execute();
    }

    @Test
    void handleExceptionWhenGetExemptionsThrows() throws ApiErrorResponseException, URIValidationException {
        when(companyExemptionsGetAll.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ResponseStatusException.class, () -> companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER));

        verify(resourceHandler, times(1)).getCompanyExemptionsResource(any());
        verify(companyExemptionsGetAll, times(1)).execute();
    }

    @Test
    void returnsEmptyWhenExemptionsNotFound() throws ApiErrorResponseException, URIValidationException {
        when(companyExemptionsGetAll.execute()).thenThrow(ResourceNotFoundException.class);
        Optional<CompanyExemptions> apiResponse = companyExemptionsApiService.getCompanyExeptions(COMPANY_NUMBER);

        assertThat(apiResponse).isEmpty();
        verify(resourceHandler, times(1)).getCompanyExemptionsResource(any());
        verify(companyExemptionsGetAll, times(1)).execute();
    }
    
}

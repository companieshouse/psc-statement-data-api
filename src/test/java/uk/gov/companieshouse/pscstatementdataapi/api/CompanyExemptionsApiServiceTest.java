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
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.exemptions.request.PrivateCompanyExemptionsGetAll;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
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
    
}

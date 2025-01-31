package uk.gov.companieshouse.pscstatementdataapi.api;

import static uk.gov.companieshouse.pscstatementdataapi.PSCStatementDataApiApplication.NAMESPACE;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadGatewayException;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;


@Component
public class CompanyExemptionsApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final Supplier<InternalApiClient> exemptionsApiClientSupplier;

    public CompanyExemptionsApiService(
            @Qualifier("exemptionsApiClientSupplier") Supplier<InternalApiClient> exemptionsApiClientSupplier) {
        this.exemptionsApiClientSupplier = exemptionsApiClientSupplier;
    }

    public Optional<CompanyExemptions> getCompanyExemptions(final String companyNumber) {
        ApiResponse<CompanyExemptions> response = null;
        try {
            response = exemptionsApiClientSupplier.get()
                    .privateDeltaResourceHandler()
                    .getCompanyExemptionsResource("/company/%s/exemptions".formatted(companyNumber))
                    .execute();
        } catch (ApiErrorResponseException ex) {
            final int statusCode = ex.getStatusCode();
            LOGGER.info("Company Exemptions API GET failed with status code [%s]".formatted(statusCode),
                    DataMapHolder.getLogMap());
            if (statusCode != 404) {
                throw new BadGatewayException("Error calling Company Exemptions API endpoint", ex);
            }
        } catch (URIValidationException ex) {
            LOGGER.info("URI validation error when calling Company Exemptions API", DataMapHolder.getLogMap());
            throw new BadGatewayException("URI validation error when calling Company Exemptions API", ex);
        }

        return Optional.ofNullable(response)
                .map(ApiResponse::getData);
    }
}

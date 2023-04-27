package uk.gov.companieshouse.pscstatementdataapi.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.handler.delta.exemptions.request.PrivateCompanyExemptionsGetAll;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyExemptionsApiService {

    private static final String GET_COMPANY_EXEMPTIONS_ENDPOINT = "/company/%s/exemptions";

    @Autowired
    InternalApiClient internalApiClient;

    @Autowired
    Logger logger;

    public Optional<CompanyExemptions> getCompanyExeptions (final String companyNumber) {
        logger.trace(String.format("Started : getCompanyExemptions for Company Number %s ", companyNumber));
        String basePath = System.getenv("API_URL");
        internalApiClient.setBasePath(basePath);
        logger.trace(String.format("Created client %s", internalApiClient.getBasePath()));
        PrivateCompanyExemptionsGetAll companyExemptions = 
                internalApiClient.privateDeltaResourceHandler()
                        .getCompanyExemptionsResource(
                                String.format(GET_COMPANY_EXEMPTIONS_ENDPOINT, companyNumber));

        try {
            ApiResponse<CompanyExemptions> execute = companyExemptions.execute();
            return Optional.ofNullable(execute.getData());
        } catch (URIValidationException e) {
            logger.error("Error occurred while calling getCompanyExemptions endpoint.");
        } catch (ApiErrorResponseException e) {
            if (e.getStatusCode() == 404) {
                logger.error(String.format(
                        "Error occurred while calling getCompanyExemptions endpoint. " 
                                + " Status Code: 404 - NOT FOUND for %s", companyNumber));
            } else {
                logger.error(String.format(
                    "Error calling getCompanyExemptions endpoint. " 
                        + "Status Code: [%s] - message [%s]", e.getStatusCode(), e.getStatusMessage()));
                throw new ResponseStatusException(e.getStatusCode(), e.getStatusMessage(), null);
            }
        } catch (Exception e) {
            logger.error(String.format(
                    "Error calling getCompanyExemptions endpoint. " 
                        + " message [%s]", e.getMessage()));
        }
        return Optional.empty();
    }
    
}

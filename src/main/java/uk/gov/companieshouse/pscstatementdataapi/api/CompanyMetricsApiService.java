package uk.gov.companieshouse.pscstatementdataapi.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.metrics.request.PrivateCompanyMetricsGet;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;


@Service
public class CompanyMetricsApiService {

    private static final String GET_COMPANY_METRICS_ENDPOINT = "/company/%s/metrics";

    @Autowired
    InternalApiClient internalApiClient;
    /**
     * Invoke Company Metrics API.
     */
    @Autowired
    Logger logger;

    /**
     * Get company metrics.
     *
     * @param companyNumber company number.
     * @return company metrics.
     */
    public Optional<MetricsApi> getCompanyMetrics(final String companyNumber) {
        logger.trace(String.format("Started : getCompanyMetrics for Company Number %s ",
                companyNumber
        ));
        String resourceHandler = internalApiClient.getBasePath();
        logger.trace(String.format("Created client %s", resourceHandler));
        PrivateCompanyMetricsGet companyMetrics =
                internalApiClient.privateCompanyMetricsResourceHandler()
                        .getCompanyMetrics(
                                String.format(GET_COMPANY_METRICS_ENDPOINT, companyNumber));
        try {
            ApiResponse<MetricsApi> execute = companyMetrics.execute();
            return Optional.ofNullable(execute.getData());
        } catch (URIValidationException exp) {
            logger.error("Error occurred while calling getCompanyMetrics endpoint.");
        } catch (ApiErrorResponseException exp) {
            if (exp.getStatusCode() == 404) {
                logger.error(String.format(
                        "Error occurred while calling getCompanyMetrics endpoint. "
                                + "Status Code: 404 - NOT FOUND for %s.", companyNumber));
            } else {
                logger.error(String.format("Error calling getCompanyMetrics endpoint. "
                                + "Status Code: [%s] - message [%s]", exp.getStatusCode(),
                        exp.getStatusMessage()));
                throw new ResponseStatusException(exp.getStatusCode(),
                        exp.getStatusMessage(), null);
            }
        } catch (Exception exp) {
            logger.error(String.format("Error calling getCompanyMetrics endpoint. "
                            + " message [%s]", exp.getMessage()));
        }
        return Optional.empty();
    }
}

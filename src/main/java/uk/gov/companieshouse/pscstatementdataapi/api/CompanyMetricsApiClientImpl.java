package uk.gov.companieshouse.pscstatementdataapi.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("CompanyMetricsApiClient")
public class CompanyMetricsApiClientImpl extends ApiClientServiceImpl {

    public CompanyMetricsApiClientImpl(
            @Value("${chs.company.metrics.key}") String companyMetricsApiKey,
            @Value("${chs.company.metrics.endpoint}") String internalApiUrl) {
        super(companyMetricsApiKey, internalApiUrl);
    }
}

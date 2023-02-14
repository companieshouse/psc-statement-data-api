package uk.gov.companieshouse.pscstatementdataapi.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.logging.Logger;

@Service
public class ApiClientService {

    @Value("${api.psc-statements-data-api-key}")
    private String apiKey;
    @Value("${api.api-url}")
    private String url;

    private Logger logger;

    @Autowired
    public ApiClientService(Logger logger) {
        this.logger = logger;
    }

    /**
     * fetches api client.
     */
    public InternalApiClient getApiClient() {
        InternalApiClient apiClient = new InternalApiClient(this.getHttpClient());
        apiClient.setBasePath(url);
        return apiClient;
    }

    /**
     * fetches HttpClient.
     */
    public ApiKeyHttpClient getHttpClient() {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(apiKey);
        return httpClient;
    }
}


package uk.gov.companieshouse.pscstatementdataapi.api;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;

@Service
public class ApiClientServiceImpl implements ApiClientService {

    private String apiKey;

    private String internalApiUrl;

    public ApiClientServiceImpl(String apiKey, String internalApiUrl) {
        this.apiKey = apiKey;
        this.internalApiUrl = internalApiUrl;
    }

    @Override
    public InternalApiClient getInternalApiClient() {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient());
        internalApiClient.setInternalBasePath(internalApiUrl);
        internalApiClient.setBasePath(internalApiUrl);

        return internalApiClient;
    }

    private HttpClient getHttpClient() {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(apiKey);
        return httpClient;
    }

}

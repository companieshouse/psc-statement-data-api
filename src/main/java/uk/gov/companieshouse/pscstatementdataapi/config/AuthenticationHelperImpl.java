package uk.gov.companieshouse.pscstatementdataapi.config;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/*public class AuthenticationHelperImpl implements AuthenticationHelper{

    private String ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER = "ERIC-Authorised-Key-Privileges";
    private static final String SENSITIVE_DATA_PRIVILEGE = "sensitive-data";
    private static final String INTERNAL_APP_PRIVILEGE = "internal-app";
    @Override
    public String[] getApiKeyPrivileges(HttpServletRequest request) {
        String headers = request.getHeader(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER);

        return Optional.ofNullable(headers).map(v -> v.split(",")).
                orElse(new String[]{});
    }

    @Override
    public boolean isKeyElevatedPrivilegesAuthorised(HttpServletRequest request) {
       String[] privileges = this.getApiKeyPrivileges(request);
              return request.getMethod().equalsIgnoreCase("GET") ? true:
                      ArrayUtils.contains(privileges, INTERNAL_APP_PRIVILEGE);
    }
}*/

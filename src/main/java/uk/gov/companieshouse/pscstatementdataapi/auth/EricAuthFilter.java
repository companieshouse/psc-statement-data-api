package uk.gov.companieshouse.pscstatementdataapi.auth;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class EricAuthFilter extends OncePerRequestFilter {

    @Autowired
    Logger logger;

    public EricAuthFilter(Logger logger) {
        this.logger=logger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String identity = request.getHeader("ERIC-Identity");
        String identityType = request.getHeader("ERIC-Identity-Type");

        if (StringUtils.isEmpty(identity)) {
            logger.info("Unauthorised request received without eric identity");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (StringUtils.isEmpty(identityType) ||
                !(identityType.equalsIgnoreCase("Key") || identityType.equalsIgnoreCase("oauth2"))) {
            logger.info("Unauthorised request received without eric identity type");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (identityType.equalsIgnoreCase("Key") && !isKeyAuthenticated(request)) {
            logger.info("supplied key does not have sufficient privilege for the action");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request,response);
    }

    private boolean isKeyAuthenticated(HttpServletRequest request) {
        String[] privileges = getApiKeyPrivileges(request);

        return request.getMethod().equals("GET") ||
                ArrayUtils.contains(privileges, "internal-app");
    }

    private String[] getApiKeyPrivileges(HttpServletRequest request) {
        String commaSeparatedPrivilegeString = request.getHeader("ERIC-Authorised-Key-Privileges");

        return Optional.ofNullable(commaSeparatedPrivilegeString)
                .map(s -> s.split(","))
                .orElse(new String[]{});
    }

}
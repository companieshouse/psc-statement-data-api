package uk.gov.companieshouse.pscstatementdataapi.auth;

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

@Component
public class EricAuthFilter extends OncePerRequestFilter {

    @Autowired
    Logger logger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String identity = request.getHeader("ERIC-Identity");
        String identityType = request.getHeader("ERIC-Identity-Type");
        logger.info(identity + identityType);
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

        filterChain.doFilter(request,response);
    }

}



package uk.gov.companieshouse.pscstatementdataapi.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SimpleAuthenticationInterceptor extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String identity = request.getHeader("ERIC-Identity");
        String identityType = request.getHeader("ERIC-Identity-Type");

        if (StringUtils.isEmpty(identity)) {
            logger.error("Unauthorised request received without eric identity");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            System.out.println("first if statement");

            return;
        }

        if (StringUtils.isEmpty(identityType) ||
                !(identityType.equalsIgnoreCase("Key") || identityType.equalsIgnoreCase("oauth2"))) {
            logger.error("Unauthorised request received without eric identity type");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            System.out.println("second if statement");
            return;
        }

        filterChain.doFilter(request,response);
    }

}



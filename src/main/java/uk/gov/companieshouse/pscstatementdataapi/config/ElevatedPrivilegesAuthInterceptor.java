package uk.gov.companieshouse.pscstatementdataapi.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*@Component
public class ElevatedPrivilegesAuthInterceptor implements HandlerInterceptor {

    AuthenticationHelper authenticationHelper;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String identityType = request.getHeader("ERIC-IDENTITY-TYPE");

        if(!StringUtils.isEmpty(identityType) &&
                identityType.equalsIgnoreCase("Key")){

            if(authenticationHelper.isKeyElevatedPrivilegesAuthorised(request)){
                return true;
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}*/

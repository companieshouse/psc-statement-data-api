package uk.gov.companieshouse.pscstatementdataapi.config;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {
   private InternalUserInterceptor internalUserInterceptor;
   private List<String> otherAllowedIdentityTypes;
   private List<String> externalMethods;
   private Logger logger;

   @Autowired
   public UserAuthInterceptor(List<String> externalMethods, List<String> otherAllowedIdentityTypes, InternalUserInterceptor internalUserInterceptor) {
      this.otherAllowedIdentityTypes = otherAllowedIdentityTypes;
      this.externalMethods = externalMethods;
      this.internalUserInterceptor = internalUserInterceptor;
      this.logger = LoggerFactory.getLogger(String.valueOf(UserAuthInterceptor.class));
   }

   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
      if (request.getMethod().equals("OPTIONS")) {
         response.setStatus(HttpServletResponse.SC_NO_CONTENT);
         return true;
      }
      if (!this.externalMethods.contains(request.getMethod())) {
         return this.internalUserInterceptor.preHandle(request, response, handler);
      } else {
         ArrayList<String> validTypes = new ArrayList<String>(Arrays.asList("key"));
         validTypes.addAll(this.otherAllowedIdentityTypes);
         return this.hasAuthorisedIdentity(request, response) && this.hasValidAuthorisedIdentityType(request, response, validTypes);
      }
   }

   private boolean hasAuthorisedIdentity(HttpServletRequest request, HttpServletResponse response) {
      String authorisedUser = AuthorisationUtil.getAuthorisedIdentity(request);
      if (authorisedUser == null) {
         this.logger.debug("no authorised identity");
         response.setStatus(401);
         return false;
      } else {
         return true;
      }
   }

   private boolean hasValidAuthorisedIdentityType(HttpServletRequest request, HttpServletResponse response, List<String> validIdentityTypes) {
      String identityType = AuthorisationUtil.getAuthorisedIdentityType(request);
      if (!validIdentityTypes.contains(identityType)) {
         this.logger.debug("invalid identity type [" + identityType + "]");
         response.setStatus(403);
         return false;
      } else {
         return true;
      }
   }
}

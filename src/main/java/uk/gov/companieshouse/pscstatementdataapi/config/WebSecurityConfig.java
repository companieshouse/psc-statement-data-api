package uk.gov.companieshouse.pscstatementdataapi.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.UserAuthenticationInterceptor;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    //Methods that will not go through internal role validation
    List<String> externalMethods = Arrays.asList("GET");
    //Key type is automatically checked by the authenticator add other allowed auth types here
    List<String> otherAllowedAuthMethods = Arrays.asList("oauth2");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor());
    }

    @Bean
    public InternalUserInterceptor internalUserInterceptor() {
        return new InternalUserInterceptor();
    }

    @Bean
    public UserAuthenticationInterceptor userAuthenticationInterceptor() {
        return new UserAuthenticationInterceptor(externalMethods, otherAllowedAuthMethods, internalUserInterceptor());
    }
}

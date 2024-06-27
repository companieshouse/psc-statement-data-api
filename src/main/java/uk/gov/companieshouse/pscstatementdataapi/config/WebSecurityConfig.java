package uk.gov.companieshouse.pscstatementdataapi.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.pscstatementdataapi.security.CustomCorsFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig implements WebMvcConfigurer{

    //Methods that will not go through internal role validation
    @Autowired
    List<String> externalMethods;
    //Key type is automatically checked by the authenticator add other allowed auth types here
    List<String> otherAllowedAuthMethods = Arrays.asList("oauth2");

    @Autowired
    CustomCorsFilter customCorsFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor());
    }

    @Bean
    public InternalUserInterceptor internalUserInterceptor() {
        return new InternalUserInterceptor();
    }

    @Bean
    public UserAuthInterceptor userAuthenticationInterceptor() {
        return new UserAuthInterceptor(externalMethods, otherAllowedAuthMethods, internalUserInterceptor());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(customCorsFilter, CsrfFilter.class);

        return http.build();
    }

}

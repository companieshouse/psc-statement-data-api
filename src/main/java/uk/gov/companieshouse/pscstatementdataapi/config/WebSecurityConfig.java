package uk.gov.companieshouse.pscstatementdataapi.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig implements WebMvcConfigurer{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(new CustomCorsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize.requestMatchers("OPTIONS")
                    .permitAll().anyRequest().permitAll()
            );

        return http.build();
    }

    //Methods that will not go through internal role validation
    List<String> externalMethods = Arrays.asList("GET");
    //Key type is automatically checked by the authenticator add other allowed auth types here
    List<String> otherAllowedAuthMethods = Arrays.asList("oauth2");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor());
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(new CustomCorsConfigurationSource());
    }

    @Bean
    public InternalUserInterceptor internalUserInterceptor() {
        return new InternalUserInterceptor();
    }

    @Bean
    public UserAuthInterceptor userAuthenticationInterceptor() {
        return new UserAuthInterceptor(externalMethods, otherAllowedAuthMethods, internalUserInterceptor());
    }

    private class CustomCorsConfigurationSource implements CorsConfigurationSource {
        @Override
        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
            CorsConfiguration configuration = new CorsConfiguration();
            
            String allowedOrigins = request.getHeader("Eric-Allowed-Origins");
            String allowedHeaders = request.getHeader("Eric-Allowed-Headers");

            if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
                configuration.addAllowedOrigin("*");
                configuration.addAllowedMethod("*");
                configuration.addAllowedHeader("*");
            } else {
                configuration.setAllowedOrigins(List.of(allowedOrigins));
                configuration.setAllowedHeaders(List.of(allowedHeaders.split(",")));
                configuration.setAllowedMethods(externalMethods);
            }
            System.out.println("The allowed origins are: " + configuration.getAllowedOrigins() + " " + request.getMethod() + "\n\n");
            return configuration;
        }
    }
}

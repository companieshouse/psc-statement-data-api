package uk.gov.companieshouse.pscstatementdataapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private SimpleAuthenticationInterceptor simpleAuthenticationInterceptor;

    @Autowired
    private  ElevatedPrivilegesAuthInterceptor elevatedPrivilegesAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(elevatedPrivilegesAuthInterceptor);
    }
}*/

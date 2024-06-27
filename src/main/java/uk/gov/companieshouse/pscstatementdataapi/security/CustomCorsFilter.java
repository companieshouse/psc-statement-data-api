package uk.gov.companieshouse.pscstatementdataapi.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomCorsFilter implements Filter {

    List<String> externalMethods = Arrays.asList("GET");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        Enumeration<String> headers = httpServletRequest.getHeaderNames();
        Collection<String> respheaders = httpServletResponse.getHeaderNames();


        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            System.out.println("request header: " + header + ": " + httpServletRequest.getHeader(header));
        }

        for (String header : respheaders) {
            System.out.println("response header: " + header + ": " + httpServletResponse.getHeader(header));
        }

        // If request is options then ERIC hasn't set up headers
        // Otherwise All headers except allowed Methods have already been set
        if (httpServletRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
            httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
            httpServletResponse.setHeader("Access-Control-Max-Age", "3600");
            httpServletResponse.setStatus(204);
            return;
        } else {
            httpServletResponse.setHeader("Access-Control-Allow-Methods", String.join(",",externalMethods));
        }
        chain.doFilter(request, response);
    }

    @Bean
    public List<String> externalMethods() {
        return externalMethods;
    }
}
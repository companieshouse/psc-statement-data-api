package uk.gov.companieshouse.pscstatementdataapi.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

@SpringBootTest
public class EricAuthFilterTest {
    
    private EricAuthFilter interceptor;

    @Mock
    Logger logger;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;

    @BeforeEach
    void setUp(){
        interceptor = new EricAuthFilter(logger);
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void ericTokenFilterAllowsCallWithKeyCredentials() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("KEY");

        interceptor.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);

    }

    @Test
    void ericTokenFilterAllowsCallWithOauth2Credentials() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");

        interceptor.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void ericTokenFilterBlocksCallWithEmptyIdentity() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");

        interceptor.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
        verify(response, times(1)).sendError(403);
    }

    @Test
    void ericTokenFilterBlocksCallWithIncorrectIdentityType() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("INVALID");

        interceptor.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
        verify(response, times(1)).sendError(403);
    }

    @Test
    void ericTokenFilterBlocksPutWithMissingPrivilege() throws Exception {
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("key");

        interceptor.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
        verify(response, times(1)).sendError(403);
    }

    @Test
    void ericTokenFilterAllowsPutWithKeyPrivilege() throws Exception {
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("key");
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("internal-app");

        interceptor.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}

package io.nuvalence.workmanager.service.config;

import io.nuvalence.workmanager.service.config.dfcx.DialogflowAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
public class BasicAuthFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final String namespace;

    public BasicAuthFilter(String namespace, AuthenticationProvider... authenticationProviders) {
        this.namespace = namespace;
        this.authenticationManager = new ProviderManager(authenticationProviders);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authenticationResult = this.attemptAuthentication(request, response);
            SecurityContextHolder.getContext().setAuthentication(authenticationResult);
        } catch (AuthenticationException ex) {
            log.warn("Error authenticating client", ex);
        }
        filterChain.doFilter(request, response);
    }

    private Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)
                || !StringUtils.startsWithIgnoreCase(authorization, "basic")) {
            return null;
        } else {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            DialogflowAuthenticationToken authRequest =
                    new DialogflowAuthenticationToken(values[0], values[1], namespace);
            return this.authenticationManager.authenticate(authRequest);
        }
    }
}

package io.nuvalence.workmanager.service.config.dfcx;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class DialogflowAuthenticationProvider implements AuthenticationProvider {
    private static String USERNAME = "DFCX_USER";
    private static String PASSWORD = "yNCmEqJfPjxVAkRTrpHR";
    private final String namespace;

    public DialogflowAuthenticationProvider(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // "validate" username and password here
        // In the future, we want something more solid than this in place for DFCX => WM auth
        if (!username.equals(USERNAME) || !password.equals(PASSWORD)) {
            throw new AuthenticationServiceException("Error verifying basic auth.");
        }

        return new DialogflowAuthenticationToken(username, password, this.namespace);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(DialogflowAuthenticationToken.class);
    }
}

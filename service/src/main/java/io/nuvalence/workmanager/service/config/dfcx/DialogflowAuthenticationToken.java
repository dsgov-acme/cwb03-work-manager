package io.nuvalence.workmanager.service.config.dfcx;

import io.nuvalence.auth.token.UserRoleMapper;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public class DialogflowAuthenticationToken extends AbstractAuthenticationToken {
    private final String credentials;
    private final String principal;

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    public DialogflowAuthenticationToken(String principal, String credentials, String namespace) {
        super(UserRoleMapper.getGrantedAuthoritiesFromRoles(List.of(), namespace));
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(true);
    }
}

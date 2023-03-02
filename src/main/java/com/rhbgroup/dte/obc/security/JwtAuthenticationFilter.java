package com.rhbgroup.dte.obc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        AuthenticationStatus authResponse = jwtTokenManager.verifyRequest(request);
        switch (authResponse.getResult()) {
            case SUCCESS:
                jwtTokenManager.supplySecurityContext(request, authResponse.getJwt());
                filterChain.doFilter(request, response);
                break;
            case EXPIRED:
                response.sendError(402, "token_expired");
                break;
            case INVALID:
                response.sendError(401, "token_invalid");
                break;
            default:
                filterChain.doFilter(request, response);
                break;
        }
    }
}

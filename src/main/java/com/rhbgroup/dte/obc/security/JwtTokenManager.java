package com.rhbgroup.dte.obc.security;

import com.rhbgroup.dte.obc.entities.user.interactor.UserInteractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class JwtTokenManager {

    @Autowired
    private UserInteractor userInteractor;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    public AuthenticationResponse verifyRequest(HttpServletRequest httpServletRequest) {

        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        if (authorizationHeader == null) {
            return AuthenticationResponse.builder()
                    .result(AuthenticationStatusEnum.BYPASSED)
                    .jwt(null)
                    .build();
        }

        String jwtToken = authorizationHeader.substring(7);

        // validate token
        return AuthenticationResponse.builder()
                .result(AuthenticationStatusEnum.SUCCESS)
                .jwt(jwtToken)
                .build();
    }

    public void supplySecurityContext(HttpServletRequest request, String jwt) {
        try {
            String username = jwtTokenUtils.getUsernameFromJwtToken(jwt);
            UserDetails userDetails = userInteractor.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception ex) {
            // TODO catch more specific errors like invalid, expired, etc
            log.info("Error >> {}", ex.getMessage());
        }
    }
}

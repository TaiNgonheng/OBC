package com.rhbgroup.dte.obc.security;

import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.domains.user.service.UserDetailsServiceImpl;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final UserDetailsServiceImpl userDetailsService;
  private final JwtTokenManager jwtTokenManager;

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return new CustomAccessDeniedHandler();
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {

    httpSecurity
        .cors()
        .configurationSource(this::setupCORS)
        .and()
        .csrf()
        .disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .exceptionHandling()
        .accessDeniedHandler(accessDeniedHandler())
        .and()

        // Adding authorization config
        .authorizeHttpRequests()
        .antMatchers(HttpMethod.POST, WhitelistUrlManager.getWhitelistUrls())
        .permitAll()
        .antMatchers(HttpMethod.GET, WhitelistUrlManager.getWhitelistUrls())
        .permitAll()
        .antMatchers(WhitelistUrlManager.getUrls(AppConstants.SYSTEM.GOWAVE))
        .hasAuthority(AppConstants.ROLE.SYSTEM_USER)
        .antMatchers(WhitelistUrlManager.getUrls(AppConstants.SYSTEM.OPEN_BANKING_GATEWAY))
        .hasAuthority(WhitelistUrlManager.getRole(AppConstants.SYSTEM.OPEN_BANKING_GATEWAY))
        .anyRequest()
        .authenticated()
        .and()

        // Authentication provider
        .authenticationProvider(authenticationProvider())

        // Adding custom filters
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenManager),
            UsernamePasswordAuthenticationFilter.class);
  }

  private CorsConfiguration setupCORS(HttpServletRequest httpServletRequest) {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    // TODO temporary set allow origin to * for testing purpose
    corsConfiguration.setAllowedOrigins(List.of("*"));
    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    corsConfiguration.setAllowedHeaders(List.of("*"));
    corsConfiguration.setAllowCredentials(false);
    return corsConfiguration;
  }
}

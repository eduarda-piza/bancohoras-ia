package com.bancohoras.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // habilita @PreAuthorize / @PostAuthorize nos controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // -------------------------------------------------------------------------
    // Beans de autenticação
    // -------------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // -------------------------------------------------------------------------
    // Filtro de segurança HTTP
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())

            // Regras de autorização
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/health",
                    "/css/**", "/js/**", "/images/**", "/webjars/**",
                    "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
            )

            // Formulário de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")           // Spring Security processa o POST aqui
                .defaultSuccessUrl("/dashboard", true)  // sempre redireciona ao dashboard
                .failureUrl("/login?error=true")
                .usernameParameter("email")             // campo <input name="email">
                .passwordParameter("senha")             // campo <input name="senha">
                .permitAll()
            )

            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // CSRF: ignora apenas o console H2 (ferramenta de desenvolvimento)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )

            // Permite frames da mesma origem para o console H2
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}

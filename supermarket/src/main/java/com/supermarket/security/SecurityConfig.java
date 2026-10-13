package com.supermarket.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define users in-memory.
     * Default credentials: admin / admin123
     * Change these before deploying to production!
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var admin = User.builder()
                .username("Harshad")
                .password(encoder.encode("12345678"))
                .roles("ADMIN")
                .build();
        var cashier = User.builder()
                .username("cashier")
                .password(encoder.encode("cashier123"))
                .roles("CASHIER")
                .build();
        return new InMemoryUserDetailsManager(admin, cashier);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API endpoints (frontend uses fetch/JS)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/api/products/import")
            )
            .authorizeHttpRequests(auth -> auth
                // Public: login page + static assets (CSS, JS)
                .requestMatchers("/login", "/css/**", "/js/**", "/favicon.ico").permitAll()
                // Only ADMIN can manage products and view reports
                .requestMatchers("/api/products/**", "/api/dashboard/**").hasAnyRole("ADMIN", "CASHIER")
                // Billing and bill history for both roles
                .requestMatchers("/api/bills/**").hasAnyRole("ADMIN", "CASHIER")
                // Everything else requires login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")           // our custom Thymeleaf login page
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(rm -> rm.key("supermart-remember-me").tokenValiditySeconds(86400 * 7));

        return http.build();
    }
}

package hr.abysalto.hiring.mid.configuration;

import hr.abysalto.hiring.mid.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomUserDetailsService customUserDetailsService;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
				.requestMatchers("/swagger-ui/**", "/v3/api-docs*/**", "/h2-console/**");
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(new Customizer<CsrfConfigurer<HttpSecurity>>() {
					@Override
					public void customize(CsrfConfigurer<HttpSecurity> httpSecurityCsrfConfigurer) {
						httpSecurityCsrfConfigurer.disable();
					}
				}).authorizeHttpRequests(authorizeRequests ->
						authorizeRequests.requestMatchers("/swagger-ui/**").permitAll()
								.requestMatchers("/v3/api-docs*/**").permitAll()
								.requestMatchers("/h2-console/**").permitAll()
								.requestMatchers("/api/auth/register").permitAll()
								.requestMatchers("/api/auth/login").permitAll()
								.anyRequest().authenticated())
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // For H2 console
				.httpBasic(Customizer.withDefaults())
				.formLogin(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(customUserDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}
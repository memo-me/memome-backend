package cloud.memome.backend.infra.security.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import cloud.memome.backend.application.auth.RefreshTokenService;
import cloud.memome.backend.application.member.MemberService;
import cloud.memome.backend.infra.security.jwt.JwtFilter;
import cloud.memome.backend.infra.security.jwt.JwtLogoutHandler;
import cloud.memome.backend.infra.security.jwt.JwtProvider;
import cloud.memome.backend.infra.security.oidc.MyOidcService;
import cloud.memome.backend.infra.security.oidc.OidcSuccessHandler;

@Configuration
@EnableWebSecurity
public class AuthConfig {
	private final MemberService memberService;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;
	private final String frontendUrl;

	public AuthConfig(MemberService memberService, RefreshTokenService refreshTokenService, JwtProvider jwtProvider,
		@Value("${app.frontend-url}") String frontendUrl) {
		this.memberService = memberService;
		this.refreshTokenService = refreshTokenService;
		this.jwtProvider = jwtProvider;
		this.frontendUrl = frontendUrl;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
			.addFilterBefore(new JwtFilter(jwtProvider), LogoutFilter.class)
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors
				.configurationSource(corsConfigurationSource()))
			.logout(logout -> logout
				.addLogoutHandler(new JwtLogoutHandler(jwtProvider, refreshTokenService))
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
			.oauth2Login(oauth2 -> oauth2
				.successHandler(new OidcSuccessHandler(jwtProvider, refreshTokenService, frontendUrl))
				.userInfoEndpoint(userinfo -> userinfo
					.oidcUserService(oidcUserService())))
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.authorizeHttpRequests(request -> request
				.requestMatchers("/error/**", "/swagger-ui/**",
					"/v3/api-docs/**", "/oauth2/**", "/api/auth/refresh", "/api/health").permitAll()
				.anyRequest().authenticated()
			)
			.build();
	}

	@Bean
	public OidcUserService oidcUserService() {
		return new MyOidcService(this.memberService);
	}

	private UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// configuration.setAllowedOrigins(List.of("http://localhost:63342")); //index.html
		configuration.setAllowedOrigins(List.of(frontendUrl));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setMaxAge(Duration.ofHours(2));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;

	}
}

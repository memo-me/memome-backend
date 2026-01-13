package cloud.memome.backend.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;

import cloud.memome.backend.member.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthConfig {
	private final MemberService memberService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
			.authorizeHttpRequests(request -> request
				.requestMatchers("/error/**").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userinfo -> userinfo
					.oidcUserService(oidcUserService())))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((req, res, ex) ->
					res.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
			.build();
	}

	@Bean
	public OidcUserService oidcUserService() {
		return new MyOidcService(this.memberService);
	}
}

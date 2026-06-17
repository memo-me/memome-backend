package cloud.memome.backend.infra.security.jwt;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		//request에서 JWT 가져옴
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		//유효하지 않은 JWT -> 다음 필터 진행
		String jwt = authorization.substring("Bearer ".length());
		Claims claims;
		try {
			claims = jwtProvider.parseJwt(jwt);

			TokenType type = TokenType.valueOf(claims.get("type", String.class));
			if (type != TokenType.ACCESS) {
				throw new JwtException("token type이 access가 아님");
			}
		} catch (JwtException e) {
			filterChain.doFilter(request, response);
			return;
		}
		//유효한 JWT -> 인증 객체 저장 및 다음필터 진행
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			Long.valueOf(claims.getSubject()),
			null,
			Collections.emptyList()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
}

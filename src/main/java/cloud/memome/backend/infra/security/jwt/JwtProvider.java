package cloud.memome.backend.infra.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {
	private final SecretKey secretKey;
	private final String issuer;
	private final JwtParser jwtParser;

	public JwtProvider(@Value("${jwt.secret}") String secret, @Value("${jwt.issuer}") String issuer) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.issuer = issuer;
		this.jwtParser = Jwts.parser()
			.requireIssuer(issuer)
			.verifyWith(secretKey)
			.build();
	}

	public String createJwt(CreateJwtDto dto) {
		return Jwts.builder()
			.issuer(this.issuer)
			.subject(dto.getSubject().toString())
			.claim("type", dto.getType().toString())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis()
				+ dto.getType().getDurationToMilli()))
			.signWith(secretKey)
			.compact();
	}

	public Claims parseJwt(String jwt) {
		Claims claims = jwtParser.parseSignedClaims(jwt)
			.getPayload();
		if (claims.getSubject() == null || claims.get("type") == null) {
			throw new JwtException("subject와 type은 null 일 수 없음");
		}
		return claims;
	}
}

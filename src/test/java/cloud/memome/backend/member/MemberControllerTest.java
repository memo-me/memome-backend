package cloud.memome.backend.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.memome.backend.auth.AuthConfig;
import cloud.memome.backend.member.dto.IdentityDto;
import cloud.memome.backend.member.dto.UpdateMemberDto;
import cloud.memome.backend.member.exception.InvalidAuthenticationException;

@WebMvcTest(MemberController.class)
@Import(AuthConfig.class)
class MemberControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MemberService memberService;

	@Test
	@DisplayName("GET /members/me: 인증되지 않은 회원 접근(401)")
	public void getAccount_unauthorized() throws Exception {
		mockMvc.perform(get("/members/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /members/me: OIDC 로그인 회원 조회 성공(200)")
	public void getAccount_success() throws Exception {
		String nickname = "홍길동";
		String email = "test@test.com";

		Member member = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "12345678"), nickname, email);
		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(member);

		URL url = new URL(ProviderType.GOOGLE.getIssuer());

		mockMvc.perform(get("/members/me")
				.with(oidcLogin()
					.idToken(token -> token
						.claims(claims -> {
							claims.put("iss", url);
							claims.put("sub", "12345678");
							claims.put("name", nickname);
							claims.put("email", email);
						})
					)
				)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("nickname").value(nickname))
			.andExpect(jsonPath("email").value(email));
	}

	@Test
	@DisplayName("GET /members/me: 인증정보가 유효하지 않을 때 인증 회원정보 조회 실패(401)")
	public void getAccount_fail_when_authentication_is_invalid() throws Exception {
		String nickname = "홍길동";
		String email = "test@test.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "12345678");
		OAuthIdentity invalidOAuthIdentity = new OAuthIdentity(ProviderType.KAKAO, oAuthIdentity.getProviderId());
		Assertions.assertThat(invalidOAuthIdentity).isNotEqualTo(oAuthIdentity);

		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenThrow(new InvalidAuthenticationException(invalidOAuthIdentity));

		mockMvc.perform(get("/members/me")
				.with(oidcLogin()
					.idToken(token -> token
						.claims(claims -> {
							claims.put("iss", changeProviderTypeToURL(invalidOAuthIdentity.getProviderType()));
							claims.put("sub", invalidOAuthIdentity.getProviderId());
							claims.put("name", nickname);
							claims.put("email", email);
						})
					)
				)
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Unauthorized"))
			.andExpect(jsonPath("status").value(HttpStatus.UNAUTHORIZED.value()))
			.andExpect(jsonPath("detail").exists());
	}

	@Test
	@DisplayName("PUT /members/me: 인증되지 않은 회원 접근: 401")
	public void updateAccount_unauthorized() throws Exception {
		mockMvc.perform(put("/members/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("PUT /members/me: 인증된 회원 정보 수정: 200")
	public void updateAccount_success() throws Exception {
		String nickname = "홍길동";
		String email = "test@test.com";

		String updated_nickname = "고길동";
		String updated_email = "고길동@test.com";

		Member updated_member = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "12345678"), updated_nickname,
			updated_email);

		when(memberService.updateMember(any(IdentityDto.class), any(UpdateMemberDto.class)))
			.thenReturn(updated_member);

		URL url = new URL(ProviderType.GOOGLE.getIssuer());

		UpdateMemberDto dto = new UpdateMemberDto(updated_nickname, updated_email);
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))
				.with(oidcLogin()
					.idToken(token -> token
						.claims(claims -> {
							claims.put("iss", url);
							claims.put("sub", "12345678");
							claims.put("name", nickname);
							claims.put("email", email);
						})
					)
				)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("nickname").value(updated_nickname))
			.andExpect(jsonPath("email").value(updated_email))
			.andExpect(jsonPath("createdAt").exists())
			.andExpect(jsonPath("updatedAt").exists());
	}

	//TODO: PUT /members/me 존재하지 않는 회원 정보 수정
	@Test
	@DisplayName("PUT /members/me: 인증정보가 유효하지 않을 때 인증 회원정보 수정 실패(401)")
	public void updateAccount_fail_when_authentication_is_invalid() throws Exception {
		String nickname = "홍길동";
		String email = "test@test.com";

		String updated_nickname = "고길동";
		String updated_email = "고길동@test.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "12345678");
		OAuthIdentity invalidOAuthIdentity = new OAuthIdentity(ProviderType.KAKAO, oAuthIdentity.getProviderId());
		Assertions.assertThat(invalidOAuthIdentity).isNotEqualTo(oAuthIdentity);

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		when(memberService.updateMember(any(IdentityDto.class), any(UpdateMemberDto.class)))
			.thenThrow(new InvalidAuthenticationException(invalidOAuthIdentity));

		mockMvc.perform(put("/members/me")
				.with(oidcLogin()
					.idToken(token -> token
						.claims(claims -> {
							claims.put("iss", changeProviderTypeToURL(invalidOAuthIdentity.getProviderType()));
							claims.put("sub", invalidOAuthIdentity.getProviderId());
							claims.put("name", nickname);
							claims.put("email", email);
						})
					)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Unauthorized"))
			.andExpect(jsonPath("status").value(HttpStatus.UNAUTHORIZED.value()))
			.andExpect(jsonPath("detail").exists());
	}

	@Test
	@DisplayName("PUT /members/me: nickname이 blank 일 때 검증실패: 400")
	public void updateAccount_fail_when_nickname_is_blank() throws Exception {
		//given
		String nickname = "홍길동";
		String email = "test@test.com";

		String updated_nickname = "          ";
		String updated_email = "고길동@test.com";

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		URL url = new URL(ProviderType.GOOGLE.getIssuer());
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
				.with(oidcLogin()
					.idToken(token -> token
						.claims(claims -> {
							claims.put("iss", url);
							claims.put("sub", "12345678");
							claims.put("name", nickname);
							claims.put("email", email);
						}))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("nickname")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: email이 blank 일 때 검증실패: 400")
	public void updateAccount_fail_when_email_is_blank() throws Exception {
		//given
		String nickname = "홍길동";
		String email = "test@test.com";

		String updated_nickname = "변경된 닉네임";
		String updated_email = "        ";

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		URL url = new URL(ProviderType.GOOGLE.getIssuer());
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
				.with(oidcLogin()
					.idToken(token -> token
						.claims(claims -> {
							claims.put("iss", url);
							claims.put("sub", "12345678");
							claims.put("name", nickname);
							claims.put("email", email);
						}))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: email이 이메일 형식이 아닐 때 검증실패: 400")
	public void updateAccount_fail_when_email_is_well_formed() throws Exception {
		//given
		String nickname = "홍길동";
		String email = "test@test.com";

		String updated_nickname = "변경된 닉네임";
		String updated_email = "잘못된 이메일 형식";

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		URL url = new URL(ProviderType.GOOGLE.getIssuer());
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
				.with(oidcLogin()
					.idToken(token -> token
						.claims(claims -> setClaims(claims, ProviderType.GOOGLE, "12345678", nickname, email)))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists())
			.andDo(print());
	}

	@Test
	@DisplayName("DELETE /members/me: 인증되지 않은 회원 접근: 401")
	public void deleteAccount_unauthorized() throws Exception {
		mockMvc.perform(delete("/members/me"))
			.andExpect(status().isUnauthorized());
	}

	private URL changeProviderTypeToURL(ProviderType type) {
		URL url;
		try {
			url = new URL(type.getIssuer());
		} catch (Exception e) {
			throw new RuntimeException("WRONG URL FORMAT: " + type.getIssuer());
		}

		return url;
	}

	private Map<String, Object> setClaims(Map<String, Object> claims, ProviderType type, String providerId,
		String nickname, String email) {
		claims.put("iss", changeProviderTypeToURL(type));
		claims.put("sub", providerId);
		claims.put("name", nickname);
		claims.put("email", email);
		return claims;
	}
}
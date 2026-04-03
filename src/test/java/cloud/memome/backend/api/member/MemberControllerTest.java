package cloud.memome.backend.api.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

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

import cloud.memome.backend.api.OidcTestUtils;
import cloud.memome.backend.application.member.MemberService;
import cloud.memome.backend.application.member.dto.IdentityDto;
import cloud.memome.backend.application.member.dto.UpdateMemberDto;
import cloud.memome.backend.application.member.exception.InvalidAuthenticationException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.member.ProviderType;
import cloud.memome.backend.infra.security.config.AuthConfig;

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
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String nickname = "홍길동";
		String email = "test@test.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);

		Member member = Member.create(oAuthIdentity, nickname, email);
		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(member);

		//when & then
		mockMvc.perform(get("/members/me")
				.with(OidcTestUtils.login(oAuthIdentity)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("nickname").value(nickname))
			.andExpect(jsonPath("email").value(email));
	}

	@Test
	@DisplayName("GET /members/me: 인증정보가 유효하지 않을 때 인증 회원정보 조회 실패(401)")
	public void getAccount_fail_when_authentication_is_invalid() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";

		OAuthIdentity invalidOAuthIdentity = new OAuthIdentity(providerType, providerId);

		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenThrow(new InvalidAuthenticationException());

		mockMvc.perform(get("/members/me")
				.with(OidcTestUtils.login(invalidOAuthIdentity)))
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
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String updated_nickname = "고길동";
		String updated_email = "고길동@test.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);
		Member updated_member = Member.create(oAuthIdentity, updated_nickname, updated_email);
		when(memberService.updateMember(any(IdentityDto.class), any(UpdateMemberDto.class)))
			.thenReturn(updated_member);

		UpdateMemberDto dto = new UpdateMemberDto(updated_nickname, updated_email);

		//when && then
		mockMvc.perform(put("/members/me")
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("nickname").value(updated_nickname))
			.andExpect(jsonPath("email").value(updated_email))
			.andExpect(jsonPath("createdAt").exists())
			.andExpect(jsonPath("updatedAt").exists());
	}

	@Test
	@DisplayName("PUT /members/me: 인증정보가 유효하지 않을 때 인증 회원정보 수정 실패(401)")
	public void updateAccount_fail_when_authentication_is_invalid() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String updated_nickname = "고길동";
		String updated_email = "고길동@test.com";

		OAuthIdentity invalidOAuthIdentity = new OAuthIdentity(providerType, providerId);
		when(memberService.updateMember(any(IdentityDto.class), any(UpdateMemberDto.class)))
			.thenThrow(new InvalidAuthenticationException());

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		mockMvc.perform(put("/members/me")
				.with(OidcTestUtils.login(invalidOAuthIdentity))
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
	@DisplayName("PUT /members/me: nickname이 null 일 때 검증실패: 400")
	public void updateAccount_fail_when_nickname_is_null() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String updated_nickname = null;
		String updated_email = "고길동@test.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		mockMvc.perform(put("/members/me")
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("nickname")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: nickname이 blank 일 때 검증실패: 400")
	public void updateAccount_fail_when_nickname_is_blank() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String updated_nickname = "          ";
		String updated_email = "고길동@test.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		mockMvc.perform(put("/members/me")
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("nickname")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: email이 null일 때 검증실패: 400")
	public void updateAccount_fail_when_email_is_null() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String updated_nickname = "변경된 닉네임";
		String updated_email = null;

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		mockMvc.perform(put("/members/me")
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: email이 blank 일 때 검증실패: 400")
	public void updateAccount_fail_when_email_is_blank() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String updated_nickname = "변경된 닉네임";
		String updated_email = "        ";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		mockMvc.perform(put("/members/me")
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: email이 이메일 형식이 아닐 때 검증실패: 400")
	public void updateAccount_fail_when_email_format_is_invalid() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String updated_nickname = "변경된 닉네임";
		String updated_email = "잘못된 이메일 형식";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);

		Map<String, String> body = new HashMap<>();
		body.put("nickname", updated_nickname);
		body.put("email", updated_email);

		//when && then
		mockMvc.perform(put("/members/me")
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Bad Request"))
			.andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("DELETE /members/me: 인증되지 않은 회원 접근: 401")
	public void deleteAccount_unauthorized() throws Exception {
		mockMvc.perform(delete("/members/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("DELETE /members/me: 인증정보가 유효하지 않을 때 계정 삭제 실패: 401")
	public void deleteAccount_fail_when_account() throws Exception {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";

		OAuthIdentity invalidOAuthIdentity = new OAuthIdentity(providerType, providerId);
		doThrow(new InvalidAuthenticationException())
			.when(memberService).removeMember(any(IdentityDto.class));

		mockMvc.perform(delete("/members/me")
				.with(OidcTestUtils.login(invalidOAuthIdentity)))
			.andExpect(status().isUnauthorized())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("type").value("about:blank"))
			.andExpect(jsonPath("title").value("Unauthorized"))
			.andExpect(jsonPath("status").value(HttpStatus.UNAUTHORIZED.value()))
			.andExpect(jsonPath("detail").exists());
	}
}
package cloud.memome.backend.member;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URL;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.memome.backend.auth.AuthConfig;
import cloud.memome.backend.member.dto.IdentityDto;
import cloud.memome.backend.member.dto.UpdateMemberDto;

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

	@Test
	@DisplayName("DELETE /members/me: 인증되지 않은 회원 접근: 401")
	public void deleteAccount_unauthorized() throws Exception {
		mockMvc.perform(delete("/members/me"))
			.andExpect(status().isUnauthorized());
	}
}
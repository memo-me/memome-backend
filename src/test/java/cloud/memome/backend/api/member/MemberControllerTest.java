package cloud.memome.backend.api.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.memome.backend.api.TestConfiguration;
import cloud.memome.backend.api.member.request.UpdateMyInfoRequest;
import cloud.memome.backend.application.member.MemberService;
import cloud.memome.backend.application.member.dto.UpdateMemberDto;
import cloud.memome.backend.application.member.exception.NoSuchMemberException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.member.ProviderType;

@WebMvcTest(MemberController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MemberService memberService;

	private static final Long LOGIN_MEMBER_ID = 1L;

	@Test
	@DisplayName("GET /members/me: 로그인 회원의 정보 반환")
	public void getAccountInfo() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);

		when(memberService.getMemberById(member.getId()))
			.thenReturn(member);

		//when && then
		mockMvc.perform(get("/members/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nickname").value(member.getNickname()))
			.andExpect(jsonPath("$.email").value(member.getEmail()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists());

		verify(memberService).getMemberById(member.getId());
	}

	@Test
	@DisplayName("GET /members/me: 로그인한 회원이 존재하지 않으면 404 반환")
	public void getAccountInfo_whenMemberDoesNotExist() throws Exception {
		//given
		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenThrow(new NoSuchMemberException());

		//when && then
		mockMvc.perform(get("/members/me"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/members/me"));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
	}

	@Test
	@DisplayName("PUT /members/me: 로그인한 회원의 정보 수정")
	public void updateAccountInfo() throws Exception {
		//given
		Member updatedMember = createMemberWithId(LOGIN_MEMBER_ID);
		UpdateMemberDto updateMemberDto =
			new UpdateMemberDto(updatedMember.getNickname(), updatedMember.getEmail());

		when(memberService.updateMember(updatedMember.getId(), updateMemberDto))
			.thenReturn(updatedMember);

		UpdateMyInfoRequest request = new UpdateMyInfoRequest(updatedMember.getNickname(),
			updatedMember.getEmail());

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nickname").value(updatedMember.getNickname()))
			.andExpect(jsonPath("$.email").value(updatedMember.getEmail()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists());

		verify(memberService).updateMember(updatedMember.getId(), updateMemberDto);
	}

	@Test
	@DisplayName("PUT /members/me: 존재하지 않는 회원의 정보를 수정할 때 404 반환")
	public void updateAccountInfo_whenMemberDoesNotExist() throws Exception {
		//given
		Member updatedMember = createMemberWithId(LOGIN_MEMBER_ID);
		UpdateMemberDto updateMemberDto =
			new UpdateMemberDto(updatedMember.getNickname(), updatedMember.getEmail());

		when(memberService.updateMember(updatedMember.getId(), updateMemberDto))
			.thenThrow(new NoSuchMemberException());

		UpdateMyInfoRequest request = new UpdateMyInfoRequest(updatedMember.getNickname(),
			updatedMember.getEmail());

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/members/me"));

		verify(memberService).updateMember(updatedMember.getId(), updateMemberDto);
	}

	@Test
	@DisplayName("PUT /members/me: 닉네임을 null로 수정할 때 400 반환")
	public void updateAccountInfo_whenNicknameIsNull() throws Exception {
		//given
		UpdateMyInfoRequest request = new UpdateMyInfoRequest(null, "test@test.com");

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/members/me"))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("nickname")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: 이메일을 null로 수정할 때 400 반환")
	public void updateAccountInfo_whenEmailIsNull() throws Exception {
		//given
		UpdateMyInfoRequest request = new UpdateMyInfoRequest("updated", null);

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/members/me"))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: 닉네임과 이메일을 null로 수정할 때 400 반환")
	public void updateAccountInfo_whenNicknameAndEmailIsNull() throws Exception {
		//given
		UpdateMyInfoRequest request = new UpdateMyInfoRequest(null, null);

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/members/me"))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("nickname")))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: 닉네임을 빈문자열로 수정할 때 400 반환")
	public void updateAccountInfo_whenNicknameIsBlank() throws Exception {
		//given
		UpdateMyInfoRequest request = new UpdateMyInfoRequest("   ", "test@test.com");

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/members/me"))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("nickname")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: 이메일을 빈문자열로 수정할 때 400 반환")
	public void updateAccountInfo_whenEmailIsBlank() throws Exception {
		//given
		UpdateMyInfoRequest request = new UpdateMyInfoRequest("updated", "     ");

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/members/me"))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /members/me: 이메일 형식을 따르지 않은 이메일로 수정할 때 400 반환")
	public void updateAccountInfo_whenEmailIsNotRegex() throws Exception {
		//given
		UpdateMyInfoRequest request = new UpdateMyInfoRequest("updated", "github.com");

		//when && then
		mockMvc.perform(put("/members/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/members/me"))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("DELETE /members/me: 로그인 회원 탈퇴")
	public void deleteAccount() throws Exception {
		//given

		//when && then
		mockMvc.perform(delete("/members/me"))
			.andExpect(status().isNoContent());

		verify(memberService).removeMember(LOGIN_MEMBER_ID);
	}

	@Test
	@DisplayName("DELETE /members/me: 존재하지 않는 회원 탈퇴 시도 시, 404 반환")
	public void deleteAccount_whenMemberDoesNotExist() throws Exception {
		//given
		doThrow(new NoSuchMemberException())
			.when(memberService).removeMember(LOGIN_MEMBER_ID);

		//when && then
		mockMvc.perform(delete("/members/me"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/members/me"));

		verify(memberService).removeMember(LOGIN_MEMBER_ID);
	}

	private Member createMemberWithId(Long id) {
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "12345678";
		String nickname = "홍길동";
		String email = "test@test.com";

		Member member = Member.create(new OAuthIdentity(providerType, providerId), nickname, email);
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}
}
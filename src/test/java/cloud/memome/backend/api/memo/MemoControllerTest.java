package cloud.memome.backend.api.memo;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.memome.backend.api.OidcTestUtils;
import cloud.memome.backend.api.memo.request.ModifyMemoRequest;
import cloud.memome.backend.application.member.MemberService;
import cloud.memome.backend.application.member.dto.IdentityDto;
import cloud.memome.backend.application.memo.MemoService;
import cloud.memome.backend.application.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.application.memo.dto.RemoveMemoDto;
import cloud.memome.backend.application.memo.exception.MemoNotFoundException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.member.ProviderType;
import cloud.memome.backend.domain.memo.Memo;
import cloud.memome.backend.infra.security.config.AuthConfig;

@WebMvcTest(MemoController.class)
@Import(AuthConfig.class)
class MemoControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private MemoService memoService;
	@MockitoBean
	private MemberService memberService;
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("GET /memos: 인증되지 않은 접근 (401)")
	public void getMemoSummaryList_unauthorized() throws Exception {
		mockMvc.perform(get("/memos"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /memos: 회원이 작성한 모든 메모 요약 조회 성공 (200)")
	public void getMemoSummaryList_success() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member member = Member.create(oAuthIdentity, "nickname", "email@email.com");
		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(member);

		Memo memo1 = Memo.create("title1", "This is body1.", member);
		Memo memo2 = Memo.create("title2", "body2.", member);
		when(memoService.getOwnedMemosAll(any(Member.class)))
			.thenReturn(List.of(memo1, memo2));

		//when & then
		mockMvc.perform(get("/memos").with(
				OidcTestUtils.login(oAuthIdentity)
			))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.count").value(2))
			.andExpect(jsonPath("$.memoSummaryList.length()").value(2))
			.andExpect(jsonPath("$.memoSummaryList[*].id").exists())
			.andExpect(jsonPath("$.memoSummaryList[*].title").exists())
			.andExpect(jsonPath("$.memoSummaryList[*].bodySummary").exists())
			.andExpect(jsonPath("$.memoSummaryList[*].createdAt").exists())
			.andExpect(jsonPath("$.memoSummaryList[*].updatedAt").exists());
	}

	@Test
	@DisplayName("GET /memos: 회원이 작성한 메모 없을 때, 모든 메모 요약 조회 성공 (200)")
	public void getMemoSummaryList_success_when_memo_not_exist() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(Member.create(oAuthIdentity, "nickname", "email@email.com"));
		when(memoService.getOwnedMemosAll(any(Member.class)))
			.thenReturn(List.of());

		//when & then
		mockMvc.perform(get("/memos").with(
				OidcTestUtils.login(oAuthIdentity)
			))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.count").value(0))
			.andExpect(jsonPath("$.memoSummaryList").isEmpty());
	}

	@Test
	@DisplayName("GET /memos/{id}: 인증되지 않은 접근 (401)")
	public void getMemo_unauthorized() throws Exception {
		Long memoId = 1L;

		mockMvc.perform(get("/memos/{id}", memoId))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /memos/{id}: 회원이 작성한 특정 메모 조회 (200)")
	public void getMemo_success() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member member = Member.create(oAuthIdentity, "nickname", "email@email.com");
		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(member);

		Long memo1Id = 26L;
		Memo memo1 = Memo.create("title1", "This is body1.", member);
		ReflectionTestUtils.setField(memo1, "id", memo1Id);
		when(memoService.getOwnedMemo(new GetOwnedMemoDto(memo1Id, member)))
			.thenReturn(memo1);

		//when & then
		mockMvc.perform(get("/memos/{id}", memo1Id).with(
				OidcTestUtils.login(oAuthIdentity)
			))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(memo1.getId()))
			.andExpect(jsonPath("$.title").value(memo1.getTitle()))
			.andExpect(jsonPath("$.body").value(memo1.getBody()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists());
	}

	@Test
	@DisplayName("GET /memos/{id}: 다른 회원이 작성한 특정 메모 조회 (404)")
	public void getMemo_fail_when_not_my_memo() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member member = Member.create(oAuthIdentity, "nickname", "email@email.com");
		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(member);

		Long memoIdNotMine = 26L;
		when(memoService.getOwnedMemo(new GetOwnedMemoDto(memoIdNotMine, member)))
			.thenThrow(new MemoNotFoundException());

		//when & then
		mockMvc.perform(get("/memos/{id}", memoIdNotMine).with(
				OidcTestUtils.login(oAuthIdentity)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").exists());
	}

	@Test
	@DisplayName("GET /memos/{id}: 존재하지 않는 메모 조회 (404)")
	public void getMemo_fail_when_not_exist() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member member = Member.create(oAuthIdentity, "nickname", "email@email.com");
		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(member);

		Long notExistMemoId = 26L;
		when(memoService.getOwnedMemo(new GetOwnedMemoDto(notExistMemoId, member)))
			.thenThrow(new MemoNotFoundException());

		//when & then
		mockMvc.perform(get("/memos/{id}", notExistMemoId).with(
				OidcTestUtils.login(oAuthIdentity)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").exists());
	}

	@Test
	@DisplayName("PUT /memos/{id}: 비로그인 상태 접근 401")
	public void modifyMemo_fail_when_unauthorize() throws Exception {
		//given
		Long memoId = 1L;

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 수정 성공")
	public void modifyMemo_success() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member member = Member.create(oAuthIdentity, "nickname", "email@email.com");

		Long memoId = 1L;

		ModifyMemoRequest request = new ModifyMemoRequest("updated title", "updated content");
		Memo updatedMemo = Memo.create(request.getTitle(), request.getBody(), member);
		ReflectionTestUtils.setField(updatedMemo, "id", memoId);

		when(memberService.getMemberByIdentity(any()))
			.thenReturn(member);
		when(memoService.updateMemo(any()))
			.thenReturn(updatedMemo);

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpectAll(
				status().isOk(),
				jsonPath("$.id").value(updatedMemo.getId()),
				jsonPath("$.title").value(updatedMemo.getTitle()),
				jsonPath("$.body").value(updatedMemo.getBody()),
				jsonPath("$.createdAt").exists(),
				jsonPath("$.updatedAt").exists()
			);
	}

	@Test
	@DisplayName("PUT /memos/{id}: 존재하지 않는 메모 수정 시도 시 실패")
	public void modifyMemo_fail_when_not_exist() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member member = Member.create(oAuthIdentity, "nickname", "email@email.com");

		Long memoId = 1L;

		ModifyMemoRequest request = new ModifyMemoRequest("updated title", "updated content");
		Memo updatedMemo = Memo.create(request.getTitle(), request.getBody(), member);
		ReflectionTestUtils.setField(updatedMemo, "id", memoId);

		when(memberService.getMemberByIdentity(any()))
			.thenReturn(member);
		when(memoService.updateMemo(any()))
			.thenThrow(new MemoNotFoundException());

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.with(OidcTestUtils.login(oAuthIdentity))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").exists());
	}

	@Test
	@DisplayName("PUT /memos/{id}: 다른 사람의 메모 수정 시도 시 실패")
	public void modifyMemo_fail_when_not_mine() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member author = Member.create(oAuthIdentity, "nickname", "email@email.com");
		Member notAuthor = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "not author"),
			"nickname123", "email123@email.com");

		Long memoId = 1L;

		ModifyMemoRequest request = new ModifyMemoRequest("updated title", "updated content");
		Memo updatedMemo = Memo.create(request.getTitle(), request.getBody(), author);
		ReflectionTestUtils.setField(updatedMemo, "id", memoId);

		when(memberService.getMemberByIdentity(any()))
			.thenReturn(notAuthor);
		when(memoService.updateMemo(any()))
			.thenThrow(new MemoNotFoundException());

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.with(OidcTestUtils.login(notAuthor.getOAuthIdentity()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").exists());
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 제목을 null로 수정 시 실패")
	public void modifyMemo_fail_when_title_is_null() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member author = Member.create(oAuthIdentity, "nickname", "email@email.com");

		Long memoId = 1L;

		ModifyMemoRequest request = new ModifyMemoRequest(null, "updated content");

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.with(OidcTestUtils.login(author.getOAuthIdentity()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("title")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 제목을 빈문자열로 수정 시 실패")
	public void modifyMemo_fail_when_title_is_empty() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member author = Member.create(oAuthIdentity, "nickname", "email@email.com");

		Long memoId = 1L;

		ModifyMemoRequest request = new ModifyMemoRequest("     ", "updated content");

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.with(OidcTestUtils.login(author.getOAuthIdentity()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("title")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 본문을 null로 수정 시 실패")
	public void modifyMemo_fail_when_body_is_null() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member author = Member.create(oAuthIdentity, "nickname", "email@email.com");

		Long memoId = 1L;

		ModifyMemoRequest request = new ModifyMemoRequest("updated title", null);

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.with(OidcTestUtils.login(author.getOAuthIdentity()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("body")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 본문을 빈문자열로 수정 시 실패")
	public void modifyMemo_fail_when_body_is_empty() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member author = Member.create(oAuthIdentity, "nickname", "email@email.com");

		Long memoId = 1L;

		ModifyMemoRequest request = new ModifyMemoRequest("updated title", "         ");

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.with(OidcTestUtils.login(author.getOAuthIdentity()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.errors[*].field").value(hasItem("body")))
			.andExpect(jsonPath("$.errors[*].message").exists());
	}

	@Test
	@DisplayName("DELETE /memos/{id}: 비로그인 상태 접근 시 실패")
	public void deleteMemo_when_unauthorized() throws Exception {
		Long memoId = 1L;

		//when && then
		mockMvc.perform(delete("/memos/{id}", memoId))
			.andExpectAll(
				status().isUnauthorized()
			);
	}

	@Test
	@DisplayName("DELETE /memos/{id}: 특정 메모 삭제 성공(204)")
	public void deleteMemo_success() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member author = Member.create(oAuthIdentity, "nickname", "email@email.com");
		Long memoId = 1L;

		when(memberService.getMemberByIdentity(any()))
			.thenReturn(author);
		doNothing()
			.when(memoService).removeMemo(any(RemoveMemoDto.class));

		//when && then
		mockMvc.perform(delete("/memos/{id}", memoId).with(
				OidcTestUtils.login(oAuthIdentity)))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));
	}

	@Test
	@DisplayName("DELETE /memos/{id}: 존재하지 않는 메모 삭제 시 실패")
	public void deleteMemo_fail_when_not_exist() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member author = Member.create(oAuthIdentity, "nickname", "email@email.com");
		Long memoId = 1L;

		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(author);
		doThrow(new MemoNotFoundException())
			.when(memoService).removeMemo(any());

		//when && then
		mockMvc.perform(delete("/memos/{id}", memoId).with(
				OidcTestUtils.login(oAuthIdentity)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").exists());
	}

	@Test
	@DisplayName("DELETE /memos/{id}: 다른 사용자의 메모 삭제 요청 시 실패")
	public void deleteMemo_fail_when_not_mine() throws Exception {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "0123456789");
		Member notAuthor = Member.create(oAuthIdentity, "nickname", "email@email.com");
		Long memoId = 1L;

		when(memberService.getMemberByIdentity(any(IdentityDto.class)))
			.thenReturn(notAuthor);
		doThrow(new MemoNotFoundException())
			.when(memoService).removeMemo(any(RemoveMemoDto.class));

		//when && then
		mockMvc.perform(delete("/memos/{id}", memoId).with(
				OidcTestUtils.login(oAuthIdentity)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").exists());
	}
}
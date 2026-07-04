package cloud.memome.backend.api.memo;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

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
import cloud.memome.backend.api.memo.request.ModifyMemoRequest;
import cloud.memome.backend.api.memo.request.WriteNewMemoRequest;
import cloud.memome.backend.api.memo.response.MemoListResponse;
import cloud.memome.backend.application.member.MemberService;
import cloud.memome.backend.application.member.exception.NoSuchMemberException;
import cloud.memome.backend.application.memo.MemoService;
import cloud.memome.backend.application.memo.dto.CreateMemoDto;
import cloud.memome.backend.application.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.application.memo.dto.RemoveMemoDto;
import cloud.memome.backend.application.memo.dto.UpdateMemoDto;
import cloud.memome.backend.application.memo.exception.MemoNotFoundException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.member.ProviderType;
import cloud.memome.backend.domain.memo.Memo;

@WebMvcTest(MemoController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class MemoControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MemoService memoService;
	@MockitoBean
	private MemberService memberService;

	private final static Long LOGIN_MEMBER_ID = 1L;

	@Test
	@DisplayName("GET /memos: 로그인한 회원이 작성한 모든 메모 요약 리스트 조회")
	public void getMemoSummaryList() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		List<Memo> memoList = createMemoListWithMember(member);

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);
		when(memoService.getOwnedMemosAll(member))
			.thenReturn(memoList);

		//when & then
		MemoListResponse.MemoSummary firstSummary = new MemoListResponse.MemoSummary(memoList.getFirst());
		mockMvc.perform(get("/memos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.count").value(memoList.size()))
			.andExpect(jsonPath("$.memoSummaryList.length()").value(memoList.size()))
			.andExpect(jsonPath("$.memoSummaryList[0].id").value(firstSummary.getId()))
			.andExpect(jsonPath("$.memoSummaryList[0].title").value(firstSummary.getTitle()))
			.andExpect(jsonPath("$.memoSummaryList[0].bodySummary").value(firstSummary.getBodySummary()))
			.andExpect(jsonPath("$.memoSummaryList[0].createdAt").exists())
			.andExpect(jsonPath("$.memoSummaryList[0].updatedAt").exists());

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).getOwnedMemosAll(member);
	}

	@Test
	@DisplayName("GET /memos: 로그인한 회원이 작성한 모든 메모 요약 리스트 조회 시 메모가 없는 경우 200 반환")
	public void getMemoSummaryList_whenMemoDoesNotExist() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		List<Memo> memoList = List.of();

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);
		when(memoService.getOwnedMemosAll(member))
			.thenReturn(memoList);

		//when & then
		mockMvc.perform(get("/memos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.count").value(memoList.size()))
			.andExpect(jsonPath("$.memoSummaryList.length()").value(memoList.size()));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).getOwnedMemosAll(member);
	}

	@Test
	@DisplayName("GET /memos: 로그인한 회원이 작성한 모든 메모 요약 리스트 조회 시 회원이 존재하지 않는 경우 404 반환")
	public void getMemoSummaryList_whenMemberDoesNotExist() throws Exception {
		//given
		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenThrow(new NoSuchMemberException());

		//when & then
		mockMvc.perform(get("/memos"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos"));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).getOwnedMemosAll(any(Member.class));
	}

	@Test
	@DisplayName("POST /memos/{id}: 특정 메모 작성")
	public void writeNewMemo() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo memo = createMemoWithIdAndMember(1L, member);

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);
		CreateMemoDto dto = new CreateMemoDto(memo.getTitle(), memo.getBody(), member);
		when(memoService.createNewMemo(dto))
			.thenReturn(memo);

		WriteNewMemoRequest request = new WriteNewMemoRequest(memo.getTitle(), memo.getBody());

		//when & then
		mockMvc.perform(post("/memos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(memo.getId()))
			.andExpect(jsonPath("$.title").value(memo.getTitle()))
			.andExpect(jsonPath("$.body").value(memo.getBody()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists());

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).createNewMemo(dto);
	}

	@Test
	@DisplayName("POST /memos: 존재하지 않는 회원이 메모 작성 시 404 반환")
	public void writeNewMemo_whenMemberDoesNotExist() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo memo = createMemoWithIdAndMember(1L, member);

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenThrow(new NoSuchMemberException());

		WriteNewMemoRequest request = new WriteNewMemoRequest(memo.getTitle(), memo.getBody());

		//when & then
		mockMvc.perform(post("/memos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos"));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).createNewMemo(any(CreateMemoDto.class));
	}

	@Test
	@DisplayName("GET /memos/{id}: 특정 메모 조회")
	public void getMemo() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo memo = createMemoWithIdAndMember(1L, member);

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);

		GetOwnedMemoDto dto = new GetOwnedMemoDto(memo.getId(), member);
		when(memoService.getOwnedMemo(dto))
			.thenReturn(memo);

		//when & then
		mockMvc.perform(get("/memos/{id}", memo.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(memo.getId()))
			.andExpect(jsonPath("$.title").value(memo.getTitle()))
			.andExpect(jsonPath("$.body").value(memo.getBody()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists());

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).getOwnedMemo(dto);
	}

	@Test
	@DisplayName("GET /memos/{id}: 존재하지 않는 메모 조회 시 404 반환")
	public void getMemo_whenMemoDoesNotExist() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo memo = createMemoWithIdAndMember(1L, member);

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);

		GetOwnedMemoDto dto = new GetOwnedMemoDto(memo.getId(), member);
		when(memoService.getOwnedMemo(dto))
			.thenThrow(new MemoNotFoundException());

		//when & then
		mockMvc.perform(get("/memos/{id}", memo.getId()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new MemoNotFoundException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos/" + memo.getId()));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).getOwnedMemo(dto);
	}

	@Test
	@DisplayName("GET /memos/{id}: 존재하지 않는 회원이 메모 조회 시 404 반환")
	public void getMemo_whenMemberDoesNotExist() throws Exception {
		//given
		Long memoId = 1L;

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenThrow(new NoSuchMemberException());

		//when & then
		mockMvc.perform(get("/memos/{id}", memoId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos/" + memoId));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).getOwnedMemo(any(GetOwnedMemoDto.class));
	}

	@Test
	@DisplayName("PUT /memos/{id}: 특정 메모 수정")
	public void modifyMemo() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo updatedMemo = createMemoWithIdAndMember(1L, member);

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);

		UpdateMemoDto dto = new UpdateMemoDto(updatedMemo.getId(), member, updatedMemo.getTitle(),
			updatedMemo.getBody());
		when(memoService.updateMemo(dto))
			.thenReturn(updatedMemo);

		ModifyMemoRequest request = new ModifyMemoRequest(updatedMemo.getTitle(), updatedMemo.getBody());

		//when & then
		mockMvc.perform(put("/memos/{id}", updatedMemo.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(updatedMemo.getId()))
			.andExpect(jsonPath("$.title").value(updatedMemo.getTitle()))
			.andExpect(jsonPath("$.body").value(updatedMemo.getBody()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists());

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).updateMemo(dto);
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 제목을 null로 수정 시 400 반환")
	public void modifyMemo_whenTitleIsNull() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo updatedMemo = createMemoWithIdAndMember(1L, member);
		ReflectionTestUtils.setField(updatedMemo, "title", null);

		ModifyMemoRequest request = new ModifyMemoRequest(updatedMemo.getTitle(), updatedMemo.getBody());

		//when & then
		mockMvc.perform(put("/memos/{id}", updatedMemo.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/memos/" + updatedMemo.getId()))
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[*].field").value("title"))
			.andExpect(jsonPath("$.errors[*].message").exists());

		verify(memberService, never()).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).updateMemo(any(UpdateMemoDto.class));
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 제목을 빈문자열로 수정 시 400 반환")
	public void modifyMemo_whenTitleIsBlank() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo updatedMemo = createMemoWithIdAndMember(1L, member);
		ReflectionTestUtils.setField(updatedMemo, "title", "     ");

		ModifyMemoRequest request = new ModifyMemoRequest(updatedMemo.getTitle(), updatedMemo.getBody());

		//when & then
		mockMvc.perform(put("/memos/{id}", updatedMemo.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/memos/" + updatedMemo.getId()))
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[*].field").value("title"))
			.andExpect(jsonPath("$.errors[*].message").exists());

		verify(memberService, never()).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).updateMemo(any(UpdateMemoDto.class));
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 본문을 null로 수정 시 400 반환")
	public void modifyMemo_whenBodyIsNull() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo updatedMemo = createMemoWithIdAndMember(1L, member);
		ReflectionTestUtils.setField(updatedMemo, "body", null);

		ModifyMemoRequest request = new ModifyMemoRequest(updatedMemo.getTitle(), updatedMemo.getBody());

		//when & then
		mockMvc.perform(put("/memos/{id}", updatedMemo.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/memos/" + updatedMemo.getId()))
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[*].field").value("body"))
			.andExpect(jsonPath("$.errors[*].message").exists());

		verify(memberService, never()).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).updateMemo(any(UpdateMemoDto.class));
	}

	@Test
	@DisplayName("PUT /memos/{id}: 메모 본문을 빈문자열로 수정 시 400 반환")
	public void modifyMemo_whenBodyIsBlank() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo updatedMemo = createMemoWithIdAndMember(1L, member);
		ReflectionTestUtils.setField(updatedMemo, "body", "     ");

		ModifyMemoRequest request = new ModifyMemoRequest(updatedMemo.getTitle(), updatedMemo.getBody());

		//when & then
		mockMvc.perform(put("/memos/{id}", updatedMemo.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.instance").value("/memos/" + updatedMemo.getId()))
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[*].field").value("body"))
			.andExpect(jsonPath("$.errors[*].message").exists());

		verify(memberService, never()).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).updateMemo(any(UpdateMemoDto.class));
	}

	@Test
	@DisplayName("PUT /memos/{id}: 존재하지 않는 메모를 수정할 때 404 반환")
	public void modifyMemo_whenMemoDoesNotExist() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Memo updatedMemo = createMemoWithIdAndMember(1L, member);

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);

		when(memoService.updateMemo(any(UpdateMemoDto.class)))
			.thenThrow(new MemoNotFoundException());

		ModifyMemoRequest request = new ModifyMemoRequest(updatedMemo.getTitle(), updatedMemo.getBody());

		//when & then
		mockMvc.perform(put("/memos/{id}", updatedMemo.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new MemoNotFoundException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos/" + updatedMemo.getId()));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).updateMemo(any(UpdateMemoDto.class));
	}

	@Test
	@DisplayName("PUT /memos/{id}: 존재하지 않는 회원이 메모 수정을 시도할 때")
	public void modifyMemo_whenMemberDoesNotExist() throws Exception {
		//given
		Long memoId = 1L;

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenThrow(new NoSuchMemberException());

		ModifyMemoRequest request = new ModifyMemoRequest("title", "body");

		//when & then
		mockMvc.perform(put("/memos/{id}", memoId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos/" + memoId));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).updateMemo(any(UpdateMemoDto.class));
	}

	@Test
	@DisplayName("DELETE /memos/{id}: 특정 메모 삭제")
	public void deleteMemo() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Long memoId = 1L;

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);

		//when & then
		mockMvc.perform(delete("/memos/{id}", memoId))
			.andExpect(status().isNoContent());

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).removeMemo(new RemoveMemoDto(memoId, member));
	}

	@Test
	@DisplayName("DELETE /memos/{id}: 존재하지 않는 메모 삭제 시 404 반환")
	public void deleteMemo_whenMemoDoesNotExist() throws Exception {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		Long memoId = 1L;

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenReturn(member);
		RemoveMemoDto dto = new RemoveMemoDto(memoId, member);
		doThrow(new MemoNotFoundException())
			.when(memoService).removeMemo(dto);

		//when & then
		mockMvc.perform(delete("/memos/{id}", memoId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new MemoNotFoundException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos/" + memoId));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService).removeMemo(dto);
	}

	@Test
	@DisplayName("DELETE /memos/{id}: 존재하지 않는 회원이 메모 삭제 시도 시 404 반환")
	public void deleteMemo_whenMemberDoesNotExist() throws Exception {
		//given
		Long memoId = 1L;

		when(memberService.getMemberById(LOGIN_MEMBER_ID))
			.thenThrow(new NoSuchMemberException());

		//when & then
		mockMvc.perform(delete("/memos/{id}", memoId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.detail").value(new NoSuchMemberException().getMessage()))
			.andExpect(jsonPath("$.instance").value("/memos/" + memoId));

		verify(memberService).getMemberById(LOGIN_MEMBER_ID);
		verify(memoService, never()).removeMemo(any(RemoveMemoDto.class));
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

	private List<Memo> createMemoListWithMember(Member member) {
		List<Memo> ret = new ArrayList<>();
		for (long i = 1; i <= 3; i++) {
			ret.add(createMemoWithIdAndMember(i, member));
		}
		return ret;
	}

	private Memo createMemoWithIdAndMember(Long id, Member member) {
		Memo memo = Memo.create("title" + id, "body" + id, member);
		ReflectionTestUtils.setField(memo, "id", id);
		return memo;
	}
}
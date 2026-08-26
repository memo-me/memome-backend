package cloud.memome.backend.api.memo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cloud.memome.backend.api.auth.Login;
import cloud.memome.backend.api.auth.LoginMember;
import cloud.memome.backend.api.memo.request.ModifyMemoRequest;
import cloud.memome.backend.api.memo.request.WriteNewMemoRequest;
import cloud.memome.backend.api.memo.response.GetMemoResponse;
import cloud.memome.backend.api.memo.response.MemoListResponse;
import cloud.memome.backend.application.member.MemberService;
import cloud.memome.backend.application.memo.MemoService;
import cloud.memome.backend.application.memo.dto.CreateMemoDto;
import cloud.memome.backend.application.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.application.memo.dto.RemoveMemoDto;
import cloud.memome.backend.application.memo.dto.UpdateMemoDto;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.memo.Memo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Memo", description = "메모 관련 API")
@RestController
@RequestMapping("/api/memos")
@RequiredArgsConstructor
public class MemoController {
	private final MemoService memoService;
	private final MemberService memberService;

	@Operation(summary = "모든 메모 요약본 조회", description = "내가 작성한 모든 메모의 요약본을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = MemoListResponse.class))),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	@GetMapping
	public MemoListResponse getMemoSummaryList(@Login LoginMember loginMember) {
		Member currentLoginMember = memberService.getMemberById(loginMember.getId());

		List<MemoListResponse.MemoSummary> memoSummaryList = memoService.getOwnedMemosAll(currentLoginMember)
			.stream()
			.map(MemoListResponse.MemoSummary::new)
			.toList();
		return new MemoListResponse(memoSummaryList);
	}

	@Operation(summary = "특정 메모 조회", description = "특정 메모의 세부 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = GetMemoResponse.class))),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 메모 조회",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	@Parameter(name = "id", in = ParameterIn.PATH, description = "메모 아이디")
	@GetMapping("/{id}")
	public GetMemoResponse getMemo(@Login LoginMember loginMember, @PathVariable("id") Long id) {
		Member currentLoginMember = memberService.getMemberById(loginMember.getId());

		Memo ownedMemo = memoService.getOwnedMemo(new GetOwnedMemoDto(id, currentLoginMember));
		return GetMemoResponse.create(ownedMemo);
	}

	@Operation(summary = "새로운 메모 작성", description = "새로운 메모를 작성합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "작성 성공",
			content = @Content(schema = @Schema(implementation = GetMemoResponse.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 형식으로 전송",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	@PostMapping
	public GetMemoResponse writeNewMemo(@Login LoginMember loginMember,
		@Validated @RequestBody WriteNewMemoRequest request) {
		Member currentLoginMember = memberService.getMemberById(loginMember.getId());

		Memo newMemo = memoService.createNewMemo(
			new CreateMemoDto(request.getTitle(), request.getBody(), currentLoginMember));

		return GetMemoResponse.create(newMemo);
	}

	@Operation(summary = "특정 메모 수정", description = "특정 메모의 세부 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공",
			content = @Content(schema = @Schema(implementation = GetMemoResponse.class))),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 메모 조회",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	@PutMapping("/{id}")
	public GetMemoResponse modifyMemo(@Login LoginMember loginMember, @PathVariable("id") Long memoId,
		@Validated @RequestBody ModifyMemoRequest request) {
		Member currentLoginMember = memberService.getMemberById(loginMember.getId());

		Memo memo = memoService.updateMemo(
			new UpdateMemoDto(memoId, currentLoginMember, request.getTitle(), request.getBody()));

		return GetMemoResponse.create(memo);
	}

	@Operation(summary = "특정 메모 삭제", description = "특정 메모를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 메모 삭제 시도 시 실패",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMemo(@Login LoginMember loginMember, @PathVariable("id") Long memoId) {
		Member currentLoginMember = memberService.getMemberById(loginMember.getId());

		memoService.removeMemo(new RemoveMemoDto(memoId, currentLoginMember));
	}
}

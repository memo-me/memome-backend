package cloud.memome.backend.api.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cloud.memome.backend.api.auth.Login;
import cloud.memome.backend.api.auth.LoginMember;
import cloud.memome.backend.api.member.request.UpdateMyInfoRequest;
import cloud.memome.backend.api.member.response.MyInfoResponse;
import cloud.memome.backend.application.member.MemberService;
import cloud.memome.backend.application.member.dto.UpdateMemberDto;
import cloud.memome.backend.domain.member.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Member", description = "회원 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@Operation(summary = "내 정보 확인", description = "내 계정의 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = MyInfoResponse.class))),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	@GetMapping("/me")
	public MyInfoResponse getAccount(@Login LoginMember loginMember) {
		Member member = memberService.getMemberById(loginMember.getId());
		return MyInfoResponse.create(member);
	}

	@Operation(summary = "내 정보 수정", description = "내 계정의 닉네임/메일을 변경할 수 있습니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공",
			content = @Content(schema = @Schema(implementation = MyInfoResponse.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 형식으로 인한 실패",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "401", description = "인증되지 않는 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
	})
	@PutMapping("/me")
	public MyInfoResponse updateAccount(@Login LoginMember loginMember,
		@Validated @RequestBody UpdateMyInfoRequest updateMyInfoReq) {
		Member member = memberService.updateMember(loginMember.getId(),
			new UpdateMemberDto(updateMyInfoReq.getNickname(), updateMyInfoReq.getEmail()));
		return MyInfoResponse.create(member);
	}

	@Operation(summary = "회원 탈퇴", description = "계정을 삭제하고 회원 탈퇴를 진행합니다")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증되지 않는 접근 또는 유효하지 않은 인증 정보",
			content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
	})
	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAccount(@Login LoginMember loginMember) {
		memberService.removeMember(loginMember.getId());
	}
}

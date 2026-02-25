package cloud.memome.backend.member;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cloud.memome.backend.auth.Login;
import cloud.memome.backend.auth.LoginMember;
import cloud.memome.backend.member.dto.IdentityDto;
import cloud.memome.backend.member.dto.UpdateMemberDto;
import cloud.memome.backend.member.request.UpdateMyInfoRequest;
import cloud.memome.backend.member.response.MyInfoResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@GetMapping("/me")
	public MyInfoResponse getAccount(@Login LoginMember loginMember) {
		Member member = memberService.getMemberByIdentity(IdentityDto.create(loginMember));
		return MyInfoResponse.create(member);
	}

	@PutMapping("/me")
	public MyInfoResponse updateAccount(@Login LoginMember loginMember,
		@Validated @RequestBody UpdateMyInfoRequest updateMyInfoReq) {
		Member member = memberService.updateMember(
			IdentityDto.create(loginMember),
			new UpdateMemberDto(updateMyInfoReq.getNickname(), updateMyInfoReq.getEmail()));
		return MyInfoResponse.create(member);
	}

	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAccount(@Login LoginMember loginMember) {
		memberService.removeMember(IdentityDto.create(loginMember));
	}
}

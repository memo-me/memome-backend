package cloud.memome.backend.api.memo;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import cloud.memome.backend.application.member.dto.IdentityDto;
import cloud.memome.backend.application.memo.MemoService;
import cloud.memome.backend.application.memo.dto.CreateMemoDto;
import cloud.memome.backend.application.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.application.memo.dto.RemoveMemoDto;
import cloud.memome.backend.application.memo.dto.UpdateMemoDto;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.memo.Memo;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/memos")
@RequiredArgsConstructor
public class MemoController {
	private final MemoService memoService;
	private final MemberService memberService;

	// GET /memos 자신이 작성한 메모 리스트 얻기
	@GetMapping
	public MemoListResponse getMemoSummaryList(@Login LoginMember loginMember) {
		Member currentLoginMember = memberService.getMemberByIdentity(
			new IdentityDto(loginMember.getProviderType(), loginMember.getProviderId()));

		List<MemoListResponse.MemoSummary> memoSummaryList = memoService.getOwnedMemosAll(currentLoginMember)
			.stream()
			.map(MemoListResponse.MemoSummary::new)
			.toList();
		return new MemoListResponse(memoSummaryList);
	}

	@GetMapping("/{id}")
	public GetMemoResponse getMemo(@Login LoginMember loginMember, @PathVariable Long id) {
		Member currentLoginMember = memberService.getMemberByIdentity(
			new IdentityDto(loginMember.getProviderType(), loginMember.getProviderId()));

		Memo ownedMemo = memoService.getOwnedMemo(new GetOwnedMemoDto(id, currentLoginMember));
		return GetMemoResponse.create(ownedMemo);
	}

	@PostMapping
	public GetMemoResponse writeNewMemo(@Login LoginMember loginMember,
		@Validated @RequestBody WriteNewMemoRequest request) {
		Member currentLoginMember = memberService.getMemberByIdentity(
			new IdentityDto(loginMember.getProviderType(), loginMember.getProviderId()));

		Memo newMemo = memoService.createNewMemo(
			new CreateMemoDto(request.getTitle(), request.getBody(), currentLoginMember));

		return GetMemoResponse.create(newMemo);
	}

	@PutMapping("/{id}")
	public GetMemoResponse modifyMemo(@Login LoginMember loginMember, @PathVariable("id") Long memoId,
		@Validated @RequestBody ModifyMemoRequest request) {
		Member currentLoginMember = memberService.getMemberByIdentity(
			new IdentityDto(loginMember.getProviderType(), loginMember.getProviderId()));

		Memo memo = memoService.updateMemo(
			new UpdateMemoDto(memoId, currentLoginMember, request.getTitle(), request.getBody()));

		return GetMemoResponse.create(memo);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMemo(@Login LoginMember loginMember, @PathVariable("id") Long memoId) {
		Member currentLoginMember = memberService.getMemberByIdentity(
			new IdentityDto(loginMember.getProviderType(), loginMember.getProviderId()));

		memoService.removeMemo(new RemoveMemoDto(memoId, currentLoginMember));
	}
}

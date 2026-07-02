package cloud.memome.backend.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cloud.memome.backend.application.member.dto.IdentityDto;
import cloud.memome.backend.application.member.dto.OAuthUserInfo;
import cloud.memome.backend.application.member.dto.UpdateMemberDto;
import cloud.memome.backend.application.member.exception.InvalidAuthenticationException;
import cloud.memome.backend.application.member.exception.NoSuchMemberException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.member.MemberRepository;
import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.memo.MemoRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final MemoRepository memoRepository;

	@Transactional
	public Member getOrCreateMember(OAuthUserInfo oAuthUserInfo) {
		OAuthIdentity oAuthIdentity = new OAuthIdentity(oAuthUserInfo.getProviderType(), oAuthUserInfo.getProviderId());
		return memberRepository.findByOAuthIdentity(oAuthIdentity)
			.orElseGet(() -> memberRepository.save(
				Member.create(oAuthIdentity, oAuthUserInfo.getNickname(), oAuthUserInfo.getEmail())));
	}

	public Member getMemberByIdentity(IdentityDto identityDto) {
		OAuthIdentity oAuthIdentity = new OAuthIdentity(identityDto.getProviderType(), identityDto.getProviderId());
		return memberRepository.findByOAuthIdentity(oAuthIdentity)
			.orElseThrow(() -> new InvalidAuthenticationException());
	}

	public Member getMemberById(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new NoSuchMemberException());
	}

	@Transactional
	public Member updateMember(IdentityDto identityDto, UpdateMemberDto dto) {
		Member member = this.getMemberByIdentity(identityDto);
		member.updateMember(dto.getNickname(), dto.getEmail());
		return member;
	}

	@Transactional
	public Member updateMember(Long id, UpdateMemberDto dto) {
		Member member = this.getMemberById(id);
		member.updateMember(dto.getNickname(), dto.getEmail());
		return member;
	}

	@Transactional
	public void removeMember(IdentityDto identityDto) {
		Member member = this.getMemberByIdentity(identityDto);
		memberRepository.delete(member);
	}

	@Transactional
	public void removeMember(Long id) {
		Member member = this.getMemberById(id);
		memoRepository.deleteAllByAuthorId(id);
		memberRepository.delete(member);
	}
}

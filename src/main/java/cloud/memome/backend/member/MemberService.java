package cloud.memome.backend.member;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cloud.memome.backend.auth.OAuthUserInfo;
import cloud.memome.backend.member.dto.IdentityDto;
import cloud.memome.backend.member.dto.UpdateMemberDto;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;

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
			.orElseThrow(() -> new NoSuchElementException("Member not found with OAuthIdentity: " + oAuthIdentity));
	}

	@Transactional
	public Member updateMember(IdentityDto identityDto, UpdateMemberDto dto) {
		Member member = this.getMemberByIdentity(identityDto);
		member.updateMember(dto.getNickname(), dto.getEmail());
		return member;
	}

	@Transactional
	public void removeMember(IdentityDto identityDto) {
		Member member = this.getMemberByIdentity(identityDto);
		memberRepository.delete(member);
	}
}

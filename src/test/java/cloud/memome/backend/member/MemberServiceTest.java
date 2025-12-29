package cloud.memome.backend.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cloud.memome.backend.auth.OAuthUserInfo;
import cloud.memome.backend.member.dto.UpdateMemberDto;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;
	
	@DisplayName("조회 또는 생성: 조회")
	@Test
	public void getOrCreateMember_get() {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "1234567890";
		String nickname = "test nickname";
		String email = "test@email.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);
		OAuthUserInfo oAuthUserInfo = new OAuthUserInfo(providerType, providerId, nickname, email);
		Member member = Member.create(oAuthIdentity, nickname, email);

		when(memberRepository.findByOAuthIdentity(oAuthIdentity))
			.thenReturn(Optional.of(member)); //저장된 객체 조회 및 리턴

		//when
		Member result = memberService.getOrCreateMember(oAuthUserInfo);

		//then
		Assertions.assertThat(result.getOAuthIdentity()).isEqualTo(oAuthIdentity);
		Assertions.assertThat(result.getNickname()).isEqualTo(nickname);
		Assertions.assertThat(result.getEmail()).isEqualTo(email);

		verify(memberRepository).findByOAuthIdentity(oAuthIdentity);
		verify(memberRepository, never()).save(member);
	}

	@DisplayName("조회 또는 생성: 생성")
	@Test
	public void getOrCreateMember_create() {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "1234567890";
		String nickname = "test nickname";
		String email = "test@email.com";

		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);
		OAuthUserInfo oAuthUserInfo = new OAuthUserInfo(providerType, providerId, nickname, email);
		Member member = Member.create(oAuthIdentity, nickname, email);

		when(memberRepository.findByOAuthIdentity(any(OAuthIdentity.class)))
			.thenReturn(Optional.empty());
		when(memberRepository.save(any(Member.class)))
			.thenReturn(member); //새로 저장 후 리턴

		//when
		Member result = memberService.getOrCreateMember(oAuthUserInfo);

		//then
		Assertions.assertThat(result.getOAuthIdentity()).isEqualTo(oAuthIdentity);
		Assertions.assertThat(result.getNickname()).isEqualTo(nickname);
		Assertions.assertThat(result.getEmail()).isEqualTo(email);

		verify(memberRepository).findByOAuthIdentity(any(OAuthIdentity.class));
		verify(memberRepository).save(any(Member.class)); //저장
	}

	@DisplayName("저장된 멤버 조회")
	@Test
	public void getMemberById() {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "1234567890");
		String nickname = "test nickname";
		String email = "test@email.com";

		Member member = Member.create(oAuthIdentity, nickname, email);
		when(memberRepository.findById(1L))
			.thenReturn(Optional.of(member));

		//when
		Member find = memberService.getMemberById(1L);

		//then
		Assertions.assertThat(find.getOAuthIdentity()).isEqualTo(oAuthIdentity);
		Assertions.assertThat(find.getNickname()).isEqualTo(nickname);
		Assertions.assertThat(find.getEmail()).isEqualTo(email);

		verify(memberRepository).findById(1L);
	}

	@DisplayName("저장되지 않은 멤버 조회")
	@Test
	public void getMemberById_notFound() {
		//given
		when(memberRepository.findById(2L))
			.thenReturn(Optional.empty());

		//when & then
		Assertions.assertThatThrownBy(() -> memberService.getMemberById(2L))
			.isInstanceOf(NoSuchElementException.class);
		verify(memberRepository).findById(2L);
	}

	@DisplayName("저장된 멤버 OAuthIdentity로 조회")
	@Test
	public void getMemberByOAuthIdentity() {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "1234567890");
		String nickname = "test nickname";
		String email = "test@email.com";

		Member member = Member.create(oAuthIdentity, nickname, email);
		when(memberRepository.findByOAuthIdentity(oAuthIdentity))
			.thenReturn(Optional.of(member));

		//when
		Member find = memberService.getMemberByOAuthIdentity(oAuthIdentity);

		//then
		Assertions.assertThat(find.getOAuthIdentity()).isEqualTo(oAuthIdentity);
		Assertions.assertThat(find.getNickname()).isEqualTo(nickname);
		Assertions.assertThat(find.getEmail()).isEqualTo(email);

		verify(memberRepository).findByOAuthIdentity(oAuthIdentity);
	}

	@DisplayName("존재하지 않는 멤버 OAuthIdentity로 조회")
	@Test
	public void getMemberByOAuthIdentity_not_found() {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "1234567890");

		when(memberRepository.findByOAuthIdentity(oAuthIdentity))
			.thenReturn(Optional.empty());

		//when && then
		Assertions.assertThatThrownBy(() -> memberService.getMemberByOAuthIdentity(oAuthIdentity))
			.isInstanceOf(NoSuchElementException.class);
		verify(memberRepository).findByOAuthIdentity(oAuthIdentity);
	}

	@DisplayName("기존 회원 정보 수정")
	@Test
	public void updateMember_success() {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "1234567890");
		String nickname = "test nickname";
		String email = "test@email.com";
		String updatedNickname = "updated nickname";
		String updatedEmail = "updated@email.com";

		UpdateMemberDto dto = new UpdateMemberDto(1L, updatedNickname, updatedEmail);

		Member member = Member.create(oAuthIdentity, nickname, email);
		when(memberRepository.findById(1L))
			.thenReturn(Optional.of(member));

		//when
		Member result = memberService.updateMember(dto);

		//then
		Assertions.assertThat(result.getOAuthIdentity()).isEqualTo(oAuthIdentity);
		Assertions.assertThat(result.getNickname()).isEqualTo(updatedNickname);
		Assertions.assertThat(result.getEmail()).isEqualTo(updatedEmail);

		verify(memberRepository).findById(1L);
	}

	@DisplayName("회원 삭제 성공")
	@Test
	public void deleteMember_success() {
		//given
		OAuthIdentity oAuthIdentity = new OAuthIdentity(ProviderType.GOOGLE, "1234567890");
		Member member = Member.create(oAuthIdentity, "nickname", "email@email.com");

		when(memberRepository.findById(any(Long.class)))
			.thenReturn(Optional.of(member));

		//when
		memberService.removeMember(any(Long.class));

		//then
		verify(memberRepository).findById(any(Long.class));
		verify(memberRepository).delete(any(Member.class));
	}

	@DisplayName("존재하지 않는 회원 삭제 시도")
	@Test
	public void delete_fail_when_not_found() {
		//given
		when(memberRepository.findById(any(Long.class)))
			.thenReturn(Optional.empty());

		//when & then
		Assertions.assertThatThrownBy(() -> memberService.removeMember(any(Long.class)))
			.isInstanceOf(NoSuchElementException.class);

		//then
		verify(memberRepository).findById(any(Long.class));
		verify(memberRepository, never()).delete(any(Member.class));
	}
}

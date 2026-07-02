package cloud.memome.backend.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import cloud.memome.backend.application.member.dto.OAuthUserInfo;
import cloud.memome.backend.application.member.dto.UpdateMemberDto;
import cloud.memome.backend.application.member.exception.NoSuchMemberException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.member.MemberRepository;
import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.member.ProviderType;
import cloud.memome.backend.domain.memo.MemoRepository;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
	@InjectMocks
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private MemoRepository memoRepository;

	private final static Long LOGIN_MEMBER_ID = 1L;

	@Test
	@DisplayName("getOrCreateMember(): 회원 조회 성공")
	public void getOrCreateMember_getMember() {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		OAuthIdentity oAuthIdentity = member.getOAuthIdentity();

		when(memberRepository.findByOAuthIdentity(oAuthIdentity))
			.thenReturn(Optional.of(member));

		//when
		OAuthUserInfo info = new OAuthUserInfo(
			oAuthIdentity.getProviderType(),
			oAuthIdentity.getProviderId(),
			member.getNickname(),
			member.getEmail()
		);
		Member result = memberService.getOrCreateMember(info);

		//then
		assertThat(result).isSameAs(member);

		verify(memberRepository).findByOAuthIdentity(oAuthIdentity);
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("getOrCreateMember(): 회원이 없으면 생성 후 반환")
	public void getOrCreateMember_saveMember() {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		OAuthIdentity oAuthIdentity = member.getOAuthIdentity();

		when(memberRepository.findByOAuthIdentity(oAuthIdentity))
			.thenReturn(Optional.empty());
		when(memberRepository.save(any(Member.class)))
			.thenReturn(member);

		OAuthUserInfo info = new OAuthUserInfo(
			oAuthIdentity.getProviderType(),
			oAuthIdentity.getProviderId(),
			member.getNickname(),
			member.getEmail()
		);

		//when
		Member result = memberService.getOrCreateMember(info);

		//then
		assertThat(result).isSameAs(member);

		verify(memberRepository).findByOAuthIdentity(oAuthIdentity);
		verify(memberRepository).save(any(Member.class));
	}

	@Test
	@DisplayName("getMemberById(): 아이디로 회원 조회 성공")
	public void getMemberById() {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);

		when(memberRepository.findById(LOGIN_MEMBER_ID))
			.thenReturn(Optional.of(member));

		//when
		Member result = memberService.getMemberById(LOGIN_MEMBER_ID);

		//then
		assertThat(result).isSameAs(member);

		verify(memberRepository).findById(LOGIN_MEMBER_ID);
	}

	@Test
	@DisplayName("getMemberById(): 존재하지 않는 회원 조회 시 예외 발생")
	public void getMemberById_whenMemberDoesNotExist() {
		//given
		when(memberRepository.findById(LOGIN_MEMBER_ID))
			.thenReturn(Optional.empty());

		//when && then
		assertThatThrownBy(() -> memberService.getMemberById(LOGIN_MEMBER_ID))
			.isInstanceOf(NoSuchMemberException.class);

		verify(memberRepository).findById(LOGIN_MEMBER_ID);
	}

	@Test
	@DisplayName("updateMember(): 회원 정보 수정")
	public void updateMember() {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);
		UpdateMemberDto updateMemberDto = new UpdateMemberDto(
			"updated", "updated@test.com"
		);

		when(memberRepository.findById(LOGIN_MEMBER_ID))
			.thenReturn(Optional.of(member));

		//when
		Member result = memberService.updateMember(LOGIN_MEMBER_ID, updateMemberDto);

		//then
		assertThat(result).isSameAs(member);
		assertThat(result.getNickname()).isEqualTo(updateMemberDto.getNickname());
		assertThat(result.getEmail()).isEqualTo(updateMemberDto.getEmail());

		verify(memberRepository).findById(LOGIN_MEMBER_ID);
	}

	@Test
	@DisplayName("updateMember(): 존재하지 않는 회원 수정 시 예외 발생")
	public void updateMember_whenMemberDoesNotExist() {
		//given
		when(memberRepository.findById(LOGIN_MEMBER_ID))
			.thenReturn(Optional.empty());

		UpdateMemberDto dto = new UpdateMemberDto(
			"updated", "updated@test.com"
		);

		//when && then
		assertThatThrownBy(() -> memberService.updateMember(LOGIN_MEMBER_ID, dto))
			.isInstanceOf(NoSuchMemberException.class);

		verify(memberRepository).findById(LOGIN_MEMBER_ID);
	}

	@Test
	@DisplayName("removeMember(): 회원 탈퇴")
	public void removeMember() {
		//given
		Member member = createMemberWithId(LOGIN_MEMBER_ID);

		when(memberRepository.findById(LOGIN_MEMBER_ID))
			.thenReturn(Optional.of(member));

		//when
		memberService.removeMember(LOGIN_MEMBER_ID);

		//then
		verify(memberRepository).findById(LOGIN_MEMBER_ID);
		verify(memoRepository).deleteAllByAuthorId(LOGIN_MEMBER_ID);
		verify(memberRepository).delete(member);
	}

	@Test
	@DisplayName("removeMember(): 존재하지 않는 회원 삭제 시 예외 발생")
	public void removeMember_whenMemberDoesNotExist() {
		//given
		when(memberRepository.findById(LOGIN_MEMBER_ID))
			.thenReturn(Optional.empty());

		//when && then
		assertThatThrownBy(() -> memberService.removeMember(LOGIN_MEMBER_ID))
			.isInstanceOf(NoSuchMemberException.class);

		verify(memberRepository).findById(LOGIN_MEMBER_ID);
		verify(memoRepository, never()).deleteAllByAuthorId(LOGIN_MEMBER_ID);
		verify(memberRepository, never()).delete(any(Member.class));
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

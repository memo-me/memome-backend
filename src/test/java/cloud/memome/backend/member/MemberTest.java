package cloud.memome.backend.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {
	@DisplayName("멤버 객체 생성")
	@Test
	public void createMember() {
		//given
		String nickname = "test nickname";
		String email = "test@email.com";

		//when
		Member member = Member.create(nickname, email);

		//then
		Assertions.assertThat(member.getNickname()).isEqualTo(nickname);
		Assertions.assertThat(member.getEmail()).isEqualTo(email);
		Assertions.assertThat(member.getCreatedAt()).isEqualTo(member.getUpdatedAt());
	}

	@DisplayName("nickname == null 일 때, 멤버 객체 생성 실패")
	@Test
	public void create_fail_when_nickname_is_null() {
		//when && then
		Assertions.assertThatThrownBy(() -> Member.create(null, "email@email.com"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("email == null 일 때, 멤버 객체 생성 실패")
	@Test
	public void create_fail_when_email_is_null() {
		//when && then
		Assertions.assertThatThrownBy(() -> Member.create("test nickname", null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("멤버 객체 수정")
	@Test
	public void updateMember() throws InterruptedException {
		//given
		String nickname = "test nickname";
		String email = "test@email.com";
		String updatedNickname = "updated nickname";
		String updatedEmail = "updated@email.com";

		Member member = Member.create(nickname, email);

		//when
		Thread.sleep(1L);
		member.updateMember(updatedNickname, updatedEmail);

		//then
		Assertions.assertThat(member.getNickname()).isEqualTo(updatedNickname);
		Assertions.assertThat(member.getEmail()).isEqualTo(updatedEmail);
		Assertions.assertThat(member.getCreatedAt()).isNotEqualTo(member.getUpdatedAt());
	}

	@DisplayName("nickname == null 일 때, 멤버 수정 실패")
	@Test
	public void update_fail_when_nickname_is_null() {
		//given
		String nickname = "test nickname";
		String email = "test@email.com";
		String updatedEmail = "updated@email.com";

		Member member = Member.create(nickname, email);

		//when && then
		Assertions.assertThatThrownBy(() -> member.updateMember(null, updatedEmail))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("email == null 일 때, 멤버 수정 실패")
	@Test
	public void update_fail_when_email_is_null() {
		//given
		String nickname = "test nickname";
		String email = "test@email.com";
		String updatedNickname = "updated nickname";

		Member member = Member.create(nickname, email);

		//when && then
		Assertions.assertThatThrownBy(() -> member.updateMember(updatedNickname, null))
			.isInstanceOf(IllegalArgumentException.class);
	}
}

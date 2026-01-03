package cloud.memome.backend.memo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import cloud.memome.backend.member.Member;
import cloud.memome.backend.member.OAuthIdentity;
import cloud.memome.backend.member.ProviderType;
import cloud.memome.backend.memo.exception.NotMemoOwnerException;

@ExtendWith(MockitoExtension.class)
class MemoTest {
	@Test
	@DisplayName("메모 생성 성공")
	public void create_memo_success() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		//when
		Memo memo = Memo.create(title, body, author);

		//then
		Assertions.assertThat(memo.getTitle()).isEqualTo(title);
		Assertions.assertThat(memo.getBody()).isEqualTo(body);
		Assertions.assertThat(memo.getAuthor()).isEqualTo(author);
		Assertions.assertThat(memo.getCreatedAt()).isSameAs(memo.getUpdatedAt());
	}

	@Test
	@DisplayName("메모 생성 실패 - title == null")
	public void create_memo_fail_when_title_is_null() {
		//given
		String title = null;
		String body = "memo body... test is the test";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		//when && then
		Assertions.assertThatThrownBy(() -> Memo.create(title, body, author))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 생성 실패 - title == blank")
	public void create_memo_fail_when_title_is_blank() {
		//given
		String title = "           ";
		String body = "memo body... test is the test";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		//when && then
		Assertions.assertThatThrownBy(() -> Memo.create(title, body, author))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 생성 실패 - body == null")
	public void create_memo_fail_when_body_is_null() {
		//given
		String title = "memo title";
		String body = null;

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		//when && then
		Assertions.assertThatThrownBy(() -> Memo.create(title, body, author))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 생성 실패 - body == blank")
	public void create_memo_fail_when_body_is_blank() {
		//given
		String title = "memo title";
		String body = "                    ";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		//when && then
		Assertions.assertThatThrownBy(() -> Memo.create(title, body, author))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 생성 실패 - author == null")
	public void create_memo_fail_when_author_is_null() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";
		Member author = null;

		//when && then
		Assertions.assertThatThrownBy(() -> Memo.create(title, body, author))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 수정 성공")
	public void update_memo_success() throws InterruptedException {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";
		String updatedTitle = "memo title, updated";
		String updatedBody = "memo body... test is the test, updated";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		Memo memo = Memo.create(title, body, author);

		//when
		memo.update(updatedTitle, updatedBody, author.getId());

		//then
		Assertions.assertThat(memo.getTitle()).isEqualTo(updatedTitle);
		Assertions.assertThat(memo.getBody()).isEqualTo(updatedBody);
		Assertions.assertThat(memo.getAuthor()).isEqualTo(author);
		Assertions.assertThat(memo.getCreatedAt()).isNotSameAs(memo.getUpdatedAt());
	}

	@Test
	@DisplayName("메모 수정 실패 - title == null")
	public void update_memo_fail_when_title_is_null() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";
		String updatedTitle = null;
		String updatedBody = "memo body... test is the test";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatThrownBy(() -> memo.update(updatedTitle, updatedBody, author.getId()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 수정 실패 - title == blank")
	public void update_memo_fail_when_title_is_blank() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";
		String updatedTitle = "           ";
		String updatedBody = "memo body... test is the test";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatThrownBy(() -> memo.update(updatedTitle, updatedBody, author.getId()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 수정 실패 - body == null")
	public void update_memo_fail_when_body_is_null() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";
		String updatedTitle = "memo title";
		String updatedBody = null;

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatThrownBy(() -> memo.update(updatedTitle, updatedBody, author.getId()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 수정 실패 - body == blank")
	public void update_memo_fail_when_body_is_blank() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";
		String updatedTitle = "memo title";
		String updatedBody = "                    ";

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", 1L);

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatThrownBy(() -> memo.update(updatedTitle, updatedBody, author.getId()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 작성자 확인 - 일치")
	public void assert_author_same() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";

		Long authorId = 1L;

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", authorId);

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatCode(() -> memo.assertAuthor(authorId))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("메모 작성자 확인 - 불일치")
	public void assert_author_fail() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";

		Long authorId = 1L;
		Long anotherAuthorId = 99L;

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", authorId);

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatThrownBy(() -> memo.assertAuthor(anotherAuthorId))
			.isExactlyInstanceOf(NotMemoOwnerException.class);
	}
}
package cloud.memome.backend.memo;

import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cloud.memome.backend.member.Member;

@ExtendWith(MockitoExtension.class)
class MemoTest {
	@Mock
	private Member author;

	@Test
	@DisplayName("메모 생성 성공")
	public void create_memo_success() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";

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

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatThrownBy(() -> memo.update(updatedTitle, updatedBody, author.getId()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("메모 작성자 확인 - 일치")
	public void is_author_same() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";

		Long authorId = 1L;
		when(author.getId())
			.thenReturn(authorId);

		Memo memo = Memo.create(title, body, author);

		//when
		memo.assertAuthor(author.getId());

		//then
		verify(author).getId();
	}

	@Test
	@DisplayName("메모 작성자 확인 - 불일치")
	public void is_author_not_same() {
		//given
		String title = "memo title";
		String body = "memo body... test is the test";

		Long author1Id = 1L;
		Long author2Id = 2L;
		when(author.getId())
			.thenReturn(author1Id);

		Memo memo = Memo.create(title, body, author);

		//when && then
		Assertions.assertThatThrownBy(() -> memo.assertAuthor(author.getId()))
			.isInstanceOf(RuntimeException.class);
	}
}
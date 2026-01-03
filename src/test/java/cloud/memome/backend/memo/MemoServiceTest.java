package cloud.memome.backend.memo;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import cloud.memome.backend.member.Member;
import cloud.memome.backend.member.OAuthIdentity;
import cloud.memome.backend.member.ProviderType;
import cloud.memome.backend.memo.dto.CreateMemoDto;
import cloud.memome.backend.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.memo.dto.RemoveMemoDto;
import cloud.memome.backend.memo.dto.UpdateMemoDto;
import cloud.memome.backend.memo.exception.NotMemoOwnerException;

@ExtendWith(MockitoExtension.class)
class MemoServiceTest {
	@InjectMocks
	private MemoService memoService;
	@Mock
	private MemoRepository memoRepository;

	@Test
	@DisplayName("메모 생성 - 성공")
	public void create_memo_success() {
		//given
		String title = "memo title";
		String body = "This is Memo body";
		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		CreateMemoDto dto = new CreateMemoDto(title, body, author);

		when(memoRepository.save(any(Memo.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		//when
		Memo result = memoService.createNewMemo(dto);

		//then
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getTitle()).isEqualTo(title);
		Assertions.assertThat(result.getBody()).isEqualTo(body);
		Assertions.assertThat(result.getAuthor()).isEqualTo(author);
	}

	@Test
	@DisplayName("메모 조회 - 성공")
	public void get_memo_success() {
		//given
		String title = "memo title";
		String body = "This is Memo body";
		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		Memo memo = Memo.create(title, body, author);

		Long memoId = 1L;
		Long authorId = 1L;
		GetOwnedMemoDto dto = new GetOwnedMemoDto(memoId, authorId);

		when(memoRepository.findByIdAndAuthorId(memoId, authorId))
			.thenReturn(Optional.of(memo));

		//when
		Memo result = memoService.getOwnedMemo(dto);

		//then
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getTitle()).isEqualTo(title);
		Assertions.assertThat(result.getBody()).isEqualTo(body);
		Assertions.assertThat(result.getAuthor()).isEqualTo(author);
	}

	@Test
	@DisplayName("메모 조회 - 실패")
	public void get_memo_fail() {
		//given
		Long memoId = 1L;
		Long authorId = 1L;
		GetOwnedMemoDto dto = new GetOwnedMemoDto(memoId, authorId);

		when(memoRepository.findByIdAndAuthorId(memoId, authorId))
			.thenReturn(Optional.empty());

		//when && then
		Assertions.assertThatThrownBy(() -> memoService.getOwnedMemo(dto))
			.isInstanceOf(NoSuchElementException.class);
	}

	@Test
	@DisplayName("모든 메모 조회 - 성공")
	public void get_all_memos() {
		//given
		String title = "memo title";
		String body = "This is Memo body";
		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		Memo memo1 = Memo.create(title, body, author);
		Memo memo2 = Memo.create(title, body, author);

		when(memoRepository.findAllByAuthor(author))
			.thenReturn(List.of(memo1, memo2));

		//when
		List<Memo> ownedMemosAll = memoService.getOwnedMemosAll(author);

		//then
		Assertions.assertThat(ownedMemosAll).isNotNull();
		Assertions.assertThat(ownedMemosAll.size()).isEqualTo(2L);
		Assertions.assertThat(ownedMemosAll.getFirst()).isNotNull();
		Assertions.assertThat(ownedMemosAll.getFirst().getTitle()).isEqualTo(title);
		Assertions.assertThat(ownedMemosAll.getFirst().getBody()).isEqualTo(body);
	}

	@Test
	@DisplayName("메모 수정 - 성공")
	public void update_memo_success() {
		//given
		String title = "memo title";
		String body = "This is Memo body";
		String updatedTitle = "memo title, updated";
		String updatedBody = "This is Memo body, updated";

		Long memoId = 1L;
		Long authorId = 1L;

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", authorId);

		Memo memo = Memo.create(title, body, author);
		when(memoRepository.findById(memoId))
			.thenReturn(Optional.of(memo));

		//when
		Memo result = memoService.updateMemo(
			new UpdateMemoDto(memoId, authorId, updatedTitle, updatedBody));

		//then
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getTitle()).isEqualTo(updatedTitle);
		Assertions.assertThat(result.getBody()).isEqualTo(updatedBody);
		Assertions.assertThat(result.getAuthor()).isEqualTo(author);
	}

	@Test
	@DisplayName("다른 작성자의 메모 수정 - 실패")
	public void update_memo_fail_when_not_mine() {
		//given
		String title = "memo title";
		String body = "This is Memo body";
		String updatedTitle = "memo title, updated";
		String updatedBody = "This is Memo body, updated";

		Long memoId = 1L;
		Long authorId = 1L;
		Long anotherAuthorId = 2L;

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", authorId);

		Memo memo = Memo.create(title, body, author);
		when(memoRepository.findById(memoId))
			.thenReturn(Optional.of(memo));

		//when
		Assertions.assertThatThrownBy(
				() -> memoService.updateMemo(new UpdateMemoDto(memoId, anotherAuthorId, updatedTitle, updatedBody)))
			.isInstanceOf(NotMemoOwnerException.class);
	}

	@Test
	@DisplayName("존재하지 않는 메모 수정 - 실패")
	public void update_memo_fail_when_memo_not_found() {
		//given
		String updatedTitle = "memo title, updated";
		String updatedBody = "This is Memo body, updated";

		Long authorId = 1L;
		Long anotherMemoId = 2L;

		when(memoRepository.findById(anotherMemoId))
			.thenReturn(Optional.empty());

		//when
		Assertions.assertThatThrownBy(
				() -> memoService.updateMemo(new UpdateMemoDto(anotherMemoId, authorId, updatedTitle, updatedBody)))
			.isInstanceOf(NoSuchElementException.class);
	}

	@Test
	@DisplayName("메모 삭제 - 성공")
	public void delete_memo_success() {
		//given
		String title = "memo title";
		String body = "This is Memo body";

		Long memoId = 1L;
		Long authorId = 1L;

		Member author = Member.create(new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", authorId);

		Memo memo = Memo.create(title, body, author);
		when(memoRepository.findById(memoId))
			.thenReturn(Optional.of(memo));

		//when
		memoService.removeMemo(
			new RemoveMemoDto(memoId, authorId));

		//then
		verify(memoRepository).findById(memoId);
		verify(memoRepository).delete(memo);
	}

	@Test
	@DisplayName("다른 작성자의 메모 삭제 - 실패")
	public void delete_memo_fail_when_not_mine() {
		//given
		String title = "memo title";
		String body = "This is Memo body";

		Long memoId = 1L;
		Long authorId = 1L;
		Long anotherAuthorId = 2L;

		Member author = Member.create(
			new OAuthIdentity(ProviderType.GOOGLE, "1234567890"), "nickname", "email");
		ReflectionTestUtils.setField(author, "id", authorId);

		Memo memo = Memo.create(title, body, author);
		when(memoRepository.findById(memoId))
			.thenReturn(Optional.of(memo));

		//when && then
		Assertions.assertThatThrownBy(() -> memoService.removeMemo(new RemoveMemoDto(memoId, anotherAuthorId)))
			.isInstanceOf(NotMemoOwnerException.class);

		verify(memoRepository).findById(memoId);
		verify(memoRepository, never()).delete(memo);
	}

	@Test
	@DisplayName("존재하지 않는 메모 삭제 - 실패")
	public void delete_memo_fail_when_memo_not_found() {
		//given
		Long authorId = 1L;
		Long anotherMemoId = 2L;

		when(memoRepository.findById(anotherMemoId))
			.thenReturn(Optional.empty());

		//when && then
		Assertions.assertThatThrownBy(() -> memoService.removeMemo(new RemoveMemoDto(anotherMemoId, authorId)))
			.isInstanceOf(NoSuchElementException.class);

		verify(memoRepository).findById(anotherMemoId);
		verify(memoRepository, never()).delete(any(Memo.class));
	}
}
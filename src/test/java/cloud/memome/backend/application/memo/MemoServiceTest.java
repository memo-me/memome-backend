package cloud.memome.backend.application.memo;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import cloud.memome.backend.application.memo.dto.CreateMemoDto;
import cloud.memome.backend.application.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.application.memo.dto.RemoveMemoDto;
import cloud.memome.backend.application.memo.dto.UpdateMemoDto;
import cloud.memome.backend.application.memo.exception.MemoNotFoundException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.member.ProviderType;
import cloud.memome.backend.domain.memo.Memo;
import cloud.memome.backend.domain.memo.MemoRepository;

@ExtendWith(MockitoExtension.class)
class MemoServiceTest {
	@InjectMocks
	private MemoService memoService;
	@Mock
	private MemoRepository memoRepository;

	@Test
	@DisplayName("createNewMemo(): 새로운 메모 작성")
	public void createNewMemo() {
		//given
		Member author = createMemberWithId(1L);
		Memo memo = createMemoWithIdAndMember(1L, author);

		CreateMemoDto dto = new CreateMemoDto(memo.getTitle(), memo.getBody(), author);

		when(memoRepository.save(any(Memo.class)))
			.thenReturn(memo);

		//when
		Memo result = memoService.createNewMemo(dto);

		//then
		assertThat(result).isSameAs(memo);

		verify(memoRepository).save(any(Memo.class));
	}

	@Test
	@DisplayName("getOwnedMemo(): 회원 자신이 작성한 메모 조회")
	public void getOwnedMemo() {
		//given
		Member author = createMemberWithId(1L);
		Memo memo = createMemoWithIdAndMember(1L, author);

		GetOwnedMemoDto dto = new GetOwnedMemoDto(author.getId(), author);

		when(memoRepository.findByIdAndAuthor(dto.getMemoId(), dto.getAuthor()))
			.thenReturn(Optional.of(memo));

		//when
		Memo result = memoService.getOwnedMemo(dto);

		//then
		assertThat(result).isSameAs(memo);

		verify(memoRepository).findByIdAndAuthor(memo.getId(), author);
	}

	@Test
	@DisplayName("getOwnedMemo(): 회원 자신이 작성하지 않거나 존재하지 않는 메모를 조회하는 경우 예외 발생")
	public void getOwnedMemo_whenMemoDoesNotExist() {
		//given
		Member author = createMemberWithId(1L);
		Memo memo = createMemoWithIdAndMember(1L, author);

		GetOwnedMemoDto dto = new GetOwnedMemoDto(memo.getId(), author);

		when(memoRepository.findByIdAndAuthor(dto.getMemoId(), dto.getAuthor()))
			.thenReturn(Optional.empty());

		//when && then
		assertThatThrownBy(() -> memoService.getOwnedMemo(dto))
			.isInstanceOf(MemoNotFoundException.class);

		verify(memoRepository).findByIdAndAuthor(dto.getMemoId(), dto.getAuthor());
	}

	@Test
	@DisplayName("getOwnedMemosAll(): 자신이 작성한 모든 메모 리스트 조회")
	public void getOwnedMemosAll() {
		//given
		Member author = createMemberWithId(1L);
		Memo memo1 = createMemoWithIdAndMember(1L, author);
		Memo memo2 = createMemoWithIdAndMember(2L, author);

		List<Memo> memoList = List.of(memo1, memo2);
		when(memoRepository.findAllByAuthor(author))
			.thenReturn(memoList);

		//when
		List<Memo> ownedMemosAll = memoService.getOwnedMemosAll(author);

		//then
		assertThat(ownedMemosAll).isSameAs(memoList);

		verify(memoRepository).findAllByAuthor(author);
	}

	@Test
	@DisplayName("getOwnedMemosAll(): 작성한 메모가 없을 때, 자신이 작성한 모든 메모 리스트 조회 시 빈 리스트 반환")
	public void getOwnedMemosAll_whenMemoDoesNotExist() {
		//given
		Member author = createMemberWithId(1L);

		List<Memo> memoList = List.of();
		when(memoRepository.findAllByAuthor(author))
			.thenReturn(memoList);

		//when
		List<Memo> ownedMemosAll = memoService.getOwnedMemosAll(author);

		//then
		assertThat(ownedMemosAll).isSameAs(memoList);

		verify(memoRepository).findAllByAuthor(author);
	}

	@Test
	@DisplayName("updateMemo(): 자신이 작성한 메모 수정")
	public void updateMemo() {
		//given
		Member author = createMemberWithId(1L);
		Memo memo = createMemoWithIdAndMember(1L, author);

		UpdateMemoDto dto = new UpdateMemoDto(memo.getId(), author, "updated title", "updated body");
		when(memoRepository.findByIdAndAuthor(dto.getMemoId(), dto.getAuthor()))
			.thenReturn(Optional.of(memo));

		//when
		Memo result = memoService.updateMemo(dto);

		//then
		assertThat(result).isSameAs(memo);
		assertThat(result.getTitle()).isEqualTo(dto.getTitle());
		assertThat(result.getBody()).isEqualTo(dto.getBody());

		verify(memoRepository).findByIdAndAuthor(dto.getMemoId(), dto.getAuthor());
	}

	@Test
	@DisplayName("updateMemo(): 자신이 작성하지 않은 또는 존재하지 않는 메모를 수정 하는 경우 예외 발생")
	public void updateMemo_whenMemoDoesNotExist() {
		//given
		Long notExistMemoId = 999L;

		Member author = createMemberWithId(1L);
		UpdateMemoDto dto = new UpdateMemoDto(notExistMemoId, author, "updated title", "updated body");

		when(memoRepository.findByIdAndAuthor(notExistMemoId, author))
			.thenReturn(Optional.empty());

		//when
		assertThatThrownBy(
			() -> memoService.updateMemo(dto))
			.isInstanceOf(MemoNotFoundException.class);

		verify(memoRepository).findByIdAndAuthor(dto.getMemoId(), dto.getAuthor());
	}

	@Test
	@DisplayName("removeMemo(): 자신이 작성한 특정 메모 삭제")
	public void removeMemo() {
		//given
		Member author = createMemberWithId(1L);
		Memo memo = createMemoWithIdAndMember(1L, author);

		RemoveMemoDto dto = new RemoveMemoDto(memo.getId(), author);

		when(memoRepository.findByIdAndAuthor(dto.getMemoId(), dto.getAuthor()))
			.thenReturn(Optional.of(memo));

		//when
		memoService.removeMemo(dto);

		//then
		verify(memoRepository).findByIdAndAuthor(dto.getMemoId(), dto.getAuthor());
		verify(memoRepository).delete(memo);
	}

	@Test
	@DisplayName("removeMemo(): 자신이 작성하지 않은 또는 존재하지 않는 메모 삭제하는 경우 예외 발생")
	public void removeMemo_whenMemoDoesNotExist() {
		//given
		Long notExistMemoId = 999L;
		Member author = createMemberWithId(1L);
		RemoveMemoDto dto = new RemoveMemoDto(notExistMemoId, author);

		when(memoRepository.findByIdAndAuthor(notExistMemoId, author))
			.thenReturn(Optional.empty());

		//when && then
		assertThatThrownBy(() -> memoService.removeMemo(dto))
			.isInstanceOf(MemoNotFoundException.class);

		verify(memoRepository).findByIdAndAuthor(dto.getMemoId(), dto.getAuthor());
		verify(memoRepository, never()).delete(any(Memo.class));
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

	private Memo createMemoWithIdAndMember(Long id, Member member) {
		Memo memo = Memo.create("title" + id, "body" + id, member);
		ReflectionTestUtils.setField(memo, "id", id);
		return memo;
	}
}
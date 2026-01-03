package cloud.memome.backend.memo;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cloud.memome.backend.member.Member;
import cloud.memome.backend.memo.dto.CreateMemoDto;
import cloud.memome.backend.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.memo.dto.RemoveMemoDto;
import cloud.memome.backend.memo.dto.UpdateMemoDto;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemoService {
	private final MemoRepository memoRepository;

	@Transactional
	public Memo createNewMemo(CreateMemoDto dto) {
		Memo memo = Memo.create(dto.getTitle(), dto.getBody(), dto.getAuthor());
		return memoRepository.save(memo);
	}

	private Memo getMemoById(Long id) {
		Memo memo = memoRepository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("Memo not found with id: " + id));
		return memo;
	}

	public Memo getOwnedMemo(GetOwnedMemoDto dto) {
		Memo memo = memoRepository.findByIdAndAuthorId(dto.getMemoId(), dto.getAuthorId())
			.orElseThrow(() -> new NoSuchElementException("Memo not found with id: " + dto.getMemoId()));
		return memo;
	}

	public List<Memo> getOwnedMemosAll(Member author) {
		return memoRepository.findAllByAuthor(author);
	}

	@Transactional
	public Memo updateMemo(UpdateMemoDto dto) {
		Memo memo = this.getMemoById(dto.getMemoId());
		memo.update(dto.getTitle(), dto.getBody(), dto.getAuthorId());
		return memo;
	}

	@Transactional
	public void removeMemo(RemoveMemoDto dto) {
		Memo memo = this.getMemoById(dto.getMemoId());
		memo.assertAuthor(dto.getAuthorId());
		memoRepository.delete(memo);
	}
}

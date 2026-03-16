package cloud.memome.backend.application.memo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cloud.memome.backend.application.memo.dto.CreateMemoDto;
import cloud.memome.backend.application.memo.dto.GetOwnedMemoDto;
import cloud.memome.backend.application.memo.dto.RemoveMemoDto;
import cloud.memome.backend.application.memo.dto.UpdateMemoDto;
import cloud.memome.backend.application.memo.exception.MemoNotFoundException;
import cloud.memome.backend.domain.member.Member;
import cloud.memome.backend.domain.memo.Memo;
import cloud.memome.backend.domain.memo.MemoRepository;
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

	public Memo getOwnedMemo(GetOwnedMemoDto dto) {
		Memo memo = memoRepository.findByIdAndAuthor(dto.getMemoId(), dto.getAuthor())
			.orElseThrow(() -> new MemoNotFoundException());
		return memo;
	}

	public List<Memo> getOwnedMemosAll(Member author) {
		return memoRepository.findAllByAuthor(author);
	}

	@Transactional
	public Memo updateMemo(UpdateMemoDto dto) {
		Memo memo = this.getOwnedMemo(new GetOwnedMemoDto(dto.getMemoId(), dto.getAuthor()));
		memo.update(dto.getTitle(), dto.getBody(), dto.getAuthor());
		return memo;
	}

	@Transactional
	public void removeMemo(RemoveMemoDto dto) {
		Memo memo = this.getOwnedMemo(new GetOwnedMemoDto(dto.getMemoId(), dto.getAuthor()));
		memoRepository.delete(memo);
	}
}

package cloud.memome.backend.memo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cloud.memome.backend.member.Member;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
	List<Memo> findAllByAuthor(Member author);

	Optional<Memo> findByIdAndAuthorId(Long id, Long authorId);
}

package cloud.memome.backend.memo;

import java.time.LocalDateTime;

import cloud.memome.backend.member.Member;
import cloud.memome.backend.memo.exception.NotMemoOwnerException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Memo {
	@Id
	@GeneratedValue
	private Long id;
	private String title;
	@Lob
	private String body;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id")
	private Member author;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	private Memo(String title, String body, Member author) {
		this.title = title;
		this.body = body;
		this.author = author;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = createdAt;
	}

	public static Memo create(String title, String body, Member author) {
		validateTitleAndBody(title, body);
		validateAuthor(author);

		return Memo.builder()
			.title(title)
			.body(body)
			.author(author)
			.build();
	}

	public void update(String title, String body, Long authorId) {
		validateTitleAndBody(title, body);
		assertAuthor(authorId);
		this.title = title;
		this.body = body;
		this.updatedAt = LocalDateTime.now();
	}

	private static void validateTitleAndBody(String title, String body) {
		if (title == null || title.isBlank() || body == null || body.isBlank()) {
			throw new IllegalArgumentException("title 또는 body는 null이거나 빈 문자열일 수 없습니다");
		}
	}

	private static void validateAuthor(Member author) {
		if (author == null) {
			throw new IllegalArgumentException("author는 null일 수 없습니다");
		}
	}

	public void assertAuthor(Long authorId) {
		if (!this.author.getId().equals(authorId)) {
			throw new NotMemoOwnerException(this.id, authorId);
		}
	}
}

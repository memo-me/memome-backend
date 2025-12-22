package cloud.memome.backend.member;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {
	@Id
	@GeneratedValue
	private Long id;
	private String nickname;
	private String email;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	private Member(String nickname, String email) {
		this.nickname = nickname;
		this.email = email;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = createdAt;
	}

	public static Member create(String nickname, String email) {
		validateUpdatableField(nickname, email);
		return Member.builder()
			.nickname(nickname)
			.email(email)
			.build();
	}

	public void updateMember(String nickname, String email) {
		validateUpdatableField(nickname, email);

		this.nickname = nickname;
		this.email = email;
		this.updatedAt = LocalDateTime.now();
	}

	private static void validateUpdatableField(String nickname, String email) {
		if (nickname == null || email == null) {
			throw new IllegalArgumentException("nickname 또는 email은 null일 수 없습니다.");
		}
	}
}

package cloud.memome.backend.member;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

	@Embedded
	@Column(unique = true, updatable = false)
	private OAuthIdentity oAuthIdentity;

	private String nickname;
	private String email;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	private Member(OAuthIdentity oAuthIdentity, String nickname, String email) {
		this.oAuthIdentity = oAuthIdentity;
		this.nickname = nickname;
		this.email = email;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = createdAt;
	}

	public static Member create(OAuthIdentity oAuthIdentity, String nickname, String email) {
		if (oAuthIdentity == null) {
			throw new IllegalArgumentException("oAuthIdentity는 null일 수 없습니다.");
		}
		validateNicknameAndEmail(nickname, email);

		return Member.builder()
			.oAuthIdentity(oAuthIdentity)
			.nickname(nickname)
			.email(email)
			.build();
	}

	public void updateMember(String nickname, String email) {
		validateNicknameAndEmail(nickname, email);

		this.nickname = nickname;
		this.email = email;
		this.updatedAt = LocalDateTime.now();
	}

	private static void validateNicknameAndEmail(String nickname, String email) {
		if (nickname == null || email == null) {
			throw new IllegalArgumentException("nickname 또는 email은 null일 수 없습니다.");
		}
	}
}

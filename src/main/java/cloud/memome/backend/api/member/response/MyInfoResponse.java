package cloud.memome.backend.api.member.response;

import java.time.LocalDateTime;

import cloud.memome.backend.domain.member.Member;
import lombok.Value;

@Value
public class MyInfoResponse {
	String nickname;
	String email;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;

	public static MyInfoResponse create(Member member) {
		return new MyInfoResponse(
			member.getNickname(),
			member.getEmail(),
			member.getCreatedAt(),
			member.getUpdatedAt()
		);
	}
}

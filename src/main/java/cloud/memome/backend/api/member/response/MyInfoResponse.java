package cloud.memome.backend.api.member.response;

import java.time.LocalDateTime;

import cloud.memome.backend.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema(description = "로그인한 사용자의 계정 정보 응답 DTO")
@Value
public class MyInfoResponse {
	@Schema(description = "사용자 닉네임", example = "alice")
	String nickname;

	@Schema(description = "사용자 이메일", example = "alice@example.com")
	String email;

	@Schema(description = "계정 생성 일시", example = "2026-04-05T03:42:19.212")
	LocalDateTime createdAt;

	@Schema(description = "계정 정보 마지막 수정 일시", example = "2026-04-05T03:42:19.212")
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

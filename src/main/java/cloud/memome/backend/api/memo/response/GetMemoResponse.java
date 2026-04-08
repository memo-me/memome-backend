package cloud.memome.backend.api.memo.response;

import java.time.LocalDateTime;

import cloud.memome.backend.domain.memo.Memo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema(description = "특정 메모의 세부 정보 조회 응답 DTO")
@Value
public class GetMemoResponse {
	@Schema(description = "메모의 아이디", example = "24")
	Long id;

	@Schema(description = "메모의 제목", example = "hello world")
	String title;

	@Schema(description = "메모의 본문", example = "lorem ipsum")
	String body;

	@Schema(description = "메모 생성 일시", example = "2026-04-05T03:42:19.212")
	LocalDateTime createdAt;

	@Schema(description = "메모 최종 수정 일시", example = "2026-04-05T03:42:19.212")
	LocalDateTime updatedAt;

	public static GetMemoResponse create(Memo memo) {
		return new GetMemoResponse(
			memo.getId(),
			memo.getTitle(),
			memo.getBody(),
			memo.getCreatedAt(),
			memo.getUpdatedAt()
		);
	}
}

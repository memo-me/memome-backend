package cloud.memome.backend.api.memo.response;

import java.time.LocalDateTime;

import cloud.memome.backend.domain.memo.Memo;
import lombok.Value;

@Value
public class GetMemoResponse {
	Long id;
	String title;
	String body;
	LocalDateTime createdAt;
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

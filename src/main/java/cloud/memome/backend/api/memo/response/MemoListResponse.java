package cloud.memome.backend.api.memo.response;

import java.time.LocalDateTime;
import java.util.List;

import cloud.memome.backend.domain.memo.Memo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema(description = "현재 로그인한 사용자가 작성한 모든 메모 리스트 조회 응답 DTO")
@Value
public class MemoListResponse {
	@Schema(description = "현재 로그인한 사용자가 작성한 모든 메모의 개수", example = "3")
	int count;

	@ArraySchema(
		schema = @Schema(implementation = MemoSummary.class),
		arraySchema = @Schema(description = "현재 로그인한 사용자가 작성한 모든 메모의 요약 리스트")
	)
	List<MemoSummary> memoSummaryList;

	public MemoListResponse(List<MemoSummary> memoSummaryList) {
		this.count = memoSummaryList.size();
		this.memoSummaryList = memoSummaryList;
	}

	@Schema(description = "메모의 요약 정보")
	@Value
	public static class MemoSummary {
		private static final Integer SUMMARY_LEN = 10;
		private static final String SUMMARY_ELLIPSIS = "...";
		private static final Integer SUMMARY_PREFIX_LEN = SUMMARY_LEN - SUMMARY_ELLIPSIS.length();

		@Schema(description = "메모의 아이디", example = "24")
		Long id;

		@Schema(description = "메모의 제목", example = "hello world")
		String title;

		@Schema(description = "메모의 본문", example = "Lorem i...")
		String bodySummary;

		@Schema(description = "메모 생성 일시", example = "2026-04-05T03:42:19.212")
		LocalDateTime createdAt;

		@Schema(description = "메모 최종 수정 일시", example = "2026-04-05T03:42:19.212")
		LocalDateTime updatedAt;

		public MemoSummary(Memo memo) {
			this.id = memo.getId();
			this.title = memo.getTitle();
			this.bodySummary = getSummary(memo.getBody());
			this.createdAt = memo.getCreatedAt();
			this.updatedAt = memo.getUpdatedAt();
		}

		private String getSummary(String body) {
			return body.length() > SUMMARY_LEN
				? body.substring(0, SUMMARY_LEN) + SUMMARY_ELLIPSIS
				: body;
		}
	}
}

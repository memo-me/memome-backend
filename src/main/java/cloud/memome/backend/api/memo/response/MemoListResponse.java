package cloud.memome.backend.api.memo.response;

import java.time.LocalDateTime;
import java.util.List;

import cloud.memome.backend.domain.memo.Memo;
import lombok.Value;

@Value
public class MemoListResponse {
	int count;
	List<MemoSummary> memoSummaryList;

	public MemoListResponse(List<MemoSummary> memoSummaryList) {
		this.count = memoSummaryList.size();
		this.memoSummaryList = memoSummaryList;
	}

	@Value
	public static class MemoSummary {
		private static final Integer SUMMARY_LEN = 10;
		private static final String SUMMARY_ELLIPSIS = "...";
		private static final Integer SUMMARY_PREFIX_LEN = SUMMARY_LEN - SUMMARY_ELLIPSIS.length();

		Long id;
		String title;
		String bodySummary;
		LocalDateTime createdAt;
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

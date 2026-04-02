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
		Long id;
		String title;
		String bodySummary;
		LocalDateTime createdAt;
		LocalDateTime updatedAt;

		public MemoSummary(Memo memo) {
			this.id = memo.getId();
			this.title = memo.getTitle();
			this.bodySummary = memo.getBody().length() > 10 ? memo.getBody().substring(0, 7) + "..." : memo.getBody();
			this.createdAt = memo.getCreatedAt();
			this.updatedAt = memo.getUpdatedAt();
		}
	}
}

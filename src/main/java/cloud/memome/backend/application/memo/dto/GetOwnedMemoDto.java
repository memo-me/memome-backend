package cloud.memome.backend.application.memo.dto;

import lombok.Value;

@Value
public class GetOwnedMemoDto {
	Long memoId;
	Long authorId;
}

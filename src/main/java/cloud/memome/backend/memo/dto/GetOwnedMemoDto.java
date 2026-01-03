package cloud.memome.backend.memo.dto;

import lombok.Value;

@Value
public class GetOwnedMemoDto {
	Long memoId;
	Long authorId;
}

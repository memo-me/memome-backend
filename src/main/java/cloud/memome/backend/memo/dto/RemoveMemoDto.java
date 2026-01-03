package cloud.memome.backend.memo.dto;

import lombok.Value;

@Value
public class RemoveMemoDto {
	Long memoId;
	Long authorId;
}

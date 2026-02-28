package cloud.memome.backend.application.memo.dto;

import lombok.Value;

@Value
public class RemoveMemoDto {
	Long memoId;
	Long authorId;
}

package cloud.memome.backend.memo.dto;

import lombok.Value;

@Value
public class UpdateMemoDto {
	Long memoId;
	Long authorId;
	String title;
	String body;
}

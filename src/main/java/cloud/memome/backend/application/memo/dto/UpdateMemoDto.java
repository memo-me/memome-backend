package cloud.memome.backend.application.memo.dto;

import lombok.Value;

@Value
public class UpdateMemoDto {
	Long memoId;
	Long authorId;
	String title;
	String body;
}

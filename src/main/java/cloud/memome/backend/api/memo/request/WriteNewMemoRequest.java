package cloud.memome.backend.api.memo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Schema(description = "새로운 메모 작성 요청 DTO")
@Value
public class WriteNewMemoRequest {
	@Schema(description = "메모의 제목 (1자 이상)", example = "hello world")
	@NotBlank
	String title;

	@Schema(description = "메모의 본문 (1자 이상)", example = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
	@NotBlank
	String body;
}

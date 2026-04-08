package cloud.memome.backend.api.memo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Schema(description = "특정 메모에 대한 수정 요청 DTO")
@Value
public class ModifyMemoRequest {
	@Schema(description = "변경할 메모 제목 (1자 이상)", example = "updated title")
	@NotBlank
	String title;

	@Schema(description = "변경할 메모 본문 (1자 이상)", example = "updated body")
	@NotBlank
	String body;
}

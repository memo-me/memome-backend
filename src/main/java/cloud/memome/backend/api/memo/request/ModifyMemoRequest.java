package cloud.memome.backend.api.memo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class ModifyMemoRequest {
	@NotBlank
	String title;
	@NotBlank
	String body;
}

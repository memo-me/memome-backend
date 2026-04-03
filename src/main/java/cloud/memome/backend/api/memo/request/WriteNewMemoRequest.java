package cloud.memome.backend.api.memo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class WriteNewMemoRequest {
	@NotBlank
	String title;
	@NotBlank
	String body;
}

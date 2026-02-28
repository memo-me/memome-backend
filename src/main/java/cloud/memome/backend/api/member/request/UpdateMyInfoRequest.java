package cloud.memome.backend.api.member.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UpdateMyInfoRequest {
	@NotBlank
	String nickname;
	@NotBlank
	@Email
	String email;
}

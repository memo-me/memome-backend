package cloud.memome.backend.member.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UpdateMyInfoRequest {
	@NotBlank
	String nickname;
	@Email
	@NotBlank
	String email;
}

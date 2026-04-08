package cloud.memome.backend.api.member.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Schema(description = "내 계정 정보 수정 요청 DTO")
@Value
public class UpdateMyInfoRequest {
	@Schema(
		description = "변경할 사용자 닉네임 (1자 이상, 공백 불가)",
		example = "updated nickname"
	)
	@NotBlank
	String nickname;

	@Schema(
		description = "변경할 사용자 이메일 (유효한 이메일 형식)",
		example = "updated@example.com"
	)
	@NotBlank
	@Email
	String email;
}

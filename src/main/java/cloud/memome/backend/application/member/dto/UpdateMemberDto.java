package cloud.memome.backend.application.member.dto;

import lombok.Value;

@Value
public class UpdateMemberDto {
	String nickname;
	String email;
}

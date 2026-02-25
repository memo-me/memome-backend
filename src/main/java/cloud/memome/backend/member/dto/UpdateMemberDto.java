package cloud.memome.backend.member.dto;

import lombok.Value;

@Value
public class UpdateMemberDto {
	String nickname;
	String email;
}

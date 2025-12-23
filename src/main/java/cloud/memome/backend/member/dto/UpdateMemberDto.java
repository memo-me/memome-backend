package cloud.memome.backend.member.dto;

import lombok.Value;

@Value
public class UpdateMemberDto {
	Long id;
	String nickname;
	String email;
}

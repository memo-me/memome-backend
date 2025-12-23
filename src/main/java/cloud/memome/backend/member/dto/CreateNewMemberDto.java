package cloud.memome.backend.member.dto;

import lombok.Value;

@Value
public class CreateNewMemberDto {
	String nickname;
	String email;
}

package cloud.memome.backend.member.dto;

import lombok.Data;

@Data
public class CreateNewMemberDto {
	private String nickname;
	private String email;
}

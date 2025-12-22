package cloud.memome.backend.member.dto;

import lombok.Data;

@Data
public class UpdateMemberDto {
	private Long id;
	private String nickname;
	private String email;
}

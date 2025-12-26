package cloud.memome.backend.member.dto;

import cloud.memome.backend.member.OAuthIdentity;
import lombok.Value;

@Value
public class CreateNewMemberDto {
	OAuthIdentity oAuthIdentity;
	String nickname;
	String email;
}

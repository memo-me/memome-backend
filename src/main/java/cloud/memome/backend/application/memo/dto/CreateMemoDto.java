package cloud.memome.backend.application.memo.dto;

import cloud.memome.backend.domain.member.Member;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(exclude = "author")
public class CreateMemoDto {
	String title;
	String body;
	Member author;
}

package cloud.memome.backend.memo.dto;

import cloud.memome.backend.member.Member;
import lombok.Value;

@Value
public class CreateMemoDto {
	String title;
	String body;
	Member author;
}

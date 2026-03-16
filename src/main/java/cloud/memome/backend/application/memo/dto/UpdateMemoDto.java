package cloud.memome.backend.application.memo.dto;

import cloud.memome.backend.domain.member.Member;
import lombok.Value;

@Value
public class UpdateMemoDto {
	Long memoId;
	Member author;
	String title;
	String body;
}

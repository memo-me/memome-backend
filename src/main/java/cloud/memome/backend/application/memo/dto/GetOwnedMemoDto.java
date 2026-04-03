package cloud.memome.backend.application.memo.dto;

import cloud.memome.backend.domain.member.Member;
import lombok.Value;

@Value
public class GetOwnedMemoDto {
	Long memoId;
	Member author;
}

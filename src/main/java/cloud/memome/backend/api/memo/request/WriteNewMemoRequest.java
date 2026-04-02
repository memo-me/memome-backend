package cloud.memome.backend.api.memo.request;

import lombok.Value;

@Value
public class WriteNewMemoRequest {
	String title;
	String body;
}

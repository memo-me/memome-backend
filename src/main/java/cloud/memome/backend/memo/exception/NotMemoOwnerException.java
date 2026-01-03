package cloud.memome.backend.memo.exception;

public class NotMemoOwnerException extends RuntimeException {
	public NotMemoOwnerException(Long memoId, Long authorId) {
		super("author(" + authorId + ") is not owner of memo(" + memoId + ")");
	}
}

package cloud.memome.backend.application.memo.exception;

public class MemoNotFoundException extends RuntimeException {
	public MemoNotFoundException() {
		super("Memo not found.");
	}
}

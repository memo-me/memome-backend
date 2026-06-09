package cloud.memome.backend.application.member.exception;

public class NoSuchMemberException extends RuntimeException {
	public NoSuchMemberException() {
		super("Member doesn't exist.");
	}
}

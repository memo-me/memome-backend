package cloud.memome.backend.application.member.exception;

public class InvalidAuthenticationException extends RuntimeException {
	public InvalidAuthenticationException() {
		super("Authentication user is no longer valid.");
	}
}

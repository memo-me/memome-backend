package cloud.memome.backend.application.member.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class InvalidAuthenticationException extends ErrorResponseException {
	public InvalidAuthenticationException() {
		super(HttpStatus.UNAUTHORIZED);
		setDetail("Authentication user is no longer valid.");
	}
}

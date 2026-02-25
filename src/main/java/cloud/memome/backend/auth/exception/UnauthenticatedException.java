package cloud.memome.backend.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class UnauthenticatedException extends ErrorResponseException {
	public UnauthenticatedException() {
		super(HttpStatus.UNAUTHORIZED);
		setDetail("Authentication is required.");
	}
}

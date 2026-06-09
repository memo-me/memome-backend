package cloud.memome.backend.api.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {
	public InvalidRefreshTokenException() {
		super("Invalid Refresh Token");
	}
}

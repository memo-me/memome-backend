package cloud.memome.backend.auth.exception;

public class InvalidPrincipal extends RuntimeException {
	public InvalidPrincipal(String expectedClassName, String actualClassName) {
		super("Expected " + expectedClassName
			+ ", but found " + actualClassName + " principal.");
	}
}

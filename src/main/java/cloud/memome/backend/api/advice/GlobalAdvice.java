package cloud.memome.backend.api.advice;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cloud.memome.backend.api.auth.exception.InvalidRefreshTokenException;
import cloud.memome.backend.application.member.exception.InvalidAuthenticationException;
import cloud.memome.backend.application.member.exception.NoSuchMemberException;
import cloud.memome.backend.application.memo.exception.MemoNotFoundException;
import io.jsonwebtoken.JwtException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice(basePackages = "cloud.memome.backend")
@RequiredArgsConstructor
@Order(-1)
public class GlobalAdvice {
	private final MessageSource messageSource;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
		List<FieldErrorDetail> errors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(err -> new FieldErrorDetail(err.getField(),
				messageSource.getMessage(err, LocaleContextHolder.getLocale())))
			.toList();

		ProblemDetail body = ProblemDetail.forStatus(exception.getStatusCode());
		body.setProperty("errors", errors);

		return createResponse(body, exception.getHeaders(), exception.getStatusCode());
	}

	@ExceptionHandler(InvalidAuthenticationException.class)
	public ResponseEntity<Object> invalidAuthenticationException(InvalidAuthenticationException exception) {
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
		return createResponse(body, HttpHeaders.EMPTY, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(JwtException.class)
	public ResponseEntity<Object> jwtException(JwtException exception) {
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid Jwt");
		return createResponse(body, HttpHeaders.EMPTY, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(NoSuchMemberException.class)
	public ResponseEntity<Object> noSuchMemberException(NoSuchMemberException exception) {
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		return createResponse(body, HttpHeaders.EMPTY, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MemoNotFoundException.class)
	public ResponseEntity<Object> memoNotFoundException(MemoNotFoundException exception) {
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		return createResponse(body, HttpHeaders.EMPTY, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<Object> invalidRefreshToken(InvalidRefreshTokenException exception) {
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		return createResponse(body, HttpHeaders.EMPTY, HttpStatus.NOT_FOUND);
	}

	private ResponseEntity<Object> createResponse(Object body, HttpHeaders headers, HttpStatusCode status) {
		return new ResponseEntity<>(body, headers, status);
	}

	@RequiredArgsConstructor
	@Getter
	public static class FieldErrorDetail {
		private final String field;
		private final String message;
	}
}

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

import cloud.memome.backend.application.member.exception.InvalidAuthenticationException;
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

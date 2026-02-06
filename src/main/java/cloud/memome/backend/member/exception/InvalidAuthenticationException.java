package cloud.memome.backend.member.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import cloud.memome.backend.member.OAuthIdentity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidAuthenticationException extends ErrorResponseException {
	public InvalidAuthenticationException(OAuthIdentity oAuthIdentity) {
		super(HttpStatus.UNAUTHORIZED);
		setDetail("Authentication user is no longer valid.");

		log.warn("Authentication principal is invalid: providerType={}, providerId={}",
			oAuthIdentity.getProviderType().name(), oAuthIdentity.getProviderId());
	}
}

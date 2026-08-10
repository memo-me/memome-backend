package cloud.memome.backend.infra.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import cloud.memome.backend.api.auth.Login;
import cloud.memome.backend.api.auth.LoginMember;
import cloud.memome.backend.infra.security.exception.UnauthenticatedException;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
		boolean hasLoginMemberType = LoginMember.class.isAssignableFrom(parameter.getParameterType());
		return hasLoginAnnotation && hasLoginMemberType;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getPrincipal() == null
			|| !(authentication instanceof UsernamePasswordAuthenticationToken
			&& authentication.getPrincipal() instanceof Long)) {
			throw new UnauthenticatedException();
		}

		return new LoginMember((Long)authentication.getPrincipal());
	}
}

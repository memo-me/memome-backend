package cloud.memome.backend.infra;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import cloud.memome.backend.auth.Login;
import cloud.memome.backend.auth.LoginMember;
import cloud.memome.backend.auth.OAuthUserInfo;
import cloud.memome.backend.auth.OAuthUserInfoResolver;
import cloud.memome.backend.auth.exception.UnauthenticatedException;

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
		if (authentication == null) {
			throw new UnauthenticatedException();
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof OAuth2User)) {
			throw new UnauthenticatedException();
		}

		OAuthUserInfo result = OAuthUserInfoResolver.resolve(((OAuth2User)principal).getAttributes());
		return new LoginMember(result.getProviderType(), result.getProviderId());
	}
}

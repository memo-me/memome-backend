package cloud.memome.backend.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import cloud.memome.backend.api.auth.LoginMember;
import cloud.memome.backend.infra.mvc.LoginMemberArgumentResolver;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {
	@Primary
	@Bean
	LoginMemberArgumentResolver loginMemberArgumentResolver() {
		return new LoginMemberArgumentResolver() {
			@Override
			public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
				NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
				return new LoginMember(1L);
			}
		};
	}
}

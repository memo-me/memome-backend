package cloud.memome.backend.auth;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import cloud.memome.backend.member.MemberService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MyOidcService extends OidcUserService {
	private final MemberService memberService;

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = super.loadUser(userRequest);

		OAuthUserInfo oAuthUserInfo = OAuthUserInfoResolver.resolve(oidcUser.getAttributes());

		memberService.getOrCreateMember(oAuthUserInfo);

		return oidcUser;
	}
}

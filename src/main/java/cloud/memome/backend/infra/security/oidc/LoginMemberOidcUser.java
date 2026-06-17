package cloud.memome.backend.infra.security.oidc;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LoginMemberOidcUser implements OidcUser {
	private final OidcUser oidcUser;
	private final Long memberId;

	@Override
	public Map<String, Object> getClaims() {
		return oidcUser.getClaims();
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return oidcUser.getUserInfo();
	}

	@Override
	public OidcIdToken getIdToken() {
		return oidcUser.getIdToken();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return oidcUser.getAttributes();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return oidcUser.getAuthorities();
	}

	@Override
	public String getName() {
		return oidcUser.getName();
	}
}

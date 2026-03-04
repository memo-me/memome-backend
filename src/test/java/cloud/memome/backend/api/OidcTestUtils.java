package cloud.memome.backend.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.net.URL;

import cloud.memome.backend.domain.member.OAuthIdentity;
import cloud.memome.backend.domain.member.ProviderType;

public class OidcTestUtils {
	public static OidcLoginRequestPostProcessor login(OAuthIdentity oAuthIdentity) {
		return oidcLogin().idToken(token -> token.claims(claims -> {
			claims.put("iss", changeProviderTypeToURL(oAuthIdentity.getProviderType()));
			claims.put("sub", oAuthIdentity.getProviderId());
		}));
	}

	private static URL changeProviderTypeToURL(ProviderType type) {
		URL url;
		try {
			url = new URL(type.getIssuer());
		} catch (Exception e) {
			throw new RuntimeException("WRONG URL FORMAT: " + type.getIssuer(), e);
		}
		return url;
	}
}

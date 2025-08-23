package ro.irisinfinity.users.config;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("sec")
public class SecurityExpressions {

    public boolean isSelf(UUID externalId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jat)) {
            return false;
        }
        Object userId = jat.getToken().getClaims().get("userId");
        return userId != null && externalId.toString().equals(String.valueOf(userId));
    }
}
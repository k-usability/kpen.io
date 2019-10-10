package io.kpen.util;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.UserInfo;
import com.auth0.net.Request;
import io.kpen.jooq.tables.records.PersonRecord;
import lombok.Data;
import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static io.kpen.jooq.Tables.PERSON;
import static io.kpen.util.Util.map2str;

public class Auth {

    @Data
    public static class User {
        public String sub;
        public String name;
        public String email;
    }

    public static User getUser(Authentication authentication) throws Auth0Exception {
        String domain = Config.get("APP_API_CLIENT_DOMAIN");
        String clientId = Config.get("APP_API_CLIENT_ID");
        String clientSecret = Config.get("APP_API_CLIENT_SECRET");
        AuthAPI auth = new AuthAPI(domain, clientId, clientSecret);

        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        System.out.println("Token attributes: " + map2str(token.getTokenAttributes()));
        System.out.println("Token name: " + token.getName());

        Jwt jwt = (Jwt) token.getPrincipal();
        System.out.println("Jwt claims: " + map2str(jwt.getClaims()));
        System.out.println("Jwt headers: " + map2str(jwt.getHeaders()));
        System.out.println("Credentials: " + token.getCredentials());
        System.out.println("Token value: " + jwt.getTokenValue());
        Request<UserInfo> request = auth.userInfo(jwt.getTokenValue());
        UserInfo userinfo = request.execute();
        System.out.println("User info: " + map2str(userinfo.getValues()));

        User user = new User();
        user.setSub(get(userinfo, "sub"));
        user.setEmail(get(userinfo, "email"));
        user.setName(get(userinfo, "name"));

        return user;
    }

    public static PersonRecord getPersonRecord(DSLContext ctx, Authentication authentication) throws Auth0Exception {
        return getPersonRecord(ctx, getUser(authentication));
    }

    public static PersonRecord getPersonRecord(DSLContext ctx, Auth.User user) {
        return ctx.fetchOne(PERSON, PERSON.AUTH0_SUB.eq(user.getSub()));
    }

    private static String get(UserInfo user, String key) {
        Object o = user.getValues().get(key);
        return o == null ? null : o.toString();
    }
}

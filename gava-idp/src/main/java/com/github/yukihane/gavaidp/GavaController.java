package com.github.yukihane.gavaidp;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ConcurrentReferenceHashMap.ReferenceType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/gavaidp")
public class GavaController {

    public static final String CLIENT_ID = "myclient";
    public static final String CLIENT_SECRET = "e3b8886b-5b6e-49a7-91c2-c28caadf0a2b";

    private static final ConcurrentReferenceHashMap<String, String> NONCES = new ConcurrentReferenceHashMap<>(200,
        ReferenceType.WEAK);
    private static final Date EXP;
    static {
        final Instant instant = LocalDateTime.of(2050, 12, 31, 23, 59).toInstant(ZoneOffset.UTC);
        EXP = Date.from(instant);
    }

    /**
     * <code>
     * http://localhost:8080/login/oauth2/code/myspring?state=-t1bljJx_gXUWyhOCPMBczvNQtO3aTZU-NDcTFamgQc%3D&session_state=f5c1d220-11ff-460e-bdd8-f98881621146&code=fd10234e-a9de-4223-85f2-c2145fa2a40d.f5c1d220-11ff-460e-bdd8-f98881621146.5860bae9-7c17-4ce8-b182-2e0b17bda1d9
     * </code>
     */
    @RequestMapping("/auth")
    public String auth(@RequestParam("state") final String state, @RequestParam("nonce") final String nonce) {
        final String code = UUID.randomUUID().toString();
        NONCES.put(code, nonce);
        return "redirect:http://localhost:8080/login/oauth2/code/myspring?code=" + code + "&state=" + state;
    }

    /**
     *
     * @see <a href=
     * "https://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html#TokenResponse">
     * 3.1.3.3. Successful Token Response</a>
     * @see <a href=
     * "https://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html#IDToken">
     * 2. ID Token</a>
     */
    @RequestMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> token(@RequestParam("code") final String code) {
        final Algorithm alg = Algorithm.HMAC256(CLIENT_SECRET);

        final String nonce = NONCES.remove(code);

        final String idToken = JWT.create()
            .withIssuer("https://gaba.example.com")
            .withSubject("dummy-subject-identifier")
            .withAudience(CLIENT_ID)
            .withExpiresAt(EXP)
            .withIssuedAt(new Date())
            .withClaim("nonce", nonce)
            .withClaim("preferred_username", "dummy_user_name")
            .sign(alg);

        final Map<String, Object> ret = new HashMap<>();
        ret.put("access_token", "dummy_access_token");
        ret.put("token_type", "Bearer");
        ret.put("refresh_token", "dummy_refresh_token");
        ret.put("expires_in", 3600);
        ret.put("id_token", idToken);

        return ret;
    }

    /**
     * token エンドポイントでエラーを返したい場合にはこちらに接続する。
     *
     * <a href=
     * "https://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html#TokenErrorResponse">
     * 3.1.3.4. Token Error Response</a>
     */
    @RequestMapping(value = "/token-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> tokenError() {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_request"));
    }

    @RequestMapping(value = "/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> userinfo() {
        final Map<String, Object> ret = new HashMap<>();
        ret.put("fav-number", 8);
        ret.put("sub", "dummy-subject-identifier");
        ret.put("preferred_username", "dummy_user_name");
        return ret;
    }

    /**
     * エラーを返す userinfo エンドポイント。
     *
     * @see <a href=
     * "https://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html#UserInfoError">
     * 5.3.3. UserInfo Error Response</a>
     * @see <a href=
     * "http://openid-foundation-japan.github.io/rfc6750.ja.html#authn-header">
     * 3. WWW-Authenticate レスポンスヘッダフィールド</a>
     */
    @RequestMapping(value = "/userinfo-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> userinfoError() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("WWW-Authenticate",
            "Bearer realm=\"example\", error=\"invalid_token\", error_description=\"The access token expired\"")
            .build();
    }

    @RequestMapping("/certs")
    public String certs() {
        throw new UnsupportedOperationException();
    }
}

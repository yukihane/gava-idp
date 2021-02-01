package com.example.oidcexample;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.web.client.RestTemplate;

// https://docs.spring.io/spring-security/site/docs/5.4.2/reference/html5/#oauth2login-provide-websecurityconfigureradapter
@EnableWebSecurity
public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorize -> authorize
                .antMatchers("/secure/**").authenticated())
            .oauth2Login(oauth2 -> {
                // https://docs.spring.io/spring-security/site/docs/5.4.2/reference/html5/#oauth2login-advanced
                oauth2.tokenEndpoint(endpoint -> {
                    endpoint.accessTokenResponseClient(accessTokenResponseClient());
                });
            });
    }

    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        final Logger logger = LoggerFactory
            .getLogger(DefaultAuthorizationCodeTokenResponseClient.class);

        // DefaultAuthorizationCodeTokenResponseClient デフォルトコンストラクタより
        final RestTemplate restTemplate = new RestTemplate(Arrays.asList(
            new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        restTemplate.setInterceptors(List.of(new RestTemplateLoggingInterceptor(logger)));

        final DefaultAuthorizationCodeTokenResponseClient ret = new DefaultAuthorizationCodeTokenResponseClient();
        ret.setRestOperations(restTemplate);
        return ret;
    }

    // https://docs.spring.io/spring-security/site/docs/5.4.2/reference/html5/#oauth2login-advanced-idtoken-verify
    // 共有鍵暗号方式のIdPを模しているためこの設定を行っている
    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
        final OidcIdTokenDecoderFactory idTokenDecoderFactory = new OidcIdTokenDecoderFactory();
        idTokenDecoderFactory.setJwsAlgorithmResolver(clientRegistration -> {
            return MacAlgorithm.HS256;
        });
        return idTokenDecoderFactory;
    }

    // https://yukihane.github.io/blog/202009/12/oidc-userinfo-not-fetched-for-custom-claims/
    // https://github.com/spring-projects/spring-security/issues/6886
    // 必ず userinfo エンドポイントにアクセスさせるための設定
    @Bean
    OidcUserService oidcUserService() {
        final OidcUserService ret = new OidcUserService();
        ret.setAccessibleScopes(Set.of());
        return ret;
    }
}

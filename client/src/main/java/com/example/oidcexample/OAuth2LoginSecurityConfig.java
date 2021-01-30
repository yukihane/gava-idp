package com.example.oidcexample;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;

// https://docs.spring.io/spring-security/site/docs/5.4.2/reference/html5/#oauth2login-provide-websecurityconfigureradapter
@EnableWebSecurity
public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorize -> authorize
                .antMatchers("/secure/**").authenticated())
            .oauth2Login(withDefaults());
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

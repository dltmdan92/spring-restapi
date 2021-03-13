package com.seungmoo.springrestapi.config;

import com.seungmoo.springrestapi.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AccountService accountService;

    @Autowired
    TokenStore tokenStore;

    @Autowired
    AppProperties appProperties;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // password 확인용 encoder
        security.passwordEncoder(passwordEncoder);
    }

    /**
     * 클라이언트 정보
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 가장 이상적인 것은 inMemory 보다는 jdbc 통해서 DB에서 토큰 관리하는게 좋다.
        clients.inMemory()
                // clientId : 애플리케이션의 public 한 ID
                .withClient(appProperties.getClientId())
                // 지원하는 Grant Type은 password / refresh_token 타입 총 두개 이다.
                // refresh_token 은 auth token을 발급받을 때 refresh_token도 같이 발급해주는데
                // 이 refresh_token을 갖고 새로운 access_token을 발급 받는 타입이다.
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("read", "write")
                // 여기서 pass 값은 client가 confidential client 인 경우 -> (각 서버에서 run하는 webapp 등)
                // 본서버에 접근하기 위해 서로 secret한 값을 공유한다.
                .secret(this.passwordEncoder.encode(appProperties.getClientSecret()))
                .accessTokenValiditySeconds(10 * 60)
                .refreshTokenValiditySeconds(6 * 10 * 60);
    }

    // 마지막으로 인증서버 endpoint에
    // authenticationManager, userDetailsService, tokenStore를 등록한다.
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(accountService)
                .tokenStore(tokenStore);
    }
}

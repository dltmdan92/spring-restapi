package com.seungmoo.springrestapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

/**
 * Resource Server는 보통 OAuth 서버와 같이 연동돼서 사용한다.
 * 외부 요청이 Resource를 필요로 할때, OAuth 서버에서 제공하는 토큰 서비스를 받고
 * 리소소 서버에서 토큰 정보를 보고 인증정보가 있는지 확인 한다.
 *
 * 인증서버는 리소스 서버는 따로 떨어져 나가는게 맞다 (큰 서비스에서는)
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("event");
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .anonymous() // anonymous 허용
                    .and()
                .authorizeRequests()
                    .mvcMatchers(HttpMethod.GET, "/api/**")
                        .anonymous() // get 요청의 api들은 anonymous 허용
                    .anyRequest()
                        .authenticated() // 다른 요청들은 인증을 필요로 함.
                    .and()
                .exceptionHandling()
                    // Resource 서버에서 AccessDenied가 뜨면 OAuth2AccessDeniedHandler로 처리한다.
                    .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}

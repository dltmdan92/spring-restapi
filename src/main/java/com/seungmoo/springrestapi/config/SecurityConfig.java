package com.seungmoo.springrestapi.config;

import com.seungmoo.springrestapi.accounts.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @EnableWebSecurity와 WebSecurityConfigurerAdapter
 * 위 두개를 적용하는 순간 SpringBoot의 시큐리티 자동 설정은 더이상 적용되지 않는다.
 *
 * 우리가 정의하는 설정이 적용될 것임.
 *
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;

    private final PasswordEncoder passwordEncoder;

    /**
     * Token을 저장하는 곳
     * 실 운영환경이면 InMemory는 쓰지 말 것
     *
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    /**
     * AuthorizationServer, ResourceServer에서 AuthenticationManager를 참조할 수 있도록
     * AuthenticationManagerBean을 선언해준다. Bean으로 노출 해준다.
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * AuthenticationManager를 어떻게 만들지 Configure
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }

    // 스프링 시큐리티 적용 이전에 webFilter를 적용하는 것
    @Override
    public void configure(WebSecurity web) throws Exception {
        // 메인 인덱스 페이지는 보안 적용 제외
        web.ignoring().mvcMatchers("/docs/index.html");
        // 스프링 부트 웹서비스 (서블릿버전)에서 제공하는 기존 static 경로들에 대해 보안 제외 처리 (ex 파비콘)
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    // 위에서 Filter로 거르지 않았기 때문에 무조건 Spring-Security의 영역에 들어오게 됨
    // 스프링 시큐리티가 적용된 후에, 인증 정책을 설정 적용하는 것
    // 위에 필터로 거르지 않으면, static 리소스나 공개문서의 경우도 일단 Spring-Security의 FilterChain을 무조건 타게 된다 --> 리소스 비효율
    // 그러므로 상단에서 Filter를 선적용해준다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*http.authorizeRequests()
                .mvcMatchers("/docs/index.html").anonymous()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).anonymous();*/
        // 위의 정책들 적용된 후에, FilterChain을 타게 된다.

        http
            .anonymous()
                .and()
            .formLogin()
                .and()
            .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**").authenticated()
                .anyRequest().authenticated(); // 그외 나머지는 인증이 필요하다.
    }
}

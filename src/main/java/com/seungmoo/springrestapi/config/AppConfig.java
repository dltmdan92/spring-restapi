package com.seungmoo.springrestapi.config;

import com.seungmoo.springrestapi.accounts.Account;
import com.seungmoo.springrestapi.accounts.AccountRole;
import com.seungmoo.springrestapi.accounts.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * passwordEncoder를 설정한다.
     * default로는 bcrypt를 제공한다.
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 앱이 구동될 때, 자동으로 User 하나 만들어줌
     * @return
     */
    //@Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {
            @Autowired
            AccountService accountService;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account seungmoo = Account.builder()
                        .email("seungmoo@gmail.com")
                        .password("seungmoo")
                        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                        .build();
                accountService.saveAccount(seungmoo);
            }
        };
    }

}

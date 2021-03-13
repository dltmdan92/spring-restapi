package com.seungmoo.springrestapi.accounts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    UserDetailsService accountService;

    @Autowired
    AccountRepository accountRepository;

    @DisplayName("로그인 인증 성공 테스트")
    @Test
    void findByUsername() {
        String username = "seungmoo@gmail.com";
        String password = "seungmoo";
        // Given
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        this.accountRepository.save(account);

        // When
        UserDetails userDetails = accountService.loadUserByUsername(username);

        // Then
        assertThat(userDetails.getPassword()).isEqualTo(password);
    }

    @DisplayName("로그인 인증 실패 테스트 - 잘못된 username")
    @Test
    void findByUsernameFail() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> accountService.loadUserByUsername("random@random.com"));
    }

}
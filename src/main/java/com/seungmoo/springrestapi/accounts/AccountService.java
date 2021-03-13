package com.seungmoo.springrestapi.accounts;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public Account saveAccount(Account account) {
        account.setPassword(this.passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
        // spring security는 User 정보를 UserDetails라는 Type으로 알아 먹을 수 있음
        // Account -> UserDetails로 변환

        // UserDetails의 구현체 중 User 객체를 통해 편리하게 계정 객체를 만들 수 있다.
        //return new User(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
        // user를 상속받는 AccountAdapeter를 리턴하고, Controller에서는 이제 Account를 받을 수 있게 된다.
        return new AccountAdapter(account);
    }

    private Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_"+role.name())).collect(Collectors.toSet());
    }
}

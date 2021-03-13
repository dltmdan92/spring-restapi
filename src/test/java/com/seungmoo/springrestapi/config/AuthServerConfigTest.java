package com.seungmoo.springrestapi.config;

import com.seungmoo.springrestapi.accounts.Account;
import com.seungmoo.springrestapi.accounts.AccountRole;
import com.seungmoo.springrestapi.accounts.AccountService;
import com.seungmoo.springrestapi.common.BaseControllerTest;
import com.seungmoo.springrestapi.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        // Given
        String clientId = "myApp";
        String clientSecret = "pass";

        String username = "seungmoo@gmail.com";
        String password = "seungmoo";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(account);

        // oauth를 셋팅하면, /oauth/token 링크에서 access_token을 발급해준다.
        this.mockMvc.perform(post("/oauth/token")
                    // HEADER에 clientId, clientSecret를 넣어주고
                    .with(httpBasic(clientId, clientSecret))
                    // requestBody(param)에는 username, password, grant-type을 넣어준다.
                    .param("username", username)
                    .param("password", password)
                    .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }

}
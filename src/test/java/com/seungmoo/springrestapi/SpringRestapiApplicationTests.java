package com.seungmoo.springrestapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // profile 설정을 통해, application-test.properties 파일을 사용할 수 있다.
class SpringRestapiApplicationTests {

    @Test
    void contextLoads() {
    }

}

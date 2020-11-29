package com.seungmoo.springrestapi.common;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

// RestDocMockMvc 커스터마이징 --> RestDocsMockMvcConfigurationCustomizer 를 구현한 빈 등록
@TestConfiguration // Test에서만 사용하는 Configuration이다.
public class RestDocsConfiguration {
    // Spring REST DOCS의 snippets를 prettyPrint 하게 하기 위해서 셋팅한다.
    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
        return configurer -> configurer.operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint());
    }
}

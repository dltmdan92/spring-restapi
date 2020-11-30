package com.seungmoo.springrestapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest // 오히려 SlicingTest가 Mocking해야 되는게 많아서 더 불편할 수 있다.
@AutoConfigureMockMvc
@AutoConfigureRestDocs // spring REST DOCS를 적용
@Import(RestDocsConfiguration.class) // 별도로 셋팅한 Bean 설정을 Import 하는 방법
@ActiveProfiles("test") // profile 설정을 통해, application-test.properties 파일을 사용할 수 있다.
@Ignore // 얘는 TestClass가 아님을 선언함.
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc; // Mocking된 DispatcherServlet으로 테스트를 수행한다. 진짜 DispatcherServlet을 띄우는 것보단 가볍다.

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;
}

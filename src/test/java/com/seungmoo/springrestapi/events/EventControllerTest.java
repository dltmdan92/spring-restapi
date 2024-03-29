package com.seungmoo.springrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seungmoo.springrestapi.accounts.AccountRepository;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest // Web용 Slicing Test // EventControllerTestNonSlicing에서 수행할 것
@ActiveProfiles("test") // profile 설정을 통해, application-test.properties 파일을 사용할 수 있다.
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc; // Mocking된 DispatcherServlet으로 테스트를 수행한다. 진짜 DispatcherServlet을 띄우는 것보단 가볍다.

    @MockBean
    EventRepository eventRepository; // @WebMvcTest 슬라이스 테스트 때문에 JpaRepository는 Bean 생성 안해줌 --> MockBean하나 만들자

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Test
    public void createEvent() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 14, 46, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 15, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2020, 11, 25, 14, 0, 0))
                .endEventDateTime(LocalDateTime.of(2020, 11, 24, 15, 0, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .free(true)
                .offline(false)
                .build();

        // MockBean은 return을 하지 않는다. 그냥 null이 리턴될 뿐이다.
        // Mockito를 통해 조건을 만들어 준다.
        // But Controller에서 newEvent객체를 따로 생성했으므로 이제 반응하지 않는다. (객체가 다름)
        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON) // HyperText Application Language (HATEOAS, 링크 정의)
                .content(objectMapper.writeValueAsString(event)) // event 요 객체를 문자열로 바꿔서 body에 넣어준다.
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(Matchers.not(true)));
    }

}

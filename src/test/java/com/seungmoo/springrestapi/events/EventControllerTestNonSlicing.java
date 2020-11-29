package com.seungmoo.springrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seungmoo.springrestapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest // 오히려 SlicingTest가 Mocking해야 되는게 많아서 더 불편할 수 있다.
@AutoConfigureMockMvc
public class EventControllerTestNonSlicing {
    @Autowired
    MockMvc mockMvc; // Mocking된 DispatcherServlet으로 테스트를 수행한다. 진짜 DispatcherServlet을 띄우는 것보단 가볍다.

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 입력값 이외의 파라미터는 무시하는 테스트
     *
     * Mocking 이 동작하지 않는 테스트
     * 컨트롤러에서 JPARepository 사용할 때, 별도의 객체를 생성해서 사용하므로
     * Mockito가 작동하지 않게 되었다. (다른 객체이기 때문에)
     * @throws Exception
     */
    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 14, 46, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 15, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2020, 11, 25, 14, 0, 0))
                .endEventDateTime(LocalDateTime.of(2020, 11, 30, 15, 0, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();

        mockMvc.perform(post("/api/events/")
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
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(false))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.toString()))
                // Spring HATEOAS를 통해 CLient는 링크 정보를 받고, Link를 통해 다른 Status로 전이할 수 있다.
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists());

    }

    /**
     * 입력값 이외 발생 시 Error를 발생시킨다. (프로퍼티에서 별도 설정)
     *
     * @throws Exception
     */
    @Test
    @TestDescription("입력받을 수 없는 값이 파라미터로 들어온 경우에 에러가 발생")
    public void makeError() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 14, 46, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 15, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2020, 11, 25, 14, 0, 0))
                .endEventDateTime(LocalDateTime.of(2020, 11, 30, 15, 0, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON) // HyperText Application Language (HATEOAS, 링크 정의)
                .content(objectMapper.writeValueAsString(event)) // event 요 객체를 문자열로 바꿔서 body에 넣어준다.
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 비어 있는 경우에 에러가 발생")
    public void createEvent_Bad_Req_Empty_input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }


    /**
     * 파라미터의 값들이 이상한 경우
     * 1. 시작일자 > 종료일자
     * 2. basePrice > maxPrice
     * 일 때 어떻게 할까??
     * @throws Exception
     */
    @Test
    @TestDescription("입력 값이 잘못된 경우(규칙 위반) 에러가 발생하는 테스트")
    public void createEvent_Bad_Req_Wrong_input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 11, 26, 14, 46, 0))
                // closeEnrollmentDateTime이 beginEnrollmentDateTime보다 빠름
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 15, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2020, 11, 27, 14, 0, 0))
                // endEventDateTime이 beginEventDateTime보다 빠름
                .endEventDateTime(LocalDateTime.of(2020, 11, 24, 15, 0, 0))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists());
    }

}

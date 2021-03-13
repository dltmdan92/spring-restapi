package com.seungmoo.springrestapi.events;

import com.seungmoo.springrestapi.accounts.Account;
import com.seungmoo.springrestapi.accounts.AccountRepository;
import com.seungmoo.springrestapi.accounts.AccountRole;
import com.seungmoo.springrestapi.accounts.AccountService;
import com.seungmoo.springrestapi.common.BaseControllerTest;
import com.seungmoo.springrestapi.common.TestDescription;
import com.seungmoo.springrestapi.config.AppProperties;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTestNonSlicing extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    /**
     * in memory DB 이지만, 테스트가 돌때는 서로 DB가 공유가 된다.
     */
    //@Before
    public void setUp() {
        eventRepository.deleteAll();
        accountRepository.deleteAll();
    }

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
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearToken())
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
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.toString()))
                // Spring HATEOAS를 통해 CLient는 링크 정보를 받고, Link를 통해 다른 Status로 전이할 수 있다.
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                // SPRING REST DOCS --> API 문서화
                // SPRING REST DOCS를 통해 snippet 생성해보자   target/generated-snippets/
                //(target/generated-snippets/create-event 에 snippet 생성)
                .andDo(document("create-event",
                        links( // target/generated-snippets/create-event에 links라는 문서조각 생성
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of enrollment"),
                                fieldWithPath("beginEventDateTime").description("date time of close of enrollment"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type is hal+json type")
                        ),
                        relaxedResponseFields(
                                // relaxedResponseFields : relaxed prefix를 붙여서 응답의 일부분만 문서화 한다.
                                // 그냥 responseField를 쓰면 payload의 모든 부분을 문서화 하려고 함
                                // 문서화 파일 형식 : adoc 파일 (ascii doctor)
                                // relaxed의 단점은 정확한 문서화를 만들지 못한다는 점
                                // 위쪽에서 이미 documentation 했던 _links 들이 빠져 있으면 responseFields는 error를 발생한다.
                                // relaxedResponseFields로 사용하거나 OR responseFields를 쓰고 fieldWithPath()로 _links.self.href 등을 선언해준다.
                                // 가급적 relaxed 보다는 그냥 responseFields 쓰는게 좋다. (API 변경되었을 때 감지하기 위해..)
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of enrollment"),
                                fieldWithPath("beginEventDateTime").description("date time of close of enrollment"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                                fieldWithPath("free").description("it tells this event is free or not"),
                                fieldWithPath("offline").description("it tells this event is offline event or not"),
                                fieldWithPath("eventStatus").description("eventStatus"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query events"),
                                fieldWithPath("_links.update-event.href").description("link to update event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ));

    }

    private String getBearToken() throws Exception {
        return "Bearer " + getAccessToken();
    }

    // 이렇게 AccessToken을 꺼낸다.
    private String getAccessToken() throws Exception {
        // Given
        // Runner에서 이미 유저 만듬
        /*
        Account account = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(account);*/

        // oauth를 셋팅하면, /oauth/token 링크에서 access_token을 발급해준다.
        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                // HEADER에 clientId, clientSecret를 넣어주고
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                // requestBody(param)에는 username, password, grant-type을 넣어준다.
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));
        MockHttpServletResponse response = perform.andReturn().getResponse();
        String content = response.getContentAsString();
        JacksonJsonParser parser = new JacksonJsonParser();
        return parser.parseMap(content).get("access_token").toString();
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
                .header(HttpHeaders.AUTHORIZATION, getBearToken())
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
                    .header(HttpHeaders.AUTHORIZATION, getBearToken())
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
                .header(HttpHeaders.AUTHORIZATION, getBearToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                // JSON Array의 경우 unwrapped가 되지 않으므로 직접 매핑 해준다.
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].field").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("content[0].rejectedValue").exists());
    }

    /**
     * 페이징 처리된 list 조회 + list 의 각 요소들의 self _link 받기
     * @throws Exception
     */
    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generateEvent);
        // When
        this.mockMvc.perform(get("/api/events")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                // query-events 디렉토리 하위로 문서화 adoc 파일 생성
                // 자세한 문서화는 생략 함.
                // 자세한 문서화 참고 : createEvent 메서드 참고
                .andDo(document("query-events"));
    }

    private Event generateEvent(int i) {
        Event event = Event.builder()
                .name("event" + i)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 14, 46, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 11, 22, 15, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2020, 11, 25, 14, 0, 0))
                .endEventDateTime(LocalDateTime.of(2020, 11, 30, 15, 0, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return this.eventRepository.save(event);
    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception {
        // Given
        Event event = this.generateEvent(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                //get-an-event 디렉토리 하위로 문서화adoc 파일 생성
                // 자세한 문서화는 생략 함.
                // 자세한 문서화 참고 : createEvent 메서드 참고
                // 자세한 문서화는 생략 함.
                // 자세한 문서화 참고 : createEvent 메서드 참고
                .andDo(document("get-an-event"));
    }

    @Test
    @TestDescription("없는 이벤트를 조회했을 때 404 응답받기")
    public void getEvent404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events/11883"))
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception{
        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "Updated event";
        eventDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, getBearToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                // 문서화 하기 (links, snippet 등 더 추가해야 함.)
                // update-event 디렉토리 하위로 문서화adoc 파일 생성
                // 자세한 문서화는 생략 함.
                // 자세한 문서화 참고 : createEvent 메서드 참고
                .andDo(document("update-event"));

    }

    @Test
    @TestDescription("입력값이 비어 있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception{
        // 400 Bad Request

        // Given
        Event event = this.generateEvent(200);
        // 필수 입력 값들을 전부 다 빈값으로 보낸다.
        EventDto eventDto = new EventDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, getBearToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @TestDescription("입력값이 잘못되어 있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception{
        // 400 Bad Request

        // Given
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception{
        // 404 Not Found

        // Given
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", "12341")
                .header(HttpHeaders.AUTHORIZATION, getBearToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

}

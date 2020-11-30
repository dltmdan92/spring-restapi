package com.seungmoo.springrestapi.events;

import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    /**
     * 생성자 주입 방식
     * 생성자에 들어오는 파리미터가 Bean으로 등록되어 있으면, @Autowired 생략 가능하다.
     * @param eventRepository
     * @param eventValidator
     */
    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    /**
     * URI 를 생성하고 201 Response를 리턴하는 핸들러 메소드
     *
     * Location Header에 생성된 이벤트를 조회할 수 있는 URI 담겨준다.
     * id는 DB에 들어갈 때 자동생성된 값으로 나온다.
     *
     * <unknown 입력 값 무시하는 메소드>
     *     - 특정 입력값을 제외하고 ignore 한다.
     * API의 RequestParameter 입력값을 제한하기 위해
     * EventDto 객체를 "별도 생성"하여 사용한다. (id, free 여부 값 등은 파라미터로 받아선 안된다.)
     * 그리고 EventDto 객체를 Event로 매핑해서 사용하면 된다. (objectMapper 사용)
     *
     * <unknown 입력 값 있을 시 Bad Request 발생>
     *     1. API RequestParameter에 @Valid 선언해주기 --> spring-boot-starter-validation을 꼭 pom.xml에 선언해준다.
     *          Valid의 수행결과는 바로 옆에 선언된 Errors 파라미터에 포워딩 된다.
     *     2.
     *
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        // @Valid에서 발생한 Error의 결과가 Errors로 넘겨진다.
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors); // 오류 발생에 대한 content 본문을 body에 넣어서 보내주자.
        }

        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // EventDto 객체를 --> Event 객체로 옮겨보자 (ModelMapper를 통해 편하게 사용)
        Event event = modelMapper.map(eventDto, Event.class);
        event.update(); // 유/무료 갱신, 원래는 Service 쪽으로 로직 위임하는게 좋다.
        Event newEvent = this.eventRepository.save(event);
        WebMvcLinkBuilder selfLinkBuilder =  linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();

        // HATEOAS link를 만들어 보자    Spring Boot 에서 기본적으로 제공하는 기능 덕분에 HATEOAS 편하게 사용가능함.
        // ResourceSupport, Resource 등 백기선 강의에서 쓰던 것들이 deprecated 되었음
        // 현재는 ResourceSupport --> RepresentationModel
        // Resource --> EntityModel 이렇게 바뀜
        EntityModel<Event> eventEntityModel = EntityModel.of(event);
        eventEntityModel.add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
        eventEntityModel.add(linkTo(EventController.class).withRel("query-events"));
        eventEntityModel.add(selfLinkBuilder.withRel("update-event"));
        eventEntityModel.add(Link.of("/docs/index.html#resources-event-create").withRel("profile"));

        // 이렇게 하면 hal+json Content-Type 포맷으로 링크 정보들이 json에 출력된다. (_links)
        return ResponseEntity.created(createdUri).body(eventEntityModel);
    }

}

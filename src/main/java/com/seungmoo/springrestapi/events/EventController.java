package com.seungmoo.springrestapi.events;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    /**
     * 생성자 주입 방식
     * 생성자에 들어오는 파리미터가 Bean으로 등록되어 있으면, @Autowired 생략 가능하다.
     * @param eventRepository
     */
    public EventController(EventRepository eventRepository, ModelMapper modelMapper) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * URI 를 생성하고 201 Response를 리턴하는 핸들러 메소드
     *
     * Location Header에 생성된 이벤트를 조회할 수 있는 URI 담겨준다.
     * id는 DB에 들어갈 때 자동생성된 값으로 나온다.
     *
     * <입력값 제한하는 메소드>
     *     - 특정 입력값을 제외하고 ignore 한다.
     * API의 RequestParameter 입력값을 제한하기 위해
     * EventDto 객체를 별도 생성하여 사용한다. (id, free 여부 값 등은 파라미터로 받아선 안된다.)
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventDto eventDto) {

        // EventDto 객체를 --> Event 객체로 옮겨보자 (ModelMapper를 통해 편하게 사용)
        Event event = modelMapper.map(eventDto, Event.class);

        Event newEvent = this.eventRepository.save(event);
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        return ResponseEntity.created(createdUri).body(event);
    }

}

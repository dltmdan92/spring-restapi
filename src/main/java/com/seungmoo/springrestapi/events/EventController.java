package com.seungmoo.springrestapi.events;

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

    /**
     * 생성자 주입 방식
     * 생성자에 들어오는 파리미터가 Bean으로 등록되어 있으면, @Autowired 생략 가능하다.
     * @param eventRepository
     */
    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * URI 를 생성하고 201 Response를 리턴하는 핸들러 메소드
     *
     * Location Header에 생성된 이벤트를 조회할 수 있는 URI 담겨준다.
     * id는 DB에 들어갈 때 자동생성된 값으로 나온다.
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        Event newEvent = this.eventRepository.save(event);
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        return ResponseEntity.created(createdUri).body(event);
    }

}

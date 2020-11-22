package com.seungmoo.springrestapi.events;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 롬복 실습
 *
 * 롬복 애노테이션은 스프링 애노테이션 처럼 커스텀 애노테이션으로 묶음 처리 불가함.
 * 애노테이션 적어줘야 함.
 */
@Entity
@Getter @Setter // Getter, Setter 메소드 생성 해줌
@Builder // builder를 선언하고 컴파일 하면 여러 메서드가 자동 생성된다. (소스 참고)
@AllArgsConstructor @NoArgsConstructor // 생성자 생성 해줌
// EqualsAndHashCode 구현할 때 클래스 내 모든 필드를 사용한다.
// but 나중에 entity가 상호 참조하는 관계가 되면 EqualsAndHashCode에서 StackOverflow가 발생할 수 있음 (서로 간의 객체에서 메소드를 서로 호출하게 된다.)
// 아래와 같이 EqualsAndHashCode는 id만 갖고 구현되게 하는게 좋다.
@EqualsAndHashCode(of = "id")
public class Event {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional) 이게 없으면 온라인 모임
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @Enumerated(EnumType.STRING) // EnumType은 String으로 해줘야 한다. (Ordinal은 순서로 저장 --> 위험!)
    private EventStatus eventStatus = EventStatus.DRAFT; // 기본값은 DRAFT로 설정

    public void update() {
        // Update free
        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        } else {
            this.free = false;
        }
        // Update offline
        // isBlank - Java 11 버전에서 추가 되었음 (이전에는 String을 trim한 후에 isEmpty로 확인했었음)
        // isBlank : 문자열 체크 (공백문자열 까지 체크)
        if (this.location == null || this.location.isBlank()) {
            this.offline = false;
        } else {
            this.offline = true;
        }
    }
}

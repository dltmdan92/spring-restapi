package com.seungmoo.springrestapi.events;


import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
@ActiveProfiles("test") // profile 설정을 통해, application-test.properties 파일을 사용할 수 있다.
public class EventTest {

    @Test
    public void builder() {
        Event event = Event.builder()
                .name("Inflearn Spring REST API")
                .description("REST API development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        Event event = new Event();
        String name = "Event";
        String description = "Spring";
        event.setName(name);
        event.setDescription(description);

        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }

    /**
     * parameter 메서드를 명시하거나, 메서드명 컨벤션을 통해 명시 X 할 수 있다.
     */
    @Test
    //@Parameters(method = "paramsForTestFree") // parameters를 활용한 테스트를 통해 중복코드 제거 가능하다.
    @Parameters
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        // Given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    // parametersFor가 메서드에 대한 컨벤션이다.
    private Object[] parametersForTestFree() {
        return new Object[][] {
            new Object[] {0, 0, true},
            new Object[] {100, 0, false},
            new Object[] {0, 100, false},
            new Object[] {100, 200, false}
        };
    }

    @Test
    @Parameters
    public void testOffline(String location, boolean isOffline) {
        // Given
        Event event = Event.builder()
                .location(location)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isEqualTo(isOffline);
    }

    private Object[][] parametersForTestOffline() {
        return new Object[][] {
                new Object[] {"강남", true},
                new Object[] {null, false},
                new Object[] {"      ", false},
        };
    }

}
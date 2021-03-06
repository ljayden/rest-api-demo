package com.wonana.restapi.events;

import com.wonana.restapi.common.BaseControllerTest;
import com.wonana.restapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;


    @Test
    @TestDescription("정상적으로 작동하는 테스트")
    public void createEvent() throws Exception {

        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("Rest API")
                .location("우리집")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,05,12,03,10))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,05,13,03,10))
                .beginEventDateTime(LocalDateTime.of(2019,05,14,10,00))
                .endEventDateTime(LocalDateTime.of(2019,05,14,12,00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();
//        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON_UTF8_VALUE)
                    .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists("Location"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(Matchers.not(EventStatus.PUBLISHED)))
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("updated-event").description("link to update event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Name of new description"),
                                fieldWithPath("beginEnrollmentDateTime").description("Name of new beginEnrollmentDateTime"),
                                fieldWithPath("closeEnrollmentDateTime").description("Name of new beginEnrollmentDateTime"),
                                fieldWithPath("beginEventDateTime").description("Name of new beginEventDateTime"),
                                fieldWithPath("endEventDateTime").description("Name of new endEventDateTime"),
                                fieldWithPath("location").description("Name of new location"),
                                fieldWithPath("basePrice").description("Name of new basePrice"),
                                fieldWithPath("maxPrice").description("Name of new maxPrice"),
                                fieldWithPath("limitOfEnrollment").description("Name of new limitOfEnrollment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("저장된 이벤트의 위치"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("hal json type")
                        ),
//                        responseFields(
                        relaxedResponseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Name of new description"),
                                fieldWithPath("beginEnrollmentDateTime").description("Name of new beginEnrollmentDateTime"),
                                fieldWithPath("closeEnrollmentDateTime").description("Name of new beginEnrollmentDateTime"),
                                fieldWithPath("beginEventDateTime").description("Name of new beginEventDateTime"),
                                fieldWithPath("endEventDateTime").description("Name of new endEventDateTime"),
                                fieldWithPath("location").description("저장된 이벤트의 위치"),
                                fieldWithPath("basePrice").description("Name of new basePrice"),
                                fieldWithPath("maxPrice").description("Name of new maxPrice"),
                                fieldWithPath("limitOfEnrollment").description("Name of new limitOfEnrollment"),
                                fieldWithPath("free").description("it tells if this event is offline event or free"),
                                fieldWithPath("offline").description("it tells if this event is offline event"),
                                fieldWithPath("eventStatus").description("it tells if this event is offline event or free"),
                                //relax 아닐떄
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.query-events.href").description("query-events"),
                                fieldWithPath("_links.updated-event.href").description("updated-event"),
                                fieldWithPath("_links.profile.href").description("profile")
                        )
                ))
        ;
    }

    @Test
    @TestDescription("sping.jackson.deserialization.unknown.parameter 일때 badRequest 지정값외 더 들어올때")
    public void createEvent_Bad_Request() throws Exception {

        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("Rest API")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,05,12,03,10))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,05,13,03,10))
                .beginEventDateTime(LocalDateTime.of(2019,05,14,10,00))
                .endEventDateTime(LocalDateTime.of(2019,05,14,12,00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();
//        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON_UTF8_VALUE)
                .content(this.objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("지정한 널이면 안되는 값이 널일때")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {

        EventDto eventDto = EventDto.builder()
                .build();
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 이상할때:비지니스 로직에 안맞을때")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {

        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("Rest API")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,05,12,03,10))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,05,13,03,10))
                .beginEventDateTime(LocalDateTime.of(2019,05,14,10,00))
                .endEventDateTime(LocalDateTime.of(2019,05,14,12,00))
                .basePrice(2000)
                .maxPrice(100)
                .limitOfEnrollment(100)
                .location("강남역 사거리")
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].field").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("content[0].rejectedValue").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(i ->
                this.genereteEvent(i));

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/events")
                .param("page","1")
                .param("size","10")
                .param("sort","id,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"))
        ;
    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception {

        // Given
        Event event = this.genereteEvent(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"))
        ;
    }

    @Test
    @TestDescription("없는 이벤트를 조회했을 때 404 응답받기")
    public void getEvent404() throws Exception {

        // When & Then
        this.mockMvc.perform(get("/api/events/11883"))
                .andExpect(status().isNotFound())
        ;

    }

    @Test
    @TestDescription("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception {
        // Given
        Event event = this.genereteEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "Update Event";
        eventDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 이벤트 수정 실패")
    public void updateEvent400() throws Exception {
        // Given
        Event event = this.genereteEvent(200);
        EventDto eventDto = new EventDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 잘몬된 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception {
        // Given
        Event event = this.genereteEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        // Given
        Event event = this.genereteEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        // When & Then
        this.mockMvc.perform(put("/api/events/123123")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    private Event genereteEvent(int i) {
        Event event = Event.builder()
                .name("event "+i)
                .description("Rest API")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,05,12,03,10))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,05,13,03,10))
                .beginEventDateTime(LocalDateTime.of(2019,05,14,10,00))
                .endEventDateTime(LocalDateTime.of(2019,05,14,12,00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();

        this.eventRepository.save(event);
        return event;
    }


}
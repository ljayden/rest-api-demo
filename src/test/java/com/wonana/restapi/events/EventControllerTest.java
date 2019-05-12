package com.wonana.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonana.restapi.common.RestDocsConfiguration;
import com.wonana.restapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
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
                                fieldWithPath("location").description("Name of new location"),
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

        // When
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


        // Then
    }

    private void genereteEvent(int i) {
        Event event = Event.builder()
                .name("event "+i)
                .description("test event")
                .build();

        this.eventRepository.save(event);
    }

}
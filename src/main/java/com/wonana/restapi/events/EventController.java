package com.wonana.restapi.events;

import com.wonana.restapi.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        if(errors.hasErrors()) {
            return badRequest(errors);
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) {
            return badRequest(errors);
        }


        Event event = modelMapper.map(eventDto,Event.class);
        event.update();
        Event savedEvent = this.eventRepository.save(event);
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(savedEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("updated-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));
        return ResponseEntity.created(createdUri).body(eventResource);
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = this.eventRepository.findAll(pageable);
        var pagedResources = assembler.toResource(page, event -> new EventResource(event));
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public  ResponseEntity getEvent(@PathVariable Integer id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);

        if (optionalEvent.isPresent() == true) {
            EventResource eventResource = new EventResource(optionalEvent.get());
            eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
            return ResponseEntity.ok(eventResource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    @PostMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id, @RequestBody EventDto eventDto) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);

        if (optionalEvent.isPresent() == true) {

            System.out.println("있음");
            Event event = optionalEvent.get();
            event.setName(eventDto.getName());
            event.setDescription(eventDto.getDescription());
            Event savedEvent = this.eventRepository.save(event);

            EventResource eventResource = new EventResource(savedEvent);
            eventResource.add(new Link("/docs/index.html#update-an-event").withRel("profile"));
            return ResponseEntity.ok(eventResource);
        } else {
            System.out.println("없음");
            return ResponseEntity.status(500).build();
        }

    }



}

package ro.irisinfinity.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.irisinfinity.events.service.EventsService;
import ro.irisinfinity.platform.common.dto.events.EventRequestDto;
import ro.irisinfinity.platform.common.dto.events.EventResponseDto;

@RestController
@RequestMapping(value = "/api/v1/events", produces = "application/json")
@RequiredArgsConstructor
public class EventsController {

    private final EventsService eventsService;

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponseDto createEvent(@RequestBody @Valid final EventRequestDto eventRequestDto) {
        return eventsService.createEvent(eventRequestDto);
    }

    @GetMapping("/{code}")
    public EventResponseDto getEventByCode(@PathVariable final String code) {
        return eventsService.getEventByCode(code);
    }

    @PostMapping("/{code}/join")
    @PreAuthorize("isAuthenticated()")
    public EventResponseDto joinEvent(
        @PathVariable final String code,
        @AuthenticationPrincipal final Jwt jwt
    ) {
        return eventsService.joinEvent(code, jwt);
    }
}
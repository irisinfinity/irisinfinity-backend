package ro.irisinfinity.events.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.irisinfinity.events.client.UsersClient;
import ro.irisinfinity.events.entity.Event;
import ro.irisinfinity.events.entity.Location;
import ro.irisinfinity.events.exception.EventAlreadyExistsException;
import ro.irisinfinity.events.exception.EventNotFoundException;
import ro.irisinfinity.events.repository.EventsRepository;
import ro.irisinfinity.events.repository.LocationRepository;
import ro.irisinfinity.platform.common.dto.events.EventRequestDto;
import ro.irisinfinity.platform.common.dto.events.EventResponseDto;
import ro.irisinfinity.platform.common.dto.events.LocationDto;
import ro.irisinfinity.platform.common.dto.users.UserResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

    private final EventsRepository eventsRepository;
    private final LocationRepository locationRepository;
    private final UsersClient usersClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public EventResponseDto createEvent(final EventRequestDto eventRequestDto) {
        if (eventsRepository.existsByCode(eventRequestDto.code())) {
            throw new EventAlreadyExistsException(
                "Event code already used: " + eventRequestDto.code());
        }

        Location location = objectMapper.convertValue(eventRequestDto.location(), Location.class);
        Location savedLocation = locationRepository.save(location);

        Event event = new Event();
        event.setCode(eventRequestDto.code());
        event.setName(eventRequestDto.name());
        event.setDate(eventRequestDto.date());
        event.setOwnerUserId(eventRequestDto.ownerUserId());
        event.setLocation(savedLocation);

        Event saved = eventsRepository.save(event);
        log.info("Event created: code={}", saved.getCode());

        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public EventResponseDto getEventByCode(final String code) {
        Event event = eventsRepository.findByCode(code).orElseThrow(EventNotFoundException::new);
        return mapToResponseDto(event);
    }

    @Transactional
    public EventResponseDto joinEvent(final String code, final Jwt jwt) {
        Event event = eventsRepository.findByCode(code).orElseThrow(EventNotFoundException::new);

        final UUID participantUserId = UUID.fromString((String) jwt.getClaims().get("userId"));
        event.getParticipants().add(participantUserId);
        Event saved = eventsRepository.save(event);

        log.info("Participant joined event: code={}, participant={}", code, participantUserId);
        return mapToResponseDto(saved);
    }

    private EventResponseDto mapToResponseDto(Event event) {
        List<UserResponseDto> participantDtos = event.getParticipants().stream()
            .map(usersClient::getUserByExternalId)
            .collect(Collectors.toList());

        Location location = event.getLocation();
        LocationDto locationDto = new LocationDto(location.getName(), location.getAddress());

        return new EventResponseDto(
            event.getId(),
            event.getCode(),
            event.getName(),
            event.getDate(),
            event.getOwnerUserId(),
            locationDto,
            participantDtos
        );
    }
}
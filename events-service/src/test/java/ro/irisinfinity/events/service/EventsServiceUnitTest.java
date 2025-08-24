package ro.irisinfinity.events.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.Jwt;
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

class EventsServiceUnitTest {

    @InjectMocks
    private EventsService eventsService;

    @Mock
    private EventsRepository eventsRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UsersClient usersClient;

    @Mock
    private ObjectMapper objectMapper;

    private EventRequestDto req;
    private LocationDto locationDto;
    private Location locationEntity;
    private Event eventEntity;

    private final String code = "SPR25";
    private final UUID ownerId = UUID.randomUUID();
    private final LocalDate date = LocalDate.now().plusDays(1);

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        locationDto = new LocationDto("Tech Hub", "Bucharest");
        req = new EventRequestDto(code, "Spring Conf", ownerId, date, locationDto);

        locationEntity = new Location();
        locationEntity.setId(10L);
        locationEntity.setName("Tech Hub");
        locationEntity.setAddress("Bucharest");

        eventEntity = new Event();
        eventEntity.setId(1L);
        eventEntity.setCode(code);
        eventEntity.setName("Spring Conf");
        eventEntity.setDate(date);
        eventEntity.setOwnerUserId(ownerId);
        eventEntity.setLocation(locationEntity);
    }

    @Test
    @DisplayName("createEvent should persist and return mapped response")
    void createEvent_success() {
        when(eventsRepository.existsByCode(code)).thenReturn(false);
        when(objectMapper.convertValue(locationDto, Location.class)).thenReturn(locationEntity);
        when(locationRepository.save(locationEntity)).thenReturn(locationEntity);
        when(eventsRepository.save(any(Event.class))).thenReturn(eventEntity);

        EventResponseDto resp = eventsService.createEvent(req);

        assertEquals(code, resp.code());
        assertEquals("Spring Conf", resp.name());
        assertEquals(ownerId, resp.ownerUserId());
        assertEquals("Tech Hub", resp.location().name());
    }

    @Test
    @DisplayName("createEvent should throw when code already exists")
    void createEvent_duplicateCode_shouldThrow() {
        when(eventsRepository.existsByCode(code)).thenReturn(true);
        assertThrows(EventAlreadyExistsException.class, () -> eventsService.createEvent(req));
    }

    @Test
    @DisplayName("getEventByCode should return mapped response when found")
    void getEventByCode_found() {
        when(eventsRepository.findByCode(code)).thenReturn(Optional.of(eventEntity));

        EventResponseDto resp = eventsService.getEventByCode(code);
        assertEquals(code, resp.code());
        assertEquals("Tech Hub", resp.location().name());
    }

    @Test
    @DisplayName("getEventByCode should throw when not found")
    void getEventByCode_notFound() {
        when(eventsRepository.findByCode(code)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> eventsService.getEventByCode(code));
    }

    @Test
    @DisplayName("joinEvent should add participant and map to UserResponseDto via UsersClient")
    void joinEvent_success() {
        UUID participantId = UUID.randomUUID();
        eventEntity.getParticipants()
            .add(participantId);
        when(eventsRepository.findByCode(code)).thenReturn(Optional.of(eventEntity));
        when(eventsRepository.save(eventEntity)).thenReturn(eventEntity);

        UserResponseDto userResp = new UserResponseDto(
            participantId, "p@example.com", "P", "User",
            LocalDate.of(1990, 1, 1), ro.irisinfinity.platform.common.enums.Sex.MALE,
            null, true, Set.of()
        );
        when(usersClient.getUserByExternalId(participantId)).thenReturn(userResp);

        Jwt jwt = Jwt.withTokenValue("t")
            .header("alg", "none")
            .claim("userId", participantId.toString())
            .build();

        EventResponseDto resp = eventsService.joinEvent(code, jwt);

        assertEquals(code, resp.code());
        assertEquals(1, resp.participants().size());
        assertEquals("p@example.com", resp.participants().getFirst().email());
    }
}

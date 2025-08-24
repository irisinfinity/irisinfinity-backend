package ro.irisinfinity.events.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ro.irisinfinity.events.client.UsersClient;
import ro.irisinfinity.events.repository.EventsRepository;
import ro.irisinfinity.events.repository.LocationRepository;
import ro.irisinfinity.platform.common.dto.events.EventRequestDto;
import ro.irisinfinity.platform.common.dto.events.EventResponseDto;
import ro.irisinfinity.platform.common.dto.events.LocationDto;
import ro.irisinfinity.platform.common.dto.users.UserResponseDto;
import ro.irisinfinity.platform.common.enums.Sex;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class EventsServiceIntegrationTest {

    @Autowired
    private EventsService eventsService;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private LocationRepository locationRepository;

    @MockBean
    private UsersClient usersClient;

    @Test
    void createAndGetEvent_ByCode_shouldWork() {
        EventRequestDto req = new EventRequestDto(
            "SPR25",
            "Spring Conf",
            UUID.randomUUID(),
            LocalDate.now().plusDays(1),
            new LocationDto("Tech Hub", "Bucharest")
        );

        EventResponseDto created = eventsService.createEvent(req);
        assertEquals(req.code(), created.code());
        assertEquals(req.name(), created.name());
        assertEquals(req.location().name(), created.location().name());

        EventResponseDto fetched = eventsService.getEventByCode(req.code());
        assertEquals(req.code(), fetched.code());
        assertEquals("Tech Hub", fetched.location().name());
    }

    @Test
    void joinEvent_shouldAddParticipant_andReturnUserDetails() {
        EventRequestDto req = new EventRequestDto(
            "JOIN25",
            "Joinable",
            UUID.randomUUID(),
            LocalDate.now().plusDays(2),
            new LocationDto("Arena", "Bucharest")
        );
        eventsService.createEvent(req);

        UUID participantId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("t")
            .header("alg", "none")
            .claim("userId", participantId.toString())
            .build();

        UserResponseDto userResp = new UserResponseDto(
            participantId, "joiner@example.com", "Join", "Er",
            LocalDate.of(1990, 1, 1), Sex.FEMALE,
            null, true, Set.of()
        );
        when(usersClient.getUserByExternalId(participantId)).thenReturn(userResp);

        EventResponseDto afterJoin = eventsService.joinEvent(req.code(), jwt);

        assertEquals(req.code(), afterJoin.code());
        assertEquals(1, afterJoin.participants().size());
        assertEquals("joiner@example.com", afterJoin.participants().getFirst().email());

        var saved = eventsRepository.findByCode(req.code()).orElseThrow();
        assertTrue(saved.getParticipants().contains(participantId));
    }
}
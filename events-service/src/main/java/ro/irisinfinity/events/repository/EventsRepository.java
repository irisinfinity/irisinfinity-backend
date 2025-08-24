package ro.irisinfinity.events.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.irisinfinity.events.entity.Event;

public interface EventsRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = "location")
    Optional<Event> findByCode(String code);

    boolean existsByCode(String code);
}
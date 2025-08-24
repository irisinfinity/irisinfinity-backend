package ro.irisinfinity.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.irisinfinity.events.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Integer> {

}
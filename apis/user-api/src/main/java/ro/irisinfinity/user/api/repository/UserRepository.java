package ro.irisinfinity.user.api.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ro.irisinfinity.user.api.data.User;

public interface UserRepository extends JpaRepository<User, Long>,
    PagingAndSortingRepository<User, Long> {

    Optional<User> findUserByExternalId(UUID externalId);

    boolean existsUserByEmail(String email);
}

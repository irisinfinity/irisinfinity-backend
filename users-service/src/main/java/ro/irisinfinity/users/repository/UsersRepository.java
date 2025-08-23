package ro.irisinfinity.users.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ro.irisinfinity.users.entity.User;

public interface UsersRepository extends JpaRepository<User, Long>,

    PagingAndSortingRepository<User, Long> {

    Optional<User> findUserByExternalId(final UUID externalId);

    Optional<User> findUserByEmail(final String email);

    boolean existsUserByEmail(final String email);
}

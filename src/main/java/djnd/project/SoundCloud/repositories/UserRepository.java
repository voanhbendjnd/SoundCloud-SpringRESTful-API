package djnd.project.SoundCloud.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import djnd.project.SoundCloud.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String Email);

    User findByEmail(String email);
    User findByEmailIgnoreCase(String email);
    User findByEmailAndRefreshToken(String email, String refreshToken);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

}

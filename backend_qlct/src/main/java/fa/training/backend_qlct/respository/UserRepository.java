package fa.training.backend_qlct.respository;

import fa.training.backend_qlct.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// tuong tac truc tiep voi database
@Repository // giup spring tao repository
public interface UserRepository extends JpaRepository<Users, Long> {
 // JpaRepository la interface cua spring data jpa,
    // cung cap cac phuong thuc de tuong tac voi database, nhu save, findById, findAll, deleteById, ...

    Optional<Users> findByUsername(String username);
}

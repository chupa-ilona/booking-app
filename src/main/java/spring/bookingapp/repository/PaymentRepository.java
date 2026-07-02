package spring.bookingapp.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring.bookingapp.model.Payment;
import spring.bookingapp.model.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    Page<Payment> findAllByBookingUserId(Long userId, Pageable pageable);

    boolean existsByBookingUserIdAndStatus(Long userId, PaymentStatus status);

}

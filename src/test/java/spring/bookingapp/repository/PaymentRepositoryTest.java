package spring.bookingapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import spring.bookingapp.model.Payment;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Find Payment by Session ID")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql",
            "classpath:database/bookings/add-booking.sql",
            "classpath:database/payments/add-payment.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/remove-payments.sql",
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findBySessionId_GivenValidSessionId_ReturnsPayment() {
        // Given
        String validSessionId = "cs_test_123456789";

        // When
        Optional<Payment> actualPayment = paymentRepository.findBySessionId(validSessionId);

        // Then
        assertTrue(actualPayment.isPresent());
        assertEquals(validSessionId, actualPayment.get().getSessionId());
    }
}

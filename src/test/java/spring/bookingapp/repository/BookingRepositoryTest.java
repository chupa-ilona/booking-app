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
import spring.bookingapp.model.Booking;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Find booking by id")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql",
            "classpath:database/bookings/add-booking.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findById_GivenValidId_ReturnsBooking() {
        // Given
        Long bookingId = 1L;

        // When
        Optional<Booking> actualBooking = bookingRepository.findById(bookingId);

        // Then
        assertTrue(actualBooking.isPresent());
        assertEquals(bookingId, actualBooking.get().getId());
    }
}
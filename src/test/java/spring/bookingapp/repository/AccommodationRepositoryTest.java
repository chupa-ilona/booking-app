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
import spring.bookingapp.model.Accommodation;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccommodationRepositoryTest {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Test
    @DisplayName("Find accommodation by id")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findById_GivenValidId_ReturnsAccommodation() {
        // Given
        Long accommodationId = 1L;

        // When
        Optional<Accommodation> actualAccommodation = accommodationRepository.findById(accommodationId);

        // Then
        assertTrue(actualAccommodation.isPresent());
        assertEquals(accommodationId, actualAccommodation.get().getId());
    }
}

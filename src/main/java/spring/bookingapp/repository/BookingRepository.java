package spring.bookingapp.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring.bookingapp.model.Booking;
import spring.bookingapp.model.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.accommodation.id = :accommodationId " +
            "AND b.status IN :statuses " +
            "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    boolean existsOverlappingBooking(@Param("accommodationId") Long accommodationId,
                                     @Param("checkInDate") LocalDate checkInDate,
                                     @Param("checkOutDate") LocalDate checkOutDate,
                                     @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE " +
            "(:userId IS NULL OR b.user.id = :userId) AND " +
            "(:status IS NULL OR b.status = :status)")
    Page<Booking> findByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") BookingStatus status,
                                        Pageable pageable);

    List<Booking> findAllByCheckOutDateLessThanEqualAndStatusIn(
            LocalDate date, List<BookingStatus> statuses);
}

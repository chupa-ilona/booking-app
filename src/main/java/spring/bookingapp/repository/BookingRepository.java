package spring.bookingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring.bookingapp.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

}

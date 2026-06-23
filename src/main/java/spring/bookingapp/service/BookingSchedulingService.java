package spring.bookingapp.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.bookingapp.model.Booking;
import spring.bookingapp.model.BookingStatus;
import spring.bookingapp.repository.BookingRepository;

@Service
@RequiredArgsConstructor
public class BookingSchedulingService {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    // Запускається щодня о 12:00 дня
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void checkExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findAllByCheckOutDateLessThanEqualAndStatusIn(
                LocalDate.now(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        if (expiredBookings.isEmpty()) {
            notificationService.sendNoExpiredBookingsMessage();
            return;
        }

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            notificationService.sendBookingExpiredMessage(booking);
        }
    }
}
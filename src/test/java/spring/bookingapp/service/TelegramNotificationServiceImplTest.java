package spring.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spring.bookingapp.model.Accommodation;
import spring.bookingapp.model.AccommodationType;
import spring.bookingapp.model.Booking;
import spring.bookingapp.model.BookingStatus;
import spring.bookingapp.model.User;
import spring.bookingapp.service.impl.TelegramNotificationServiceImpl;
import spring.bookingapp.telegram.BookingTelegramBot;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceImplTest {

    @Mock
    private BookingTelegramBot telegramBot;

    @InjectMocks
    private TelegramNotificationServiceImpl notificationService;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private Booking booking;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("testuser@example.com");

        Accommodation accommodation = new Accommodation();
        accommodation.setType(AccommodationType.valueOf("HOUSE"));

        booking = new Booking();
        booking.setId(101L);
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(LocalDate.of(2026, 6, 20));
        booking.setCheckOutDate(LocalDate.of(2026, 6, 25));
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Send booking confirmation - Success")
    void sendBookingConfirmation_ValidBooking_SendsMessage() {
        // When
        notificationService.sendBookingConfirmation(booking);

        // Then
        verify(telegramBot).sendMessage(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();

        assertTrue(actualMessage.contains("Нове бронювання створено!"));
        assertTrue(actualMessage.contains("testuser@example.com"));
        assertTrue(actualMessage.contains("HOUSE"));
        assertTrue(actualMessage.contains("2026-06-20"));
    }

    @Test
    @DisplayName("Send payment successful message - Success")
    void sendPaymentSuccessfulMessage_ValidBooking_SendsMessage() {
        // When
        notificationService.sendPaymentSuccessfulMessage(booking);

        // Then
        verify(telegramBot).sendMessage(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();

        assertTrue(actualMessage.contains("Оплату успішно отримано!"));
        assertTrue(actualMessage.contains("101"));
        assertTrue(actualMessage.contains("testuser@example.com"));
        assertTrue(actualMessage.contains("CONFIRMED"));
    }
}

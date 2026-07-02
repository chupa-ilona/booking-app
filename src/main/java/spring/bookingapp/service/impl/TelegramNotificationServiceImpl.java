package spring.bookingapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.bookingapp.model.Booking;
import spring.bookingapp.service.NotificationService;
import spring.bookingapp.telegram.BookingTelegramBot;

@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements NotificationService {

    private final BookingTelegramBot telegramBot;

    @Override
    public void sendBookingConfirmation(Booking booking) {
        String message = String.format(
                "🆕 Нове бронювання створено!\n\n"
                        + "👤 Користувач: %s\n"
                        + "🏠 Житло: %s\n"
                        + "📅 Заїзд: %s\n"
                        + "📅 Виїзд: %s",
                booking.getUser().getEmail(),
                booking.getAccommodation().getType(),
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );
        telegramBot.sendMessage(message);
    }

    @Override
    public void sendPaymentSuccessfulMessage(Booking booking) {
        String message = String.format(
                "✅ Оплату успішно отримано!\n\n"
                        + "🆔 Бронювання №: %d\n"
                        + "👤 Користувач: %s\n"
                        + "📌 Статус: %s",
                booking.getId(),
                booking.getUser().getEmail(),
                booking.getStatus().name()
        );
        telegramBot.sendMessage(message);
    }

    @Override
    public void sendBookingExpiredMessage(Booking booking) {
        String message = String.format(
                "⚠️ Бронювання прострочено та скасовано!\n\n"
                        + "🆔 Бронювання №: %d\n"
                        + "👤 Користувач: %s\n"
                        + "🏠 Житло звільнено: %s",
                booking.getId(),
                booking.getUser().getEmail(),
                booking.getAccommodation().getType()
        );
        telegramBot.sendMessage(message);
    }

    @Override
    public void sendNoExpiredBookingsMessage() {
        telegramBot.sendMessage("✅ Перевірка завершена: No expired bookings today!");
    }

    @Override
    public void sendBookingCanceledMessage(Booking booking) {
        String message = String.format(
                "❌ Бронювання скасовано!\n\n"
                        + "🆔 Бронювання №: %d\n"
                        + "👤 Користувач: %s\n"
                        + "🏠 Житло: %s\n"
                        + "📅 Дати: %s - %s",
                booking.getId(),
                booking.getUser().getEmail(),
                booking.getAccommodation().getType(),
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );
        telegramBot.sendMessage(message);
    }
}

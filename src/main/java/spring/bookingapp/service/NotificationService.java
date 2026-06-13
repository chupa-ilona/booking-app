package spring.bookingapp.service;

import spring.bookingapp.model.Booking;

public interface NotificationService {

    void sendBookingConfirmation(Booking booking);

    void sendPaymentSuccessfulMessage(Booking booking);
}

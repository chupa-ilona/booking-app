package spring.bookingapp.service;

import spring.bookingapp.dto.PaymentDto;
import spring.bookingapp.dto.PaymentRequestDto;

public interface PaymentService {

    PaymentDto createPaymentSession(PaymentRequestDto requestDto);

    PaymentDto handleSuccessfulPayment(String sessionId);

    PaymentDto handleCanceledPayment(String sessionId);
}

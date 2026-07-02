package spring.bookingapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.bookingapp.dto.PaymentDto;
import spring.bookingapp.dto.PaymentRequestDto;

public interface PaymentService {

    PaymentDto createPaymentSession(PaymentRequestDto requestDto);

    PaymentDto handleSuccessfulPayment(String sessionId);

    PaymentDto handleCanceledPayment(String sessionId);

    Page<PaymentDto> findAll(Long userId, Pageable pageable);
}

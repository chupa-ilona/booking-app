package spring.bookingapp.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import spring.bookingapp.dto.PaymentDto;
import spring.bookingapp.dto.PaymentRequestDto;
import spring.bookingapp.exception.EntityNotFoundException;
import spring.bookingapp.mapper.PaymentMapper;
import spring.bookingapp.model.*;
import spring.bookingapp.repository.BookingRepository;
import spring.bookingapp.repository.PaymentRepository;
import spring.bookingapp.service.NotificationService;
import spring.bookingapp.service.PaymentService;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional
    public PaymentDto createPaymentSession(PaymentRequestDto requestDto) {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking with id "
                        + requestDto.getBookingId() + " not found"));

        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        days = days == 0 ? 1 : days;
        BigDecimal totalAmount = booking.getAccommodation()
                .getDailyRate()
                .multiply(BigDecimal.valueOf(days));

        long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

        try {
            SessionCreateParams params = createStripeSessionParams(booking, amountInCents);

            Session session = Session.create(params);

            Payment payment = new Payment();
            payment.setStatus(PaymentStatus.PENDING);
            payment.setBooking(booking);
            payment.setSessionUrl(session.getUrl());
            payment.setSessionId(session.getId());
            payment.setAmountToPay(totalAmount);

            return paymentMapper.toDto(paymentRepository.save(payment));

        } catch (StripeException e) {
            throw new RuntimeException("Can't create Stripe payment session", e);
        }

    }

    @Override
    @Transactional
    public PaymentDto handleSuccessfulPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with session id "
                        + sessionId + " not found"));

        payment.setStatus(PaymentStatus.PAID);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        notificationService.sendPaymentSuccessfulMessage(booking);

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDto handleCanceledPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with session id "
                        + sessionId + " not found"));

        payment.setStatus(PaymentStatus.FAILED);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> findAll(Long userId, Pageable pageable) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MANAGER"));

        if (!isManager) {
            return paymentRepository.findAllByBookingUserId(currentUser.getId(), pageable)
                    .map(paymentMapper::toDto);
        }

        if (userId != null) {
            return paymentRepository.findAllByBookingUserId(userId, pageable)
                    .map(paymentMapper::toDto);
        }

        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toDto);
    }

    private SessionCreateParams createStripeSessionParams(Booking booking, long amountInCents) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String successUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/payments/success")
                .queryParam("sessionId", "{CHECKOUT_SESSION_ID}")
                .build().toUriString();

        String cancelUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/payments/cancel")
                .queryParam("sessionId", "{CHECKOUT_SESSION_ID}")
                .build().toUriString();

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName("Booking accommodation: "
                                                                        + booking.getAccommodation()
                                                                        .getType())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}

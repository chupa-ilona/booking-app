package spring.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import spring.bookingapp.dto.PaymentDto;
import spring.bookingapp.dto.PaymentRequestDto;
import spring.bookingapp.exception.EntityNotFoundException;
import spring.bookingapp.mapper.PaymentMapper;
import spring.bookingapp.model.*;
import spring.bookingapp.repository.BookingRepository;
import spring.bookingapp.repository.PaymentRepository;
import spring.bookingapp.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking booking;
    private Payment payment;
    private String sessionId;

    @BeforeEach
    void setUp() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(AccommodationType.valueOf("HOUSE"));
        accommodation.setDailyRate(BigDecimal.valueOf(100.0));

        booking = new Booking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(4)); // 3 days
        booking.setAccommodation(accommodation);
        booking.setStatus(BookingStatus.PENDING);

        sessionId = "cs_test_123456789";

        payment = new Payment();
        payment.setId(1L);
        payment.setSessionId(sessionId);
        payment.setBooking(booking);
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Create payment session - Success")
    void createPaymentSession_ValidRequest_ReturnsDto() {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(booking.getId());

        PaymentDto expectedDto = new PaymentDto();
        expectedDto.setId(1L);
        expectedDto.setSessionId(sessionId);

        when(bookingRepository.findById(requestDto.getBookingId())).thenReturn(Optional.of(booking));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expectedDto);

        // Mocking Stripe static method Session.create()
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session stripeSession = mock(Session.class);
            when(stripeSession.getUrl()).thenReturn("http://stripe.url/checkout");
            when(stripeSession.getId()).thenReturn(sessionId);

            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(stripeSession);

            // When
            PaymentDto actualDto = paymentService.createPaymentSession(requestDto);

            // Then
            assertEquals(expectedDto.getId(), actualDto.getId());
            assertEquals(expectedDto.getSessionId(), actualDto.getSessionId());
            verify(bookingRepository).findById(requestDto.getBookingId());
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    @DisplayName("Create payment session - Booking not found - Throws Exception")
    void createPaymentSession_BookingNotFound_ThrowsException() {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(999L);

        when(bookingRepository.findById(requestDto.getBookingId())).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPaymentSession(requestDto));

        assertEquals("Booking with id 999 not found", exception.getMessage());
        verify(bookingRepository, times(1)).findById(requestDto.getBookingId());
        verify(paymentRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Handle successful payment - Success")
    void handleSuccessfulPayment_ValidSessionId_ReturnsDto() {
        // Given
        PaymentDto expectedDto = new PaymentDto();
        expectedDto.setId(payment.getId());
        expectedDto.setStatus(PaymentStatus.PAID);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expectedDto);

        // When
        PaymentDto actualDto = paymentService.handleSuccessfulPayment(sessionId);

        // Then
        assertEquals(PaymentStatus.PAID, actualDto.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        verify(paymentRepository).findBySessionId(sessionId);
        verify(bookingRepository).save(booking);
        verify(notificationService).sendPaymentSuccessfulMessage(booking);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Handle successful payment - Payment not found - Throws Exception")
    void handleSuccessfulPayment_InvalidSessionId_ThrowsException() {
        // Given
        String invalidSessionId = "invalid_id";
        when(paymentRepository.findBySessionId(invalidSessionId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.handleSuccessfulPayment(invalidSessionId));

        assertEquals("Payment with session id " + invalidSessionId + " not found", exception.getMessage());
        verify(bookingRepository, times(0)).save(any());
        verify(notificationService, times(0)).sendPaymentSuccessfulMessage(any());
    }

    @Test
    @DisplayName("Handle canceled payment - Success")
    void handleCanceledPayment_ValidSessionId_ReturnsDto() {
        // Given
        PaymentDto expectedDto = new PaymentDto();
        expectedDto.setId(payment.getId());
        expectedDto.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expectedDto);

        // When
        PaymentDto actualDto = paymentService.handleCanceledPayment(sessionId);

        // Then
        assertEquals(PaymentStatus.FAILED, actualDto.getStatus());
        verify(paymentRepository).findBySessionId(sessionId);
        verify(paymentRepository).save(payment);
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Handle canceled payment - Payment not found - Throws Exception")
    void handleCanceledPayment_InvalidSessionId_ThrowsException() {
        // Given
        String invalidSessionId = "invalid_id";
        when(paymentRepository.findBySessionId(invalidSessionId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.handleCanceledPayment(invalidSessionId));

        assertEquals("Payment with session id " + invalidSessionId + " not found", exception.getMessage());
        verify(paymentRepository, times(0)).save(any());
    }
}

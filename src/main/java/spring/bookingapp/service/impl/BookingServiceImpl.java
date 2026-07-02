package spring.bookingapp.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.bookingapp.dto.BookingDto;
import spring.bookingapp.dto.CreateBookingRequestDto;
import spring.bookingapp.exception.EntityNotFoundException;
import spring.bookingapp.mapper.BookingMapper;
import spring.bookingapp.model.Accommodation;
import spring.bookingapp.model.Booking;
import spring.bookingapp.model.BookingStatus;
import spring.bookingapp.model.User;
import spring.bookingapp.repository.AccommodationRepository;
import spring.bookingapp.repository.BookingRepository;
import spring.bookingapp.service.BookingService;
import spring.bookingapp.service.NotificationService;



@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final AccommodationRepository accommodationRepository;
    private final NotificationService notificationService;
    private final spring.bookingapp.repository.PaymentRepository paymentRepository;

    @Override
    @Transactional
    public BookingDto save(CreateBookingRequestDto requestDto) {

        if (requestDto.getCheckInDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
        if (!requestDto.getCheckOutDate().isAfter(requestDto.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be strictly after check-in date");
        }

        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        boolean hasPendingPayments = paymentRepository.existsByBookingUserIdAndStatus(
                currentUser.getId(), spring.bookingapp.model.PaymentStatus.PENDING);

        if (hasPendingPayments) {
            throw new IllegalStateException("You cannot create a new booking because you have a pending payment.");
        }

        Accommodation accommodation = accommodationRepository
                .findById(requestDto.getAccommodationId())
                .orElseThrow(() -> new EntityNotFoundException("Accommodation with id "
                        + requestDto.getAccommodationId() + " not found"));

        Long overlappingBookingsCount = bookingRepository.countOverlappingBookings(
                requestDto.getAccommodationId(),
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        if (overlappingBookingsCount >= accommodation.getAvailability()) {
            throw new IllegalArgumentException("The accommodation is fully booked for the selected dates");
        }

        Booking booking = bookingMapper.toModel(requestDto);
        booking.setUser(currentUser);
        booking.setAccommodation(accommodation);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        notificationService.sendBookingConfirmation(savedBooking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public Page<BookingDto> findAll(Long userId, BookingStatus status, Pageable pageable) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MANAGER"));

        if (!isManager) {
            userId = currentUser.getId();
        }

        return bookingRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    public BookingDto getById(Long id) {
        return bookingMapper.toDto(bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking with id "
                        + id + " not found")));
    }

    @Override
    public BookingDto update(Long id, CreateBookingRequestDto requestDto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking with id "
                        + id + " not found"));
        bookingMapper.update(requestDto, booking);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking with id " + id + " not found"));

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalArgumentException("This booking is already canceled.");
        }

        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);
        notificationService.sendBookingCanceledMessage(booking);
    }
}

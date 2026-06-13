package spring.bookingapp.service.impl;

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

    @Override
    @Transactional
    public BookingDto save(CreateBookingRequestDto requestDto) {
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Accommodation accommodation = accommodationRepository
                .findById(requestDto.getAccommodationId())
                .orElseThrow(() -> new EntityNotFoundException("Accommodation with id "
                        + requestDto.getAccommodationId() + " not found"));

        Booking booking = bookingMapper.toModel(requestDto);
        booking.setUser(currentUser);
        booking.setAccommodation(accommodation);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        notificationService.sendBookingConfirmation(savedBooking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public Page<BookingDto> findAll(Pageable pageable) {
        return bookingRepository.findAll(pageable)
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
    public void deleteById(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new EntityNotFoundException("Booking with id " + id + " not found");
        }
        bookingRepository.deleteById(id);

    }
}

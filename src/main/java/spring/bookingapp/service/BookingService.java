package spring.bookingapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.bookingapp.dto.BookingDto;
import spring.bookingapp.dto.CreateBookingRequestDto;
import spring.bookingapp.model.BookingStatus;

public interface BookingService {

    BookingDto save(CreateBookingRequestDto bookingDto);

    Page<BookingDto> findAll(Long userId, BookingStatus status, Pageable pageable);

    BookingDto getById(Long id);

    BookingDto update(Long id, CreateBookingRequestDto bookingDto);

    void deleteById(Long id);
}

package spring.bookingapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.bookingapp.dto.BookingDto;
import spring.bookingapp.dto.CreateBookingRequestDto;
import spring.bookingapp.service.BookingService;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    public BookingDto save(@RequestBody @Valid CreateBookingRequestDto requestDto) {
        return bookingService.save(requestDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGER')")
    public Page<BookingDto> findAll(Pageable pageable) {
        return bookingService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    public BookingDto getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGER')")
    public BookingDto update(@PathVariable Long id,
                             @RequestBody @Valid CreateBookingRequestDto requestDto) {
        return bookingService.update(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    public void deleteById(@PathVariable Long id) {
        bookingService.deleteById(id);
    }
}

package spring.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.bookingapp.dto.BookingDto;
import spring.bookingapp.dto.CreateBookingRequestDto;
import spring.bookingapp.model.BookingStatus;
import spring.bookingapp.service.BookingService;

@Tag(name = "Booking management",
        description = "Endpoints for managing bookings")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Create a new booking",
            description = "Create a new booking for an accommodation.")
    public BookingDto save(@RequestBody @Valid CreateBookingRequestDto requestDto) {
        return bookingService.save(requestDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Get all bookings",
            description = "Get a page of bookings. Managers can filter by user_id and status."
                    + " Customers can only see their own bookings.")
    public Page<BookingDto> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BookingStatus status,
            Pageable pageable) {
        return bookingService.findAll(userId, status, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Get booking by ID",
            description = "Get details of a specific booking by its ID.")
    public BookingDto getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update booking",
            description = "Update details of a booking. Available to managers only.")
    @PreAuthorize("hasAuthority('MANAGER')")
    public BookingDto update(@PathVariable Long id,
                             @RequestBody @Valid CreateBookingRequestDto requestDto) {
        return bookingService.update(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Delete booking",
            description = "Cancel or delete a booking by its ID.")
    public void deleteById(@PathVariable Long id) {
        bookingService.deleteById(id);
    }
}

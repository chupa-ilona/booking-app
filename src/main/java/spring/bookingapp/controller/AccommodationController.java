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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.bookingapp.dto.AccommodationDto;
import spring.bookingapp.dto.CreateAccommodationRequestDto;
import spring.bookingapp.service.AccommodationService;

@Tag(name = "Accommodation management",
        description = "Endpoints for managing accommodations")
@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
public class AccommodationController {
    private final AccommodationService accommodationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Create a new accommodation",
            description = "Create a new accommodation. Available to managers only.")
    public AccommodationDto save(@RequestBody @Valid CreateAccommodationRequestDto requestDto) {
        return accommodationService.save(requestDto);
    }

    @GetMapping
    @Operation(summary = "Get all accommodations",
            description = "Get a page of all available accommodations.")
    public Page<AccommodationDto> findAll(Pageable pageable) {
        return accommodationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accommodation by ID",
            description = "Get details of a specific accommodation by its ID.")
    public AccommodationDto getById(@PathVariable Long id) {
        return accommodationService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Update accommodation",
            description = "Update details of an accommodation. Available to managers only.")
    public AccommodationDto update(@PathVariable Long id,
                                   @RequestBody @Valid CreateAccommodationRequestDto requestDto) {
        return accommodationService.update(id, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Delete accommodation",
            description = "Delete an accommodation by its ID. Available to managers only.")
    public void deleteById(@PathVariable Long id) {
        accommodationService.deleteById(id);
    }
}

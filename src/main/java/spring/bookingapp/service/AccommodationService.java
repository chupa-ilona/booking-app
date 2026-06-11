package spring.bookingapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.bookingapp.dto.AccommodationDto;
import spring.bookingapp.dto.CreateAccommodationRequestDto;

public interface AccommodationService {
    AccommodationDto save(CreateAccommodationRequestDto requestDto);

    Page<AccommodationDto> findAll(Pageable pageable);

    AccommodationDto getById(Long id);

    AccommodationDto update(Long id, CreateAccommodationRequestDto requestDto);

    void deleteById(Long id);
}

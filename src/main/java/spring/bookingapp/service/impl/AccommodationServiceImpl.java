package spring.bookingapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.bookingapp.dto.AccommodationDto;
import spring.bookingapp.dto.CreateAccommodationRequestDto;
import spring.bookingapp.exception.EntityNotFoundException;
import spring.bookingapp.mapper.AccommodationMapper;
import spring.bookingapp.model.Accommodation;
import spring.bookingapp.repository.AccommodationRepository;
import spring.bookingapp.service.AccommodationService;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    @Override
    @Transactional
    public AccommodationDto save(CreateAccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationRepository
                .save(accommodationMapper.toModel(requestDto));
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    public Page<AccommodationDto> findAll(Pageable pageable) {
        return accommodationRepository
                .findAll(pageable)
                .map(accommodationMapper::toDto);
    }

    @Override
    public AccommodationDto getById(Long id) {
        return accommodationMapper
                .toDto(accommodationRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Accommodation with id "
                                + id + " not found")));
    }

    @Override
    @Transactional
    public AccommodationDto update(Long id, CreateAccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Accommodation with id "
                        + id + " not found"));

        accommodationMapper.update(requestDto, accommodation);
        return accommodationMapper.toDto(accommodationRepository.save(accommodation));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!accommodationRepository.existsById(id)) {
            throw new EntityNotFoundException("Accommodation with id " + id + " not found");
        }
        accommodationRepository.deleteById(id);
    }
}

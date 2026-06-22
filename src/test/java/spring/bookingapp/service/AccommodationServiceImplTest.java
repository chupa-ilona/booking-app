package spring.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import spring.bookingapp.dto.AccommodationDto;
import spring.bookingapp.dto.CreateAccommodationRequestDto;
import spring.bookingapp.exception.EntityNotFoundException;
import spring.bookingapp.mapper.AccommodationMapper;
import spring.bookingapp.model.Accommodation;
import spring.bookingapp.model.AccommodationType;
import spring.bookingapp.repository.AccommodationRepository;
import spring.bookingapp.service.impl.AccommodationServiceImpl;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceImplTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationMapper accommodationMapper;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    @Test
    @DisplayName("Save valid accommodation")
    void save_ValidRequest_ReturnsDto() {
        // Given
        CreateAccommodationRequestDto requestDto = new CreateAccommodationRequestDto();
        requestDto.setType(AccommodationType.valueOf("HOUSE"));
        requestDto.setDailyRate(BigDecimal.valueOf(150.0));

        Accommodation accommodation = new Accommodation();
        accommodation.setType(requestDto.getType());

        Accommodation savedAccommodation = new Accommodation();
        savedAccommodation.setId(1L);
        savedAccommodation.setType(requestDto.getType());

        AccommodationDto expectedDto = new AccommodationDto();
        expectedDto.setId(1L);
        expectedDto.setType(AccommodationType.valueOf("HOUSE"));

        when(accommodationMapper.toModel(requestDto)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(savedAccommodation);
        when(accommodationMapper.toDto(savedAccommodation)).thenReturn(expectedDto);

        // When
        AccommodationDto actualDto = accommodationService.save(requestDto);

        // Then
        assertEquals(expectedDto.getId(), actualDto.getId());
        verify(accommodationRepository).save(accommodation);
    }

    @Test
    @DisplayName("Find All Accommodation")
    void findAll_ValidPageable_ReturnsAllAccommodations() {
        // Given
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        AccommodationDto accommodationDto = new AccommodationDto();
        accommodationDto.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        List<Accommodation> accommodations = List.of(accommodation);
        Page<Accommodation> accommodationPage = new PageImpl<>(accommodations, pageable, accommodations.size());

        when(accommodationRepository.findAll(pageable)).thenReturn(accommodationPage);
        when(accommodationMapper.toDto(accommodation)).thenReturn(accommodationDto);

        // When
        Page<AccommodationDto> actualPage = accommodationService.findAll(pageable);

        // Then
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(accommodationDto.getId(), actualPage.getContent().get(0).getId());
        verify(accommodationRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Get Accommodation by Valid ID")
    void getById_ValidId_ReturnsDto() {
        // Given
        Long id = 1L;
        Accommodation accommodation = new Accommodation();
        accommodation.setId(id);
        AccommodationDto expectedDto = new AccommodationDto();
        expectedDto.setId(id);

        when(accommodationRepository.findById(id)).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(expectedDto);

        // When
        AccommodationDto actualDto = accommodationService.getById(id);

        // Then
        assertEquals(expectedDto.getId(), actualDto.getId());
        verify(accommodationRepository).findById(id);
    }

    @Test
    @DisplayName("Get Accommodation by Invalid ID - exception")
    void getById_InvalidId_ThrowsException() {
        // Given
        Long invalidId = 100L;
        when(accommodationRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> accommodationService.getById(invalidId));
        verify(accommodationRepository).findById(invalidId);
    }

    @Test
    @DisplayName("Update by Valid ID")
    void update_ValidIdAndRequest_ReturnsUpdatedDto() {
        // Given
        Long id = 1L;
        CreateAccommodationRequestDto updateRequest = new CreateAccommodationRequestDto();
        updateRequest.setType(AccommodationType.valueOf("APARTMENT"));

        Accommodation existingAccommodation = new Accommodation();
        existingAccommodation.setId(id);
        existingAccommodation.setType(AccommodationType.valueOf("HOUSE"));

        Accommodation updatedAccommodation = new Accommodation();
        updatedAccommodation.setId(id);
        updatedAccommodation.setType(AccommodationType.valueOf("APARTMENT"));

        AccommodationDto expectedDto = new AccommodationDto();
        expectedDto.setId(id);
        expectedDto.setType(AccommodationType.valueOf("APARTMENT"));

        when(accommodationRepository.findById(id)).thenReturn(Optional.of(existingAccommodation));
        when(accommodationRepository.save(existingAccommodation)).thenReturn(updatedAccommodation);
        when(accommodationMapper.toDto(updatedAccommodation)).thenReturn(expectedDto);

        // When
        AccommodationDto actualDto = accommodationService.update(id, updateRequest);

        // Then
        assertEquals(expectedDto.getType(), actualDto.getType());
        verify(accommodationRepository).findById(id);
        verify(accommodationRepository).save(existingAccommodation);
    }

    @Test
    @DisplayName("Update by Invalid ID - exception")
    void update_InvalidId_ThrowsException() {
        // Given
        Long invalidId = 100L;
        CreateAccommodationRequestDto updateRequest = new CreateAccommodationRequestDto();
        when(accommodationRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> accommodationService.update(invalidId, updateRequest));
    }

    @Test
    @DisplayName("delete by valid ID")
    void deleteById_ValidId_Success() {
        // Given
        Long id = 1L;
        when(accommodationRepository.existsById(id)).thenReturn(true);

        // When
        accommodationService.deleteById(id);

        // Then
        verify(accommodationRepository).existsById(id);
        verify(accommodationRepository).deleteById(id);
    }
}

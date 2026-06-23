package spring.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import spring.bookingapp.service.impl.BookingServiceImpl;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User currentUser;
    private Accommodation accommodation;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("test@example.com");

        accommodation = new Accommodation();
        accommodation.setId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Save booking with Valid Data")
    void save_ValidRequest_ReturnsDto() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);

        CreateBookingRequestDto requestDto = new CreateBookingRequestDto();
        requestDto.setAccommodationId(accommodation.getId());

        Booking booking = new Booking();

        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setUser(currentUser);
        savedBooking.setAccommodation(accommodation);
        savedBooking.setStatus(BookingStatus.PENDING);

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);

        when(accommodationRepository.findById(requestDto.getAccommodationId()))
                .thenReturn(Optional.of(accommodation));
        when(bookingMapper.toModel(requestDto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(savedBooking);
        when(bookingMapper.toDto(savedBooking)).thenReturn(expectedDto);

        // When
        BookingDto actualDto = bookingService.save(requestDto);

        // Then
        assertEquals(expectedDto.getId(), actualDto.getId());
        verify(accommodationRepository).findById(requestDto.getAccommodationId());
        verify(bookingRepository).save(booking);
        verify(notificationService).sendBookingConfirmation(savedBooking);
    }

    @Test
    @DisplayName("Save booking with invalid accommodation")
    void save_AccommodationNotFound_ThrowsException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);

        CreateBookingRequestDto requestDto = new CreateBookingRequestDto();
        requestDto.setAccommodationId(999L);

        when(accommodationRepository.findById(requestDto.getAccommodationId()))
                .thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.save(requestDto));

        assertEquals("Accommodation with id " + requestDto.getAccommodationId() + " not found", exception.getMessage());
        verify(bookingRepository, times(0)).save(any());
        verify(notificationService, times(0)).sendBookingConfirmation(any());
    }

//    @Test
//    @DisplayName("Get all bookings")
//    void findAll_ValidPageable_ReturnsAllBookings() {
//        // Given
//        Booking booking = new Booking();
//        booking.setId(1L);
//        BookingDto bookingDto = new BookingDto();
//        bookingDto.setId(1L);
//
//        Pageable pageable = PageRequest.of(0, 10);
//        List<Booking> bookings = List.of(booking);
//        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, bookings.size());
//
//        when(bookingRepository.findAll(pageable)).thenReturn(bookingPage);
//        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);
//
//        // When
//        Page<BookingDto> actualPage = bookingService.findAll(pageable,);
//
//        // Then
//        assertEquals(1, actualPage.getTotalElements());
//        assertEquals(bookingDto.getId(), actualPage.getContent().get(0).getId());
//        verify(bookingRepository).findAll(pageable);
//    }

    @Test
    @DisplayName("Get booking by Valid ID")
    void getById_ValidId_ReturnsDto() {
        // Given
        Long id = 1L;
        Booking booking = new Booking();
        booking.setId(id);
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(id);

        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(expectedDto);

        // When
        BookingDto actualDto = bookingService.getById(id);

        // Then
        assertEquals(expectedDto.getId(), actualDto.getId());
        verify(bookingRepository).findById(id);
    }

    @Test
    @DisplayName("Get booking by invalid ID")
    void getById_InvalidId_ThrowsException() {
        // Given
        Long invalidId = 999L;
        when(bookingRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.getById(invalidId));

        assertEquals("Booking with id " + invalidId + " not found", exception.getMessage());
        verify(bookingRepository).findById(invalidId);
    }

    @Test
    @DisplayName("Update booking by valid ID")
    void update_ValidIdAndRequest_ReturnsUpdatedDto() {
        // Given
        Long id = 1L;
        CreateBookingRequestDto updateRequest = new CreateBookingRequestDto();

        Booking existingBooking = new Booking();
        existingBooking.setId(id);

        Booking updatedBooking = new Booking();
        updatedBooking.setId(id);

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(id);

        when(bookingRepository.findById(id)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.save(existingBooking)).thenReturn(updatedBooking);
        when(bookingMapper.toDto(updatedBooking)).thenReturn(expectedDto);

        // When
        BookingDto actualDto = bookingService.update(id, updateRequest);

        // Then
        assertEquals(expectedDto.getId(), actualDto.getId());
        verify(bookingRepository).findById(id);
        verify(bookingMapper).update(updateRequest, existingBooking);
        verify(bookingRepository).save(existingBooking);
    }

    @Test
    @DisplayName("Update booking by Invalid ID")
    void update_InvalidId_ThrowsException() {
        // Given
        Long invalidId = 999L;
        CreateBookingRequestDto updateRequest = new CreateBookingRequestDto();
        when(bookingRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.update(invalidId, updateRequest));

        assertEquals("Booking with id " + invalidId + " not found", exception.getMessage());
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Delete booking by Valid ID")
    void deleteById_ValidId_Success() {
        // Given
        Long id = 1L;
        when(bookingRepository.existsById(id)).thenReturn(true);

        // When
        bookingService.deleteById(id);

        // Then
        verify(bookingRepository).existsById(id);
        verify(bookingRepository).deleteById(id);
    }

    @Test
    @DisplayName("Delete booking by Invalid ID")
    void deleteById_InvalidId_ThrowsException() {
        // Given
        Long invalidId = 999L;
        when(bookingRepository.existsById(invalidId)).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.deleteById(invalidId));

        assertEquals("Booking with id " + invalidId + " not found", exception.getMessage());
        verify(bookingRepository).existsById(invalidId);
        verify(bookingRepository, times(0)).deleteById(any());
    }
}

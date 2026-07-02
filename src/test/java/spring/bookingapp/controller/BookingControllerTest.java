package spring.bookingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import spring.bookingapp.dto.CreateBookingRequestDto;
import spring.bookingapp.model.User;
import spring.bookingapp.service.NotificationService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    private void setupCustomSecurityContext(Long userId, String role) {
        User customUser = new User();
        customUser.setId(userId);
        customUser.setEmail("test@example.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        customUser, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // CREATE BOOKING ENDPOINT
    @Test
    @DisplayName("Create a new booking (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createBooking_ValidRequestDto_Success() throws Exception {
        setupCustomSecurityContext(1L, "CUSTOMER");

        // Given
        CreateBookingRequestDto requestDto = new CreateBookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDate.now().plusDays(2));
        requestDto.setCheckOutDate(LocalDate.now().plusDays(7));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/bookings")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkInDate").value(requestDto.getCheckInDate().toString()))
                .andExpect(jsonPath("$.checkOutDate").value(requestDto.getCheckOutDate().toString()));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    @DisplayName("Create a new booking with missing dates (Negative)")
    void createBooking_InvalidRequestDto_ReturnsBadRequest() throws Exception {
        // Given
        CreateBookingRequestDto requestDto = new CreateBookingRequestDto();
        requestDto.setAccommodationId(1L);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/bookings")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // GET ALL BOOKINGS
    @Test
    @DisplayName("Get all bookings as MANAGER (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql",
            "classpath:database/bookings/add-booking.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAll_AsManager_ReturnsAllBookings() throws Exception {
        setupCustomSecurityContext(1L, "MANAGER");

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get all bookings as CUSTOMER (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql",
            "classpath:database/bookings/add-booking.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAll_AsCustomer_ReturnsBookings_Success() throws Exception {
        setupCustomSecurityContext(1L, "CUSTOMER");

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray());
    }

    // GET BOOKING BY ID
    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    @DisplayName("Get booking by valid ID (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql",
            "classpath:database/bookings/add-booking.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingById_ValidId_ReturnsBookingDto() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    @DisplayName("Get booking by invalid ID (Negative)")
    void getBookingById_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/bookings/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // UPDATE BOOKING
    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @DisplayName("Update existing booking (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql",
            "classpath:database/bookings/add-booking.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateBooking_ValidRequestDto_ReturnsUpdatedBooking() throws Exception {
        // Given
        CreateBookingRequestDto requestDto = new CreateBookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDate.now().plusDays(10));
        requestDto.setCheckOutDate(LocalDate.now().plusDays(15));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(put("/bookings/1")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkInDate").value(requestDto.getCheckInDate().toString()));
    }

    // DELETE BOOKING
    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    @DisplayName("Delete booking by valid ID (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql",
            "classpath:database/users/add-user.sql",
            "classpath:database/bookings/add-booking.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/bookings/remove-bookings.sql",
            "classpath:database/users/remove-users.sql",
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteBooking_ValidId_Success() throws Exception {
        mockMvc.perform(delete("/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
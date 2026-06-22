package spring.bookingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import spring.bookingapp.dto.CreateAccommodationRequestDto;
import spring.bookingapp.model.AccommodationType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccommodationControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }


    // CREATE ACCOMMODATION ENDPOINT
    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @DisplayName("Create a new accommodation (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createAccommodation_ValidRequestDto_Success() throws Exception {
        // Given
        CreateAccommodationRequestDto requestDto = new CreateAccommodationRequestDto();
        requestDto.setType(AccommodationType.valueOf("HOUSE"));
        requestDto.setLocation("Lviv, Rynok Square");
        requestDto.setSize("120 sq.m.");
        requestDto.setAmenities(List.of("WiFi", "Air Conditioning", "Kitchen"));
        requestDto.setDailyRate(BigDecimal.valueOf(200.00));
        requestDto.setAvailability(2);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/accommodations")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value(requestDto.getType().name()))
                .andExpect(jsonPath("$.location").value(requestDto.getLocation()))
                .andExpect(jsonPath("$.size").value(requestDto.getSize()));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @DisplayName("Create a new accommodation with missing fields (Negative)")
    void createAccommodation_InvalidRequestDto_ReturnsBadRequest() throws Exception {
        CreateAccommodationRequestDto requestDto = new CreateAccommodationRequestDto();
        requestDto.setDailyRate(BigDecimal.valueOf(-50.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/accommodations")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // GET ALL ACCOMMODATIONS
    @Test
    @WithMockUser(username = "user", authorities = {"CUSTOMER"})
    @DisplayName("Get all accommodations (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAll_GivenAccommodationsInCatalog_ReturnsAllAccommodations() throws Exception {
        mockMvc.perform(get("/accommodations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray());
    }


    // GET ACCOMMODATION BY ID
    @Test
    @WithMockUser(username = "user", authorities = {"CUSTOMER"})
    @DisplayName("Get accommodation by valid ID (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAccommodationById_ValidId_ReturnsAccommodationDto() throws Exception {
        mockMvc.perform(get("/accommodations/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"CUSTOMER"})
    @DisplayName("Get accommodation by invalid ID (Negative)")
    void getAccommodationById_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/accommodations/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    // UPDATE ACCOMMODATION
    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @DisplayName("Update existing accommodation (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateAccommodation_ValidRequestDto_ReturnsUpdatedAccommodation() throws Exception {
        // Given - Знову ж таки, заповнюємо всі поля DTO
        CreateAccommodationRequestDto requestDto = new CreateAccommodationRequestDto();
        requestDto.setType(AccommodationType.valueOf("APARTMENT"));
        requestDto.setLocation("Updated Location");
        requestDto.setSize("50 sq.m.");
        requestDto.setAmenities(List.of("WiFi", "Balcony"));
        requestDto.setDailyRate(BigDecimal.valueOf(300.00));
        requestDto.setAvailability(5);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(put("/accommodations/1")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(requestDto.getType().name()))
                .andExpect(jsonPath("$.location").value(requestDto.getLocation()));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @DisplayName("Update accommodation with non-existing ID (Negative)")
    void updateAccommodation_InvalidId_ReturnsNotFound() throws Exception {
        // Given
        CreateAccommodationRequestDto requestDto = new CreateAccommodationRequestDto();
        requestDto.setType(AccommodationType.valueOf("APARTMENT"));
        requestDto.setLocation("Updated Location");
        requestDto.setSize("50 sq.m.");
        requestDto.setAmenities(List.of("WiFi"));
        requestDto.setDailyRate(BigDecimal.valueOf(300.00));
        requestDto.setAvailability(5);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(put("/accommodations/100")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    // DELETE ACCOMMODATION
    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @DisplayName("Delete accommodation by valid ID (Positive)")
    @Sql(scripts = {
            "classpath:database/accommodations/add-accommodation.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteAccommodation_ValidId_Success() throws Exception {
        mockMvc.perform(delete("/accommodations/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @DisplayName("Delete accommodation with non-existing ID (Negative)")
    void deleteAccommodation_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/accommodations/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

package ru.lvrmmm.hotelbookingservice.room.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.lvrmmm.hotelbookingservice.booking.repository.BookingRepository;
import ru.lvrmmm.hotelbookingservice.integration.AbstractIntegrationTest;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        roomRepository.saveAndFlush(new Room(
                801, new BigDecimal("5500.00"), RoomOccupancyType.SINGLE,
                RoomComfortLevel.SUPERIOR, 1, true));
    }

    @Test
    void getAllRooms_returnsPageFromDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/rooms")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("sortBy", "roomNumber")
                        .queryParam("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].roomNumber").value(801))
                .andExpect(jsonPath("$.content[0].comfortLevel").value("SUPERIOR"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllRooms_rejectsInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/v1/rooms")
                        .queryParam("direction", "sideways"))
                .andExpect(status().isBadRequest());
    }
}

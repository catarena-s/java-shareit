package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingService service;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    @Autowired
    private MockMvc mvc;
    private BookingDto dto;

    @BeforeEach
    void setUp() {
        dto = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.WAITING)
                .build();
    }

    @SneakyThrows
    @Test
    void create() {
        when(service.createBooking(anyLong(), any()))
                .thenReturn(dto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.itemId", is(dto.getItemId()), Long.class))
                .andExpect(jsonPath("$.start", is(dto.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(dto.getEnd().format(formatter))))
                .andExpect(jsonPath("$.status", is(dto.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void approve() {
        BookingDto updateDto = dto.toBuilder()
                .status(BookingStatus.APPROVED)
                .build();
        when(service.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(updateDto);

        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateDto.getId()), Long.class))
                .andExpect(jsonPath("$.itemId", is(updateDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.start", is(updateDto.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(updateDto.getEnd().format(formatter))))
                .andExpect(jsonPath("$.status", is(updateDto.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void getAllByBooker() {
        when(service.getAllByBooker(anyLong(), anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].itemId", is(dto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(dto.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(dto.getEnd().format(formatter))))
                .andExpect(jsonPath("$[0].status", is(dto.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void getAllByBooker_withPagination() {
        when(service.getAllByBooker(anyLong(), anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].itemId", is(dto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(dto.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(dto.getEnd().format(formatter))))
                .andExpect(jsonPath("$[0].status", is(dto.getStatus().name())));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(service.getAllByOwner(anyLong(), anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].itemId", is(dto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(dto.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(dto.getEnd().format(formatter))))
                .andExpect(jsonPath("$[0].status", is(dto.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void getAllByOwner_withPagination() {
        when(service.getAllByOwner(anyLong(), anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].itemId", is(dto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(dto.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(dto.getEnd().format(formatter))))
                .andExpect(jsonPath("$[0].status", is(dto.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void getBookingByIdForUser() {
        when(service.getBookingByIdForUser(anyLong(), anyLong()))
                .thenReturn(dto);

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.itemId", is(dto.getItemId()), Long.class))
                .andExpect(jsonPath("$.start", is(dto.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(dto.getEnd().format(formatter))))
                .andExpect(jsonPath("$.status", is(dto.getStatus().name())));
    }
}
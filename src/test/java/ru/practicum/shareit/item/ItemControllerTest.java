package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @MockBean
    private ItemService service;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    private ItemDto dto;

    @BeforeEach
    void setUp() {
        dto = ItemDto.builder()
                .id(1L)
                .name("Отвертка")
                .description("Отвертка")
                .available(true)
                .build();
    }

    @Test
    void create() throws Exception {
        when(service.create(any(), anyLong()))
                .thenReturn(dto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.description", is(dto.getDescription())))
        ;
    }

    @Test
    void getAll() throws Exception {
        when(service.getAllByOwner(anyLong(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(dto.getName())))
                .andExpect(jsonPath("$[0].description", is(dto.getDescription())));
    }

    @Test
    void getAll_Pagination() throws Exception {
        when(service.getAllByOwner(anyLong(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(dto.getName())))
                .andExpect(jsonPath("$[0].description", is(dto.getDescription())));
    }

    @Test
    void getById() throws Exception {
        when(service.getById(anyLong(), anyLong()))
                .thenReturn(dto);

        mvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.description", is(dto.getDescription())));
    }

    @Test
    void update() throws Exception {
        when(service.update(any(), anyLong(), anyLong()))
                .thenReturn(dto);

        mvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.description", is(dto.getDescription())));
    }

    @Test
    void search() throws Exception {
        when(service.search(anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ОтвЁ")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(dto.getName())))
                .andExpect(jsonPath("$[0].description", is(dto.getDescription())));
    }

    @Test
    void search_Pagination() throws Exception {
        when(service.search(anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ОтвЁ")
                        .param("from", "0")
                        .param("size", "20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void search_Pagination_withFromIsNull() throws Exception {
        when(service.search(anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ОтвЁ")
                        .param("size", "20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void search_withTextEmpty() throws Exception {
        when(service.search(anyString(), any()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createCommentToItem() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .itemId(1L)
                .text("Коммент")
                .build();

        when(service.addComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated())));
    }
}
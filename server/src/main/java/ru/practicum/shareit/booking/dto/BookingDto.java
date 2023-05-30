package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingDto {
    private Long itemId;// вещь, которую пользователь бронирует
    private LocalDateTime start;// дата и время начала бронирования
    private LocalDateTime end;// дата и время конца бронирования
}

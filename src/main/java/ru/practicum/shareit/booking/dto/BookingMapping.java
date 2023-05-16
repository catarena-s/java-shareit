package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapping {

    public static BookingDto toDto(Booking booking) {
        if (booking == null) return null;
        return BookingDto.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(ItemMapper.toShort(booking.getItem()))
                .booker(UserMapper.toShort(booking.getBooker()))
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto, Item item, User user) {
        return Booking.builder()
                .booker(user)
                .item(item)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();
    }

    public static BookingShortDto toShortDto(BookingDto booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .build();
    }

    public static List<BookingDto> toListDto(Iterable<Booking> iterator) {
        if (!iterator.iterator().hasNext()) return Collections.emptyList();

        List<Booking> bookings = new ArrayList<>();
        iterator.iterator().forEachRemaining(bookings::add);
        return bookings.stream()
                .map(BookingMapping::toDto)
                .collect(Collectors.toList());
    }
}

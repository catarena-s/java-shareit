package ru.practicum.shareit.util;

import org.springframework.data.domain.Sort;

public class Constants {
    public static final String MSG_USER_WITH_ID_NOT_FOUND = "User with id=%d not found";
    public static final String MSG_ITEM_WITH_ID_NOT_FOUND = "Item with id=%d not found";
    public static final String MSG_BOOKING_WITH_ID_NOT_FOUND = "Booking with id=%d not found";
    public static final Sort SORT_BY_START_ASC = Sort.by(Sort.Direction.ASC, "start");
    public static final Sort SORT_BY_START_DESC = Sort.by("start").descending();
    public static final Sort SORT_BY_END_ASC = Sort.by("end").descending();
}

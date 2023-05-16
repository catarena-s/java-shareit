package ru.practicum.shareit.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final Clock DEFAULT_CLOCK = Clock.systemDefaultZone();

    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id={}";
    public static final String MSG_USER_WITH_ID_NOT_FOUND = "User with id=%d not found";
    public static final String MSG_ITEM_WITH_ID_NOT_FOUND = "Item with id=%d not found";
    public static final String MSG_BOOKING_WITH_ID_NOT_FOUND = "Booking with id=%d not found";

    public static final Sort SORT_BY_ID_ACS = Sort.by(Sort.Direction.ASC, "id");
    public static final Sort SORT_BY_START_ASC = Sort.by(Sort.Direction.ASC, "start");
    public static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");
    public static final Sort SORT_BY_REQUEST_CREATE_DATE_DESC = Sort.by(Sort.Direction.DESC, "created");
    public static final Clock TEST_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
}

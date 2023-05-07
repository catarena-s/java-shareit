package ru.practicum.shareit.booking.enums;

public enum BookingState {
    ALL,
    CURRENT,//текущие
    PAST,//завершённые
    FUTURE,//будущие
    WAITING,//ожидающие подтверждения
    REJECTED;//отклонённые
}

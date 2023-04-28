package ru.practicum.shareit.booking;

public enum BookingState {
    ALL,
    CURRENT,//текущие
    PAST,//завершённые
    FUTURE,//будущие
    WAITING,//ожидающие подтверждения
    REJECTED;//отклонённые

//    public static Optional<BookingState> from(String state) {
//        final String upperCase = state.toUpperCase();
//        if (!Arrays.asList(values()).contains(upperCase)) return Optional.empty();
//        return Optional.of(valueOf(upperCase));
//    }
}

package ru.practicum.shareit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestInitDataUtil {
    public static User makeUser(long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    public static Item makeItem(
            long id, String name, String description, boolean isAvailable,
            User owner, ItemRequest request) {
        return Item.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(isAvailable)
                .owner(owner)
                .request(request)
                .build();
    }

    public static Booking makeBooking(
            long id,
            Item item,
            User booker,
            LocalDateTime start,
            LocalDateTime end,
            BookingStatus status) {
        return Booking.builder()
                .id(id)
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status(status)
                .build();
    }

    public static ItemRequest maleRequest(long id, String description, User requester, LocalDateTime dateTime, List<Item> itemList) {
        return ItemRequest.builder()
                .id(id)
                .description(description)
                .requester(requester)
                .created(dateTime)
                .items((itemList == null || itemList.isEmpty()) ? Collections.emptyList() : itemList)
                .build();
    }

    @NotNull
    public static List<User> getUserList() {
        return List.of(
                makeUser(1L, "Jon", "jon@mail.ru"),
                makeUser(2L, "Jane", "jane@mail.ru"),
                makeUser(3L, "Mary", "mary@mail.ru")

        );
    }

    @NotNull
    public static List<ItemRequest> getRequestList(LocalDateTime currentTime, List<User> userList) {
        return List.of(
                maleRequest(1L, "I would like to use the item", userList.get(0), currentTime.minusDays(1), null),
                maleRequest(2L, "I would like to use the pillow", userList.get(2), currentTime.minusDays(2), null),
                maleRequest(3L, "I would like to use the screwdriver", userList.get(0), currentTime.minusDays(1),
                        List.of(makeItem(1L, "screwdriver", "screwdriver description", true, userList.get(2), null)))
        );
    }

    @NotNull
    public static List<Booking> getBookingList(BookingRepository bookingRepository, List<User> users, List<Item> items) {

        final LocalDateTime currentTime = LocalDateTime.now();
        final Booking booking1 = addBooking(bookingRepository, users.get(1), items.get(0), currentTime.minusDays(1), currentTime.plusDays(1));
        final Booking booking2 = addBooking(bookingRepository, users.get(0), items.get(1), currentTime.minusDays(1), currentTime.plusDays(1));
        final Booking booking3 = addBooking(bookingRepository, users.get(0), items.get(2), currentTime.minusDays(1), currentTime.plusDays(1));
        final Booking booking4 = addBooking(bookingRepository, users.get(1), items.get(3), currentTime.minusDays(3), currentTime.minusDays(2));
        final Booking booking5 = addBooking(bookingRepository, users.get(2), items.get(3), currentTime.minusDays(1), currentTime.plusDays(1));
        booking3.setStatus(BookingStatus.APPROVED);
        booking2.setStatus(BookingStatus.APPROVED);

        return List.of(booking1, booking2, booking3, booking4, booking5);
    }

    public static List<Item> getItemList(ItemRepository itemRepository, List<User> users) {
        final Item item1 = addItem(itemRepository,"item 1", "Item1 description", users.get(0));
        final Item item2 = addItem(itemRepository,"item 2", "Item2 description", users.get(1));
        final Item item3 = addItem(itemRepository,"item 3", "Item23 description", users.get(1));
        final Item item4 = addItem(itemRepository,"item4", "Item4 description", users.get(0));

        return List.of(item1, item2, item3, item4);
    }

    @NotNull
    public static List<User> getUserList(UserRepository userRepository) {
        final User user1 = addUser(userRepository, "Jon", "jon@mail.ru");
        final User user2 = addUser(userRepository, "Jane", "jane@mail.ru");
        final User user3 = addUser(userRepository, "Mary", "mary@mail.ru");

        return List.of(user1, user2, user3);
    }

    @NotNull
    private static Booking addBooking(BookingRepository bookingRepository, User user2, Item item1, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.save(Booking.builder()
                .status(BookingStatus.WAITING)
                .item(item1)
                .booker(user2)
                .start(start)
                .end(end)
                .build());
    }

    @NotNull
    private static User addUser(UserRepository userRepository, String Jane, String mail) {
        return userRepository.save(User.builder()
                .name(Jane).email(mail)
                .build());
    }

    @NotNull
    private static Item addItem(ItemRepository itemRepository, String name, String Item1_description, User user1) {
        return itemRepository.save(Item.builder()
                .name(name).description(Item1_description)
                .owner(user1)
                .available(true)
                .build());
    }
}

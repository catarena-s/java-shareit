package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.TestInitDataUtil;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.util.Constants.SORT_BY_START_ASC;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private List<User> users;
    private List<Item> items;
    private List<Booking> bookings;

    @BeforeEach
    void init() {
        users = TestInitDataUtil.getUserList(userRepository);
        items = TestInitDataUtil.getItemList(itemRepository, users);
        bookings = TestInitDataUtil.getBookingList(bookingRepository, users, items);
    }


    /**
     * @param index 0 - item's owner , 1 - booker
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void findByIdAndBookerOrOwner(int index) {
        final Booking expectedBooking = bookings.get(0);
        final User user = users.get(index);
        final Optional<Booking> actualBooking = bookingRepository.findByIdAndBookerOrOwner(user.getId(), expectedBooking.getId());

        assertThat(actualBooking)
                .isPresent()
                .hasValueSatisfying(booking -> {
                            assertThat(booking).hasFieldOrPropertyWithValue("id", expectedBooking.getId());
                            assertThat(booking.getItem()).hasFieldOrPropertyWithValue("id", expectedBooking.getItem().getId());
                            assertThat(booking.getBooker()).hasFieldOrPropertyWithValue("id", expectedBooking.getBooker().getId());
                            assertThat(booking).hasFieldOrPropertyWithValue("start", expectedBooking.getStart());
                            assertThat(booking).hasFieldOrPropertyWithValue("end", expectedBooking.getEnd());
                            assertThat(booking).hasFieldOrPropertyWithValue("status", expectedBooking.getStatus());
                        }
                );
    }

    @Test
    void findByIdAndBookerOrOwner_WrongOwner() {
        final Booking expectedBooking = bookings.get(0);
        final User user = users.get(2);
        final Optional<Booking> actualBooking = bookingRepository.findByIdAndBookerOrOwner(user.getId(), expectedBooking.getId());

        assertThat(actualBooking).isEmpty();
    }

    @Test
    void findAllByOwnerAndStatus() {
        final User owner = users.get(1);
        final List<Booking> expectedList = List.of(bookings.get(1), bookings.get(2));
        final List<Booking> actualList = bookingRepository.findAllByOwnerAndStatus(owner.getId(), BookingStatus.APPROVED, SORT_BY_START_ASC);

        assertEquals(expectedList, actualList);
    }

    @Test
    void findByItemIdAndOwnerAndStatus() {
        final Item item = items.get(3);
        final User owner = users.get(0);
        final List<Booking> expectedList = List.of(bookings.get(3), bookings.get(4));

        final List<Booking> actualList = bookingRepository.findByItemIdAndOwnerAndStatus(item.getId(), owner.getId(), BookingStatus.WAITING, SORT_BY_START_ASC);

        assertEquals(expectedList, actualList);
    }

    @Test
    void findByItemIdAndBookerIdAndEndBefore() {
        final Item item = items.get(3);
        final User booker = users.get(1);

        final Optional<Item> actualItem = bookingRepository.findByItemIdAndBookerIdAndEndBefore(item.getId(), booker.getId(), LocalDateTime.now());

        assertThat(actualItem)
                .isPresent();

    }

    @Test
    void findByItemIdAndBookerIdAndEndBefore_thenEmpty() {
        final Item item = items.get(1);
        final User booker = users.get(0);

        final Optional<Item> actualItem = bookingRepository.findByItemIdAndBookerIdAndEndBefore(item.getId(), booker.getId(), LocalDateTime.now());

        assertThat(actualItem)
                .isEmpty();

    }

    @Test
    void existsApprovedBookingForItemWithCrossTime() {
        final Item item = items.get(2);
        final LocalDateTime currentTime = LocalDateTime.now();
        final boolean existBooking = bookingRepository.existsApprovedBookingForItemWithCrossTime(
                item.getId(),
                currentTime,
                currentTime.plusDays(2)
        );

        assertTrue(existBooking);
    }

}
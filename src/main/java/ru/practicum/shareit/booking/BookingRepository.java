package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    /**
     * Check exists booking by Id and bookerId
     * @param bookingId
     * @param userId
     * @return true or false
     */
    boolean existsByIdAndBookerId(long bookingId, long userId);

    /**
     * Find booking by Id for booker or item's owner
     * @param userId
     * @param id
     * @return booking
     */
    @Query(value = "select b from Booking b " +
            "where (b.booker.id = :userId or b.item.owner.id = :userId) and b.id = :id ")
    Optional<Booking> findByIdAndBookerOrOwner(@Param("userId") long userId,
                                               @Param("id") long id);

    /**
     * Find all bookings for item's owner by status
     * @param ownerId item's owner
     * @param status status
     * @param order sorting order
     * @return booking list
     */
    @Query(value = "select b from Booking b " +
            "where b.item.owner.id = :ownerId and b.status = :status")
    List<Booking> findAllByOwnerAndStatus(@Param("ownerId") long ownerId,
                                          @Param("status") BookingStatus status,
                                          Sort order);

    /**
     *  Find all bookings by item's owner and itemId and status
     * @param itemId
     * @param ownerId
     * @param status
     * @param order sorting order
     * @return item booking list
     */
    @Query("select b from Booking b " +
            "where b.item.owner.id = :ownerId and b.item.id = :itemId " +
            "and b.status = :status")
    List<Booking> findByItemIdAndOwnerAndStatus(@Param("itemId") long itemId,
                                                @Param("ownerId") long ownerId,
                                                @Param("status") BookingStatus status,
                                                Sort order);

    /**
     * Get item by itemId and bookerId if it was booked before current time
     * @param itemId
     * @param bookerId
     * @param now
     * @return item
     */
    @Query("select b.item from Booking b " +
            "where b.item.id=:itemId and b.booker.id= :bookerId and b.end < :now")
    Optional<Item> findByItemIdAndBookerIdAndEndBefore(@Param("itemId") long itemId,
                                                       @Param("bookerId") long bookerId,
                                                       @Param("now") LocalDateTime now);

    /**
     * Check for an approved booking for an item with a time crossing
     * @param itemId
     * @param start booking start time
     * @param end booking end time
     * @return true or false
     */
    @Query("select case when count(b)> 0 then true else false end from Booking b " +
            "where  b.item.id = :itemId and" +
            "((:start between b.start and  b.end) or (:end between b.start and  b.end)) and " +
            "b.status = 'APPROVED'")
    boolean existsApprovedBookingForItemWithCrossTime(@Param("itemId") long itemId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);
}

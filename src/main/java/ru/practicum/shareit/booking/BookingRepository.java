package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    /**
     * Find all
     * @param booker
     * @param state
     * @param orders
     * @return
     */
    List<Booking> findAllByBookerIdAndStatus(long booker, BookingStatus state, Sort orders);

    @Query(value = "select b from Booking b " +
            "where (b.booker.id = ?1 or b.item.owner.id = ?1) and b.id = ?2 ")
    Optional<Booking> findByIdAndBookerOrOwner(long bookerId, long id);

    @Query(value = "select b from Booking b " +
//            "inner join items it on it.id = b.item_id " +
            "where b.item.owner.id = ?1 " +
            "order by b.start desc"//,
//            nativeQuery = true
    )
    List<Booking> findAllByOwner(long userId);

    //    @Query(value = "select b.* from bookings b " +
//            "inner join items it on it.id = b.item_id " +
//            "where it.owner_id = ?1 " +
//            "order by b.start desc",
//            nativeQuery = true)
    List<Booking> findAllByBookerId(long userId, Sort orders);

    @Query("select b from Booking b " +
            "inner join Item it on it.id = b.item " +
            "where it.owner.id = :ownerId and b.end < :now " +
            "order by b.start desc")
    List<Booking> findAllByOwnerAndStartBefore(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "inner join Item it on it.id = b.item " +
            "where it.owner.id = :ownerId and b.start > :now " +
            "order by b.start desc")
    List<Booking> findAllByOwnerAndStartAfter(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "inner join Item it on it.id = b.item " +
            "where it.owner.id = :ownerId and  :now between b.start and b.end " +
            "order by b.start desc")
    List<Booking> findAllByOwnerAndStart(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query(value = "select b from Booking b " +
            // "inner join Item it on it.id = b.item " +
            "where b.item.owner.id = :ownerId and b.status = :status")
    List<Booking> findAllByOwnerAndStatus(@Param("ownerId") long ownerId,
                                          @Param("status") BookingStatus status,
                                          Sort orders);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(long userId, LocalDateTime bookingStatus, LocalDateTime currentDate, Sort orders);

    List<Booking> findAllByBookerIdAndEndBefore(long userId, LocalDateTime now, Sort orders);

    List<Booking> findAllByBookerIdAndStartAfter(long userId, LocalDateTime now, Sort orders);

//    @Query("select case when count(b)> 0 then true else false end from Booking b " +
////            "inner join Item it on it.id = b.item " +
//            "where b.item.owner.id = :ownerId and b.id = :bookingId and b.status = :status")
//    boolean existsByIdAndOwnerAndStatus(
//            @Param("bookingId") long bookingId,
//            @Param("ownerId") long ownerId,
//            @Param("status") BookingStatus status);

    boolean existsByIdAndBookerId(long bookingId, long userId);

    Booking findTop1ByItemIdAndStartBeforeAndStatus(long itemId, Date from, Sort start, BookingStatus approved);

    Booking findTop1ByItemIdAndStartAfterAndStatus(long itemId, Date from, Sort start, BookingStatus approved);

    boolean existsByIdAndBookerIdAndStartAfter(long itemId, long userId, LocalDateTime from);

    @Query(value = "select b from Booking b " +
            // "inner join Item it on it.id = b.item " +
            "where b.item.owner.id = :ownerId and b.id = :bookingId " +
            "order by b.start desc")
    Optional<Booking> findByIdAndOwner(@Param("bookingId") long bookingId,
                                       @Param("ownerId") long ownerId);

    @Query("select b from Booking b " +
            "where b.start > now()")
    List<Booking> findAllNext(long userId);

    @Query("select b from Booking b " +
            "where b.end < now()")
    List<Booking> findAllLasts(long userId);

    @Query(value = "select b from Booking b " +
            "where b.item.owner.id = :ownerId and b.item.id = :itemId " +
            "and b.status = :status")
    List<Booking> findByItemIdAndOwner(@Param("itemId") long itemId,
                                       @Param("ownerId") long ownerId,
                                       @Param("status") BookingStatus status,
                                       Sort orders);

}

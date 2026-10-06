package com.bookingSystem.repository;

import com.bookingSystem.entity.Reservation;
import com.bookingSystem.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

     @Query("SELECT r FROM Reservation r WHERE r.user.id = :id")
     Page<Reservation> findAllByUserId(@Param("id") Integer id, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.resource.id = :id")
     Page<Reservation> findAllByResourceId(@Param("id") Integer id, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.user.id = :id AND r.status = :status")
    Page<Reservation> findAllByUserWithStatus(@Param("id") Integer id, @Param("status") ReservationStatus reservationStatus, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.resource.id = :id AND r.status = :status")
    Page<Reservation> findAllByResourceWithStatus(@Param("id") Integer id,@Param("status") ReservationStatus reservationStatus, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.resource.id = :id AND r.status = :status")
     List<Reservation> findAllByResourceWithStatus(@Param("id") Integer id, @Param("status") ReservationStatus status);

     @Query("SELECT r FROM Reservation r WHERE r.resource.price >= :minPrice")
    Page<Reservation> findAllByMinPrice(Pageable pageable,@Param("minPrice") BigDecimal minPrice);

     @Query("SELECT r FROM Reservation r WHERE r.resource.price <= :maxPrice")
    Page<Reservation> findAllByMaxPrice(Pageable pageable, @Param("maxPrice") BigDecimal maxPrice);

     @Query("SELECT r FROM Reservation r WHERE r.resource.price >= :minPrice AND r.resource.price <= :maxPrice")
    Page<Reservation> findAllByMaxPriceAndMinPrice(Pageable pageable,@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

     @Query("SELECT r FROM Reservation r WHERE r.user.id = :id AND r.resource.price BETWEEN :minPrice AND :maxPrice")
    Page<Reservation> findAllByUserIdWithMinPriceAndMaxPrice(@Param("id") Integer id, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.user.id = :id AND r.resource.price >= :minPrice")
    Page<Reservation> findAllByUserIdWithMinPrice(@Param("id") Integer id, @Param("minPrice") BigDecimal minPrice, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.user.id = :id AND r.resource.price <= :maxPrice")
    Page<Reservation> findAllByUserIdWithMaxPrice(@Param("id") Integer id, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.resource.id = :id AND r.resource.price BETWEEN :minPrice AND :maxPrice")
    Page<Reservation> findAllByResourceIdWithMinPriceAndMaxPrice(@Param("id") Integer id, @Param("minPrice") BigDecimal minPrice,@Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.resource.id = :id AND r.resource.price >= :minPrice")
    Page<Reservation> findAllByResourceIdWithMinPrice(@Param("id") Integer id, @Param("minPrice") BigDecimal minPrice, Pageable pageable);

     @Query("SELECT r FROM Reservation r WHERE r.resource.id = :id AND r.resource.price <= :maxPrice")
    Page<Reservation> findAllByResourceIdWithMaxPrice(@Param("id") Integer id, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

     @Query("""
            SELECT r FROM Reservation r
            WHERE r.user.id = :id
            AND r.resource.price
            BETWEEN :minPrice AND :maxPrice
            AND r.status = :status
            """)
    Page<Reservation> findAllByUserIdWithStatusBetweenMinPriceAndMaxPrice(
            @Param("id") Integer id,
            @Param("status") ReservationStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

     @Query("""
            SELECT r FROM Reservation r
            WHERE r.user.id = :id
            AND r.resource.price >= :minPrice
            AND r.status = :status
            """)
    Page<Reservation> findAllByUserIdWithStatusAndMinPrice(
            @Param("id") Integer id,
            @Param("status") ReservationStatus status,
            @Param("minPrice") BigDecimal minPrice,
            Pageable pageable);

     @Query("""
            SELECT r FROM Reservation r
            WHERE r.user.id = :id
            AND r.resource.price <= :maxPrice
            AND r.status = :status
            """)
    Page<Reservation> findAllByUserIdWithStatusAndMaxPrice(
            @Param("id") Integer id,
            @Param("status") ReservationStatus status,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

     @Query("""
            SELECT r FROM Reservation r
            WHERE r.resource.id = :id
            AND r.resource.price BETWEEN :minPrice AND :maxPrice
            AND r.status = :status
            """)
    Page<Reservation> findAllByResourceIdWithStatusBetweenMinPriceAndMaxPrice(
            @Param("id") Integer id,
            @Param("status") ReservationStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

     @Query("""
            SELECT r FROM Reservation r
            WHERE r.resource.id = :id
            AND r.resource.price >= :minPrice
            AND r.status = :status
            """)
    Page<Reservation> findAllByResourceIdWithStatusAndMinPrice(
            @Param("id") Integer id,
            @Param("status") ReservationStatus status,
            @Param("minPrice") BigDecimal minPrice,
            Pageable pageable);

     @Query("""
            SELECT r FROM Reservation r
            WHERE r.resource.id = :id
            AND r.resource.price <= :maxPrice
            AND r.status = :status
            """)
    Page<Reservation> findAllByResourceIdWithStatusAndMaxPrice(
            @Param("id") Integer id,
            @Param("status") ReservationStatus status,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}

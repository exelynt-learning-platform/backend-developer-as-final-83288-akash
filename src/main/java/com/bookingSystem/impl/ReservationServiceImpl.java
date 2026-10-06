package com.bookingSystem.impl;

import com.bookingSystem.exception.*;
import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.entity.*;
import com.bookingSystem.helper.ModelMapper;
import com.bookingSystem.helper.PageResponse;
import com.bookingSystem.repository.ReservationRepository;
import com.bookingSystem.repository.ResourceRepository;
import com.bookingSystem.repository.UserRepository;
import com.bookingSystem.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.bookingSystem.helper.AuthValidator.getCleanRole;
import static com.bookingSystem.helper.AuthValidator.validAdminAuth;
import static com.bookingSystem.helper.ModelMapper.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository repository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Override
    public void deleteReservation(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);

        boolean exists = this.repository.existsById(id);
        if (exists)
            this.repository.deleteById(id);
        else
            throw new ReservationDoesNotExistException(
                    "Reservation with id: " + id + " does not exist !!"
            );
    }

    @Override
    public ReservationResponse addReservation(ReservationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException(
                    "User is not authenticated!"
            );

        String username = authentication.getName();

        UserRole userRole = getCleanRole(authentication);

        User existingUser = this.userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserDoesNotExistException(
                        "Failed to create Reservation for user with id: "
                                + request.getUserId()
                                + " does not exist !!"
                ));

        if ((request.getStartDate() != null && request.getEndDate() != null)
                && request.getEndDate().isAfter(request.getStartDate())) {

            Reservation reservation = mapToReservation(request);
            reservation.setStatus(ReservationStatus.PENDING);

            if (userRole.equals(UserRole.USER)) {

                User authenticatedUser = this.userRepository.findByEmail(username)
                        .orElseThrow(() -> new UserDoesNotExistException(
                                "Failed to create Reservation for User with email: "
                                        + username
                                        + " does not exist !!"
                        ));

                if (!(authenticatedUser.getId().equals(existingUser.getId())))
                    throw new AuthorizationDeniedException(
                            "You can create Reservation for yourself only !!"
                    );
            }

            reservation.setUser(existingUser);

            Integer resourceId = request.getResourceId();

            Resource resource = this.resourceRepository.findById(resourceId)
                    .orElseThrow(() -> new ResourceDoesNotExistException(
                            "Failed to create Reservation for Resource with id: "
                                    + resourceId
                                    + " does not exist !!"
                    ));

            reservation.setResource(resource);

            Reservation savedReservation = this.repository.save(reservation);

            return mapToReservationResponse(savedReservation);
        }

        throw new InvalidDateException("Invalid dates !!!");
    }

    @Override
    public ReservationResponse updateReservation(
            Integer id,
            ReservationRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validAdminAuth(authentication);

        if ((request.getStartDate() == null || request.getEndDate() == null)
                || request.getEndDate().isBefore(request.getStartDate())) {

            throw new InvalidDateException("Invalid dates !!!");
        }

        Reservation existingReservation = this.repository.findById(id)
                .orElseThrow(() -> new ReservationDoesNotExistException(
                        "Reservation with id: " + id + " does not exist  !!"
                ));

        ReservationStatus status = existingReservation.getStatus();

        if (status.equals(ReservationStatus.CONFIRMED))
            throw new ReservationAlreadyConfirmedException(
                    "Reservation is already CONFIRMED!"
            );
        else if (status.equals(ReservationStatus.CANCELLED))
            throw new ReservationAlreadyCancelledException(
                    "Reservation is already CANCELED!"
            );

        boolean isAvailable = isValidDate(
                existingReservation.getResource().getId(),
                request
        );

        if (isAvailable)
            existingReservation.setStatus(ReservationStatus.CONFIRMED);
        else
            existingReservation.setStatus(ReservationStatus.CANCELLED);

        Reservation updatedReservation = this.repository.save(existingReservation);

        return mapToReservationResponse(updatedReservation);
    }

    private boolean isValidDate(Integer id, ReservationRequest request) {

        List<ReservationResponse> allReservationsByResourceWithStatus =
                this.getReservationListByResourceWithStatus(
                        id,
                        ReservationStatus.CONFIRMED.toString()
                );

        if (allReservationsByResourceWithStatus.isEmpty())
            return true;

        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();

        for (ReservationResponse reservation : allReservationsByResourceWithStatus) {

            if (startDate.isBefore(reservation.getEndDate())
                    && endDate.isAfter(reservation.getStartDate())) {

                return false;
            }
        }

        return true;
    }

    public List<ReservationResponse> getReservationListByResourceWithStatus(
            Integer id,
            String status
    ) {
        ReservationStatus reservationStatus = ReservationStatus.valueOf(status);

        List<Reservation> reservationList =
                this.repository.findAllByResourceWithStatus(
                        id,
                        reservationStatus
                );

        return mapToReservationResponseList(reservationList);
    }

    @Override
    public ReservationResponse getReservationByReservationId(Integer id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException(
                    "User is not authenticated!"
            );

        UserRole userRole = getCleanRole(authentication);

        Reservation existingReservation = this.repository.findById(id)
                .orElseThrow(() -> new ReservationDoesNotExistException(
                        "Reservation with id: " + id + " does not exist !!"
                ));

        if (userRole.equals(UserRole.ADMIN))
            return mapToReservationResponse(existingReservation);

        String username = authentication.getName();

        User authenticatedUser = this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UserDoesNotExistException(
                        "Username: " + username + " does not exist !!"
                ));

        if (!(authenticatedUser.getId().equals(
                existingReservation.getUser().getId()
        ))) {

            throw new AuthorizationDeniedException(
                    "You can access only your own Reservations!"
            );
        }

        return mapToReservationResponse(existingReservation);
    }

    @Override
    public PageResponse<ReservationResponse> getAllReservations(
            Pageable pageable,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        validAdminAuth(authentication);

        // BigDecimal comparison
        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be greater than maxPrice !!"
            );
        }

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be negative  !!"
            );
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "maxPrice can't be negative !!"
            );
        }

        if (minPrice != null) {

            if (maxPrice != null) {

                // both the minPrice and maxPrice are given
                Page<Reservation> allByMaxPriceAndMinPrice =
                        this.repository.findAllByMaxPriceAndMinPrice(
                                pageable,
                                minPrice,
                                maxPrice
                        );

                Page<ReservationResponse> reservationResponsePage =
                        allByMaxPriceAndMinPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );

            } else {

                // only minPrice is given
                Page<Reservation> allByMinPrice =
                        this.repository.findAllByMinPrice(
                                pageable,
                                minPrice
                        );

                Page<ReservationResponse> reservationResponsePage =
                        allByMinPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );
            }

        } else if (maxPrice != null) {

            // only maxPrice is given
            Page<Reservation> allByMaxPrice =
                    this.repository.findAllByMaxPrice(
                            pageable,
                            maxPrice
                    );

            Page<ReservationResponse> reservationResponsePage =
                    allByMaxPrice.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );

        } else {

            // both the minPrice and maxPrice are null
            Page<Reservation> reservations =
                    this.repository.findAll(pageable);

            Page<ReservationResponse> reservationResponsePage =
                    reservations.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );
        }
    }

    @Override
    public PageResponse<ReservationResponse> getAllReservationsByUserWithStatus(
            Integer id,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException(
                    "User is not authenticated!"
            );

        String username = authentication.getName();

        User authenticatedUser = this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UserDoesNotExistException(
                        "User with username: " + username + " does not exist  !!"
                ));

        checkStatus(status);

        if (authenticatedUser.getRole().equals(UserRole.USER)) {

            if (!authenticatedUser.getId().equals(id))
                throw new AuthorizationDeniedException("Access Denied!");
        }

        ReservationStatus reservationStatus =
                ReservationStatus.valueOf(status);

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be negative !!"
            );
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "maxPrice can't be negative !!"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be greater than maxPrice !!"
            );
        }

        if (minPrice != null) {

            if (maxPrice != null) {

                // both prices are given
                Page<Reservation>
                        allByUserIdWithStatusBetweenMinPriceAndMaxPrice =
                        this.repository
                                .findAllByUserIdWithStatusBetweenMinPriceAndMaxPrice(
                                        id,
                                        reservationStatus,
                                        minPrice,
                                        maxPrice,
                                        pageable
                                );

                Page<ReservationResponse> reservationResponsePage =
                        allByUserIdWithStatusBetweenMinPriceAndMaxPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );

            } else {

                // only min price is given
                Page<Reservation>
                        allByUserIdWithStatusAndMinPrice =
                        this.repository.findAllByUserIdWithStatusAndMinPrice(
                                id,
                                reservationStatus,
                                minPrice,
                                pageable
                        );

                Page<ReservationResponse> reservationResponsePage =
                        allByUserIdWithStatusAndMinPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );
            }

        } else if (maxPrice != null) {

            // only maxPrice is given
            Page<Reservation>
                    allByUserIdWithStatusAndMaxPrice =
                    this.repository.findAllByUserIdWithStatusAndMaxPrice(
                            id,
                            reservationStatus,
                            maxPrice,
                            pageable
                    );

            Page<ReservationResponse> reservationResponsePage =
                    allByUserIdWithStatusAndMaxPrice.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );

        } else {

            // none are given
            Page<Reservation> reservations =
                    this.repository.findAllByUserWithStatus(
                            id,
                            reservationStatus,
                            pageable
                    );

            Page<ReservationResponse> reservationResponsePage =
                    reservations.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );
        }
    }

    @Override
    public PageResponse<ReservationResponse> getAllReservationsByResourceWithStatus(
            Integer id,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        validAdminAuth(authentication);

        checkStatus(status);

        ReservationStatus reservationStatus =
                ReservationStatus.valueOf(status);

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be negative !!"
            );
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "maxPrice can't be negative !!"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be greater than maxPrice !!"
            );
        }

        if (minPrice != null) {

            if (maxPrice != null) {

                // both prices are given
                Page<Reservation>
                        allByResourceIdWithStatusBetweenMinPriceAndMaxPrice =
                        this.repository
                                .findAllByResourceIdWithStatusBetweenMinPriceAndMaxPrice(
                                        id,
                                        reservationStatus,
                                        minPrice,
                                        maxPrice,
                                        pageable
                                );

                Page<ReservationResponse> reservationResponsePage =
                        allByResourceIdWithStatusBetweenMinPriceAndMaxPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );

            } else {

                // only min price is given
                Page<Reservation>
                        allByResourceIdWithStatusAndMinPrice =
                        this.repository
                                .findAllByResourceIdWithStatusAndMinPrice(
                                        id,
                                        reservationStatus,
                                        minPrice,
                                        pageable
                                );

                Page<ReservationResponse> reservationResponsePage =
                        allByResourceIdWithStatusAndMinPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );
            }

        } else if (maxPrice != null) {

            // only maxPrice is given
            Page<Reservation>
                    allByResourceIdWithStatusAndMaxPrice =
                    this.repository
                            .findAllByResourceIdWithStatusAndMaxPrice(
                                    id,
                                    reservationStatus,
                                    maxPrice,
                                    pageable
                            );

            Page<ReservationResponse> reservationResponsePage =
                    allByResourceIdWithStatusAndMaxPrice.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );

        } else {

            // none are given
            Page<Reservation> reservations =
                    this.repository.findAllByResourceWithStatus(
                            id,
                            reservationStatus,
                            pageable
                    );

            Page<ReservationResponse> reservationResponsePage =
                    reservations.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );
        }
    }

    public void checkStatus(String status) {

        if ((!Objects.equals(status, ReservationStatus.CONFIRMED.name()))
                && (!Objects.equals(status, ReservationStatus.PENDING.name()))
                && (!Objects.equals(status, ReservationStatus.CANCELLED.name()))) {

            throw new InvalidStatusException(
                    "Invalid Reservation Status !!"
            );
        }
    }

    @Override
    public PageResponse<ReservationResponse> getAllReservationsByUserId(
            Integer id,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException(
                    "User is not authenticated!"
            );

        String username = authentication.getName();

        User authenticatedUser = this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UserDoesNotExistException(
                        "User with username: " + username + " does not exist !!"
                ));

        if (authenticatedUser.getRole().equals(UserRole.USER)) {

            if (!authenticatedUser.getId().equals(id))
                throw new AuthorizationDeniedException("Access Denied!");
        }

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be negative !!"
            );
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "maxPrice can't be negative !!"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be greater than maxPrice !!"
            );
        }

        if (minPrice != null) {

            if (maxPrice != null) {

                // both prices are given
                Page<Reservation>
                        allByUserIdWithMinPriceAndMaxPrice =
                        this.repository.findAllByUserIdWithMinPriceAndMaxPrice(
                                id,
                                minPrice,
                                maxPrice,
                                pageable
                        );

                Page<ReservationResponse> reservationResponsePage =
                        allByUserIdWithMinPriceAndMaxPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );

            } else {

                // only min price is given
                Page<Reservation>
                        allByUserIdWithMinPrice =
                        this.repository.findAllByUserIdWithMinPrice(
                                id,
                                minPrice,
                                pageable
                        );

                Page<ReservationResponse> reservationResponsePage =
                        allByUserIdWithMinPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );
            }

        } else if (maxPrice != null) {

            // only maxPrice is given
            Page<Reservation>
                    allByUserIdWithMaxPrice =
                    this.repository.findAllByUserIdWithMaxPrice(
                            id,
                            maxPrice,
                            pageable
                    );

            Page<ReservationResponse> reservationResponsePage =
                    allByUserIdWithMaxPrice.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );

        } else {

            // none are given
            Page<Reservation> reservations =
                    this.repository.findAllByUserId(id, pageable);

            Page<ReservationResponse> reservationResponsePage =
                    reservations.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );
        }
    }

    @Override
    public PageResponse<ReservationResponse> getAllReservationsByResourceId(
            Integer id,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        validAdminAuth(authentication);

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be greater than maxPrice !!"
            );
        }

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "minPrice can't be negative !!"
            );
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "maxPrice can't be negative !!"
            );
        }

        if (minPrice != null) {

            if (maxPrice != null) {

                // both are given
                Page<Reservation>
                        allByResourceIdWithMinPriceAndMaxPrice =
                        this.repository.findAllByResourceIdWithMinPriceAndMaxPrice(
                                id,
                                minPrice,
                                maxPrice,
                                pageable
                        );

                Page<ReservationResponse> reservationResponsePage =
                        allByResourceIdWithMinPriceAndMaxPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );

            } else {

                // only minPrice is given
                Page<Reservation>
                        allByResourceIdWithMinPrice =
                        this.repository.findAllByResourceIdWithMinPrice(
                                id,
                                minPrice,
                                pageable
                        );

                Page<ReservationResponse> reservationResponsePage =
                        allByResourceIdWithMinPrice.map(
                                ModelMapper::mapToReservationResponse
                        );

                return mapToReservationResponsePageResponse(
                        reservationResponsePage
                );
            }

        } else if (maxPrice != null) {

            // only maxPrice is given
            Page<Reservation>
                    allByResourceIdWithMaxPrice =
                    this.repository.findAllByResourceIdWithMaxPrice(
                            id,
                            maxPrice,
                            pageable
                    );

            Page<ReservationResponse> reservationResponsePage =
                    allByResourceIdWithMaxPrice.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );

        } else {

            // none are given
            Page<Reservation> reservations =
                    this.repository.findAllByResourceId(
                            id,
                            pageable
                    );

            Page<ReservationResponse> reservationResponsePage =
                    reservations.map(
                            ModelMapper::mapToReservationResponse
                    );

            return mapToReservationResponsePageResponse(
                    reservationResponsePage
            );
        }
    }
}
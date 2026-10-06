package com.bookingSystem.impl;

import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.entity.*;
import com.bookingSystem.exception.*;
import com.bookingSystem.helper.PageResponse;
import com.bookingSystem.repository.ReservationRepository;
import com.bookingSystem.repository.ResourceRepository;
import com.bookingSystem.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 2);
    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 3, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 5, 16, 0);

    @Mock
    private Authentication authentication;

    @Mock
    private ReservationRepository repository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    // ------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    private static User user(int id, String email) {
        return User.builder().id(id).email(email).role(UserRole.USER).build();
    }

    private static Resource resource(int id) {
        return Resource.builder().id(id).build();
    }

    private static Resource resource(int id, double price) {
        return Resource.builder().id(id).price(bd(price)).build();
    }

    private static Reservation reservation(int id, ReservationStatus status, User user, Resource resource) {
        return reservation(id, status, user, resource, null, null);
    }

    private static Reservation reservation(int id, ReservationStatus status, User user, Resource resource,
                                           LocalDateTime start, LocalDateTime end) {
        return Reservation.builder()
                .id(id)
                .status(status)
                .user(user)
                .resource(resource)
                .startDate(start)
                .endDate(end)
                .build();
    }

    private static ReservationRequest request(int userId, int resourceId, LocalDateTime start, LocalDateTime end) {
        return ReservationRequest.builder()
                .userId(userId)
                .resourceId(resourceId)
                .startDate(start)
                .endDate(end)
                .build();
    }

    /** Builds a page of reservations (ids 1 to n) with the given statuses. */
    private static Page<Reservation> reservationPage(User user, Resource resource, ReservationStatus... statuses) {
        List<Reservation> content = new ArrayList<>();
        for (int i = 0; i < statuses.length; i++) {
            content.add(reservation(i + 1, statuses[i], user, resource));
        }
        return new PageImpl<>(content, PAGEABLE, content.size());
    }

    /** Puts the mocked Authentication into the SecurityContext. */
    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /** Authenticated with only an email (no authorities stubbed). */
    private void authenticateWithEmail(String email) {
        when(authentication.getName()).thenReturn(email);
        authenticate();
    }

    private void authenticateAsAdmin() {
        when(authentication.getAuthorities())
                .then(a -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        authenticate();
    }

    private void authenticateAsAdmin(String email) {
        when(authentication.getName()).thenReturn(email);
        authenticateAsAdmin();
    }

    private void authenticateAsUser() {
        when(authentication.getAuthorities())
                .then(a -> List.of(new SimpleGrantedAuthority("ROLE_USER")));
        authenticate();
    }

    private void authenticateAsUser(String email) {
        when(authentication.getName()).thenReturn(email);
        authenticateAsUser();
    }

    // ------------------------------------------------------------------
    // addReservation
    // ------------------------------------------------------------------

    @Test
    void addReservation_shouldCreateReservationSuccessfully() {
        User user = user(9, "rohit@gmail.com");
        Resource resource = resource(205, 98000.00);
        ReservationRequest request = request(9, 205, START, END);
        Reservation savedReservation = reservation(118, ReservationStatus.PENDING, user, resource, START, END);

        authenticateAsUser("rohit@gmail.com");
        when(resourceRepository.findById(205)).thenReturn(Optional.of(resource));
        when(userRepository.findById(9)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("rohit@gmail.com")).thenReturn(Optional.of(user));
        when(repository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationResponse response = reservationService.addReservation(request);

        assertNotNull(response);
        assertEquals(118, response.getId());
        assertEquals(ReservationStatus.PENDING.toString(), response.getStatus());

        verify(userRepository).findById(9);
        verify(userRepository).findByEmail("rohit@gmail.com");
        verify(resourceRepository).findById(205);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(repository).save(captor.capture());

        Reservation saved = captor.getValue();
        assertEquals(ReservationStatus.PENDING, saved.getStatus());
        assertEquals(user, saved.getUser());
        assertEquals(resource, saved.getResource());
    }

    @Test
    void addReservation_shouldThrowException_whenUserCreatesReservationForAnotherUser() {
        ReservationRequest request = request(10, 205, START, END);
        User existingUser = user(10, "someone@gmail.com");
        User authenticatedUser = user(9, "rohit@gmail.com");

        when(userRepository.findById(10)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("rohit@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        authenticateAsUser("rohit@gmail.com");

        assertThrows(AuthorizationDeniedException.class, () -> reservationService.addReservation(request));

        verify(resourceRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void addReservation_shouldCreateReservationWhenRoleIsAdmin() {
        ReservationRequest request = request(9, 205, START, END);
        User existingUser = user(9, "rohit@gmail.com");
        Resource existingResource = resource(205, 98000.00);
        Reservation savedReservation = reservation(118, ReservationStatus.PENDING, existingUser, existingResource, START, END);

        when(resourceRepository.findById(205)).thenReturn(Optional.of(existingResource));
        when(userRepository.findById(9)).thenReturn(Optional.of(existingUser));
        authenticateAsAdmin("akash@gmail.com");
        when(repository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationResponse response = reservationService.addReservation(request);

        assertNotNull(response);
        assertEquals(118, response.getId());
        assertEquals(ReservationStatus.PENDING.toString(), response.getStatus());

        verify(userRepository).findById(9);
        verify(resourceRepository).findById(205);
        // ADMIN should not need to be looked up by email
        verify(userRepository, never()).findByEmail(any());

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(repository).save(captor.capture());

        Reservation saved = captor.getValue();
        assertEquals(ReservationStatus.PENDING, saved.getStatus());
        assertEquals(existingUser, saved.getUser());
        assertEquals(existingResource, saved.getResource());
    }

    @Test
    void addReservation_shouldThrowException_WhenUserCreateReservationForAUserWhichDoesNotExist() {
        ReservationRequest request = request(10, 205, START, END);

        authenticateAsUser("rohit@gmail.com");
        when(userRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> reservationService.addReservation(request));

        verify(userRepository).findById(10);
        verify(resourceRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void addReservation_shouldThrowException_ResourceDoesNotExist() {
        ReservationRequest request = request(10, 205, START, END);
        User user = user(10, "rohit@gmail.com");

        authenticateAsUser("rohit@gmail.com");
        when(userRepository.findByEmail("rohit@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(10)).thenReturn(Optional.of(user));
        when(resourceRepository.findById(205)).thenReturn(Optional.empty());

        assertThrows(ResourceDoesNotExistException.class, () -> reservationService.addReservation(request));

        verify(resourceRepository).findById(205);
        verify(repository, never()).save(any());
    }

    @Test
    void addReservation_shouldThrowException_InvalidDateException() {
        // start date is after end date
        ReservationRequest request = request(10, 205, LocalDateTime.of(2026, 12, 6, 10, 0), END);
        User user = user(10, "rohit@gmail.com");

        authenticateAsUser("rohit@gmail.com");
        when(userRepository.findById(10)).thenReturn(Optional.of(user));

        assertThrows(InvalidDateException.class, () -> reservationService.addReservation(request));

        verify(userRepository).findById(10);
        verify(resourceRepository, never()).findById(any());
        verify(userRepository, never()).findByEmail(any());
        verify(repository, never()).save(any());
    }

    @Test
    void addReservation_shouldThrowException_AuthenticationCredentialsNotFoundException() {
        ReservationRequest request = request(10, 205, START, END);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> reservationService.addReservation(request));

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).findById(any());
        verify(resourceRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // updateReservation
    // ------------------------------------------------------------------

    @Test
    void updateReservation_shouldThrowException_whenUserIsNotAdmin() {
        authenticateAsUser();

        assertThrows(AuthorizationDeniedException.class,
                () -> reservationService.updateReservation(118, new ReservationRequest()));

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).findById(any());
        verify(resourceRepository, never()).findById(any());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void updateReservation_shouldUpdateReservationSuccessfully() {
        Integer id = 118;
        ReservationRequest updateRequest = request(10, 205, START, END);
        User existingUser = user(10, "rohit@gmail.com");
        Resource existingResource = resource(205, 98000.00);
        Reservation existingReservation =
                reservation(118, ReservationStatus.PENDING, existingUser, existingResource, START, END);
        Reservation updatedReservation =
                reservation(118, ReservationStatus.CONFIRMED, existingUser, existingResource, START, END);

        authenticateAsAdmin();
        when(repository.findById(id)).thenReturn(Optional.of(existingReservation));
        // NOTE: the old `when(reservationService.getReservationListByResourceWithStatus(...))` line was
        // removed: it stubbed the real service (not a mock), which makes Mockito throw. A mocked
        // repository already returns an empty List by default, so "no conflicting confirmed
        // reservations" is the default behavior here.
        when(repository.save(any(Reservation.class))).thenReturn(updatedReservation);

        ReservationResponse response = reservationService.updateReservation(id, updateRequest);

        assertNotNull(response);
        assertEquals(id, response.getId());

        verify(repository).findById(id);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(repository).save(captor.capture());

        Reservation updated = captor.getValue();
        assertEquals(ReservationStatus.CONFIRMED, updated.getStatus());
        assertEquals(existingUser, updated.getUser());
        assertEquals(existingResource, updated.getResource());
    }

    @Test
    void updateReservation_shouldThrowException_ReservationDoesNotExist() {
        Integer id = 1;
        ReservationRequest updateRequest = request(10, 205, START, END);

        authenticateAsAdmin();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ReservationDoesNotExistException.class,
                () -> reservationService.updateReservation(id, updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    void updateReservation_shouldRejectReservationUpdate_whenStatusIsConfirmed() {
        assertUpdateRejectedForStatus(ReservationStatus.CONFIRMED, ReservationAlreadyConfirmedException.class);
    }

    @Test
    void updateReservation_shouldRejectReservationUpdate_whenStatusIsCancelled() {
        assertUpdateRejectedForStatus(ReservationStatus.CANCELLED, ReservationAlreadyCancelledException.class);
    }

    private void assertUpdateRejectedForStatus(ReservationStatus status, Class<? extends Throwable> expected) {
        Integer id = 1;
        ReservationRequest updateRequest = request(10, 205, START, END);
        Reservation existingReservation =
                reservation(1, status, user(10, "rohit@gmail.com"), resource(205, 98000.00), START, END);

        authenticateAsAdmin();
        when(repository.findById(id)).thenReturn(Optional.of(existingReservation));

        assertThrows(expected, () -> reservationService.updateReservation(id, updateRequest));

        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // deleteReservation
    // ------------------------------------------------------------------

    @Test
    void deleteReservation_shouldDeleteExistingReservation() {
        authenticateAsAdmin();
        when(repository.existsById(1)).thenReturn(true);

        reservationService.deleteReservation(1);

        verify(repository).existsById(1);
        verify(repository).deleteById(1);
    }

    @Test
    void deleteReservation_shouldThrowException_ReservationDoesNotExist() {
        authenticateAsAdmin();
        when(repository.existsById(1)).thenReturn(false);

        assertThrows(ReservationDoesNotExistException.class, () -> reservationService.deleteReservation(1));

        verify(repository).existsById(1);
        verify(repository, never()).deleteById(any());
    }

    // ------------------------------------------------------------------
    // getReservationByReservationId
    // ------------------------------------------------------------------

    @Test
    void getReservationByReservationId_shouldSuccessfullyGetReservationByIdToAdmin() {
        Reservation reservation =
                reservation(2, ReservationStatus.PENDING, user(20, null), resource(30));

        authenticateAsAdmin();
        when(repository.findById(any())).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationByReservationId(2);

        assertNotNull(response);
        assertEquals(reservation.getId(), response.getId());
        assertEquals(reservation.getUser().getId(), response.getUser().getId());
        assertEquals(reservation.getStatus().toString(), response.getStatus());
        assertEquals(reservation.getResource().getId(), response.getResource().getId());

        verify(repository).findById(2);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void getReservationByReservationId_shouldSuccessfullyGetReservationByIdToOwnerUser() {
        User user = user(20, "user@gmail.com");
        Reservation reservation = reservation(2, ReservationStatus.PENDING, user, resource(30));

        authenticateAsUser("user@gmail.com");
        when(repository.findById(2)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        ReservationResponse response = reservationService.getReservationByReservationId(2);

        assertNotNull(response);
        assertEquals(reservation.getId(), response.getId());
        assertEquals(reservation.getUser().getId(), response.getUser().getId());
        assertEquals(reservation.getUser().getEmail(), response.getUser().getEmail());
        assertEquals(reservation.getResource().getId(), response.getResource().getId());

        verify(repository).findById(2);
        verify(userRepository).findByEmail("user@gmail.com");
    }

    @Test
    void getReservationByReservationId_shouldThrowException_whenUserGetReservationOfAnotherUser() {
        Reservation reservation =
                reservation(2, ReservationStatus.PENDING, user(20, "user20@gmail.com"), resource(30));
        User authenticatedUser = user(19, "user19@gmail.com");

        authenticateAsUser("user19@gmail.com");
        when(repository.findById(2)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("user19@gmail.com")).thenReturn(Optional.of(authenticatedUser));

        assertThrows(AuthorizationDeniedException.class,
                () -> reservationService.getReservationByReservationId(2));

        verify(repository).findById(2);
        verify(userRepository).findByEmail("user19@gmail.com");
    }

    @Test
    void getReservationByReservationId_shouldThrowException_ReservationDoesNotExistException() {
        authenticateAsUser();
        when(repository.findById(100)).thenReturn(Optional.empty());

        assertThrows(ReservationDoesNotExistException.class,
                () -> reservationService.getReservationByReservationId(100));

        verify(repository).findById(100);
        verify(authentication, never()).getName();
        verify(userRepository, never()).findByEmail(any());
    }

    // Edge case: defensive check
    @Test
    void getReservationByReservationId_shouldThrowException_UserDoesNotExistException() {
        Reservation reservation =
                reservation(2, ReservationStatus.PENDING, user(20, "user20@gmail.com"), resource(30));

        authenticateAsUser("user20@gmail.com");
        when(repository.findById(2)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("user20@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class,
                () -> reservationService.getReservationByReservationId(2));

        verify(repository).findById(2);
        verify(userRepository).findByEmail("user20@gmail.com");
    }

    @Test
    void getReservationByReservationId_shouldThrowException_AuthenticationCredentialsNotFoundException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> reservationService.getReservationByReservationId(2));
    }

    // ------------------------------------------------------------------
    // getAllReservations (admin only)
    // ------------------------------------------------------------------

    @Test
    void getAllReservations_shouldSuccessfullyGetAllReservationsForAdmin() {
        Page<Reservation> page = reservationPage(user(20, "user20@gmail.com"), resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateAsAdmin();
        when(repository.findAll(PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService.getAllReservations(PAGEABLE, null, null);

        assertNotNull(result);
        assertEquals(page.getContent().getFirst().getStatus().toString(), result.content().getFirst().getStatus());
        assertEquals(page.getContent().getLast().getStatus().toString(), result.content().getLast().getStatus());

        verify(repository).findAll(PAGEABLE);
    }

    @Test
    void getAllReservations_shouldThrowException_AuthorizationDeniedException() {
        authenticateAsUser();

        assertThrows(AuthorizationDeniedException.class,
                () -> reservationService.getAllReservations(PAGEABLE, null, null));

        verify(repository, never()).findAll(PAGEABLE);
    }

    @Test
    void getAllReservations_shouldGetAllReservations_whenMinPriceIsGiven() {
        BigDecimal minPrice = bd(2000.0);
        Page<Reservation> page = reservationPage(user(20, "user20@gmail.com"), resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateAsAdmin();
        when(repository.findAllByMinPrice(PAGEABLE, minPrice)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService.getAllReservations(PAGEABLE, minPrice, null);

        assertNotNull(result);
        assertEquals(page.getContent().getFirst().getUser().getId(), result.content().getFirst().getUser().getId());
        assertEquals(page.getContent().getLast().getResource().getId(), result.content().getLast().getResource().getId());

        verify(repository).findAllByMinPrice(PAGEABLE, minPrice);
        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(), any(), any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(PAGEABLE);
    }

    @Test
    void getAllReservations_shouldGetAllReservations_whenMaxPriceIsGiven() {
        BigDecimal maxPrice = bd(20000.0);
        Page<Reservation> page = reservationPage(user(20, "user20@gmail.com"), resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateAsAdmin();
        when(repository.findAllByMaxPrice(PAGEABLE, maxPrice)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService.getAllReservations(PAGEABLE, null, maxPrice);

        assertNotNull(result);
        assertEquals(page.getContent().getFirst().getUser().getId(), result.content().getFirst().getUser().getId());
        assertEquals(page.getContent().getLast().getResource().getId(), result.content().getLast().getResource().getId());

        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository).findAllByMaxPrice(PAGEABLE, maxPrice);
        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(), any(), any());
        verify(repository, never()).findAll(PAGEABLE);
    }

    @Test
    void getAllReservations_shouldGetAllReservations_whenMinPriceAndMaxPriceGiven() {
        BigDecimal minPrice = bd(1000.0);
        BigDecimal maxPrice = bd(30000.0);
        Page<Reservation> page = reservationPage(user(20, "user20@gmail.com"), resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateAsAdmin();
        when(repository.findAllByMaxPriceAndMinPrice(PAGEABLE, minPrice, maxPrice)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService.getAllReservations(PAGEABLE, minPrice, maxPrice);

        assertNotNull(result);
        assertEquals(page.getContent().getFirst().getUser().getId(), result.content().getFirst().getUser().getId());
        assertEquals(page.getContent().getLast().getResource().getId(), result.content().getLast().getResource().getId());

        verify(repository).findAllByMaxPriceAndMinPrice(PAGEABLE, minPrice, maxPrice);
        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(PAGEABLE);
    }

    @Test
    void getAllReservations_shouldThrowException_IllegalArgumentException_whenMinPriceIsGreaterThanMaxPrice() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.getAllReservations(Pageable.unpaged(), bd(3000.0), bd(2000.0)));

        verifyNoAdminPriceQueries();
    }

    @Test
    void getAllReservations_shouldThrowException_IllegalArgumentException_whenMinPriceIsNegative() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.getAllReservations(Pageable.unpaged(), bd(-3000.0), null));

        verifyNoAdminPriceQueries();
    }

    @Test
    void getAllReservations_shouldThrowException_IllegalArgumentException_whenMaxPriceIsNegative() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.getAllReservations(Pageable.unpaged(), null, bd(-2000.0)));

        verifyNoAdminPriceQueries();
    }

    private void verifyNoAdminPriceQueries() {
        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(), any(), any());
        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(any(Pageable.class));
    }

    // ------------------------------------------------------------------
    // getAllReservationsByUserId
    // ------------------------------------------------------------------

    @Test
    void getReservationByUserId_shouldReturnUserReservations() {
        User authenticatedUser = user(20, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserId(20, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByUserId(20, null, null, PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserId(20, PAGEABLE);
    }

    @Test
    void getReservationByUserId_shouldReturnUserReservations_withMinPrice() {
        BigDecimal minPrice = bd(100.0);
        User authenticatedUser = user(20, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithMinPrice(20, minPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByUserId(20, minPrice, null, PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithMinPrice(20, minPrice, PAGEABLE);
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());
    }

    @Test
    void getReservationByUserId_shouldReturnUserReservations_withMaxPrice() {
        BigDecimal maxPrice = bd(100000.0);
        User authenticatedUser = user(20, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithMaxPrice(20, maxPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByUserId(20, null, maxPrice, PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithMaxPrice(20, maxPrice, PAGEABLE);
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());
    }

    @Test
    void getReservationByUserId_shouldReturnUserReservations_withMaxPriceAndMinPrice() {
        BigDecimal minPrice = bd(100.0);
        BigDecimal maxPrice = bd(10000.0);
        User authenticatedUser = user(20, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(30, 2000.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithMinPriceAndMaxPrice(20, minPrice, maxPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByUserId(20, minPrice, maxPrice, PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithMinPriceAndMaxPrice(20, minPrice, maxPrice, PAGEABLE);
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());
    }

    @Test
    void getReservationByUserId_shouldThrowException_whenMinPriceGreaterThanMaxPrice() {
        assertInvalidPriceForUserReservations(bd(20000.0), bd(10000.0));
    }

    @Test
    void getReservationByUserId_shouldThrowException_whenMinPriceIsNegative() {
        assertInvalidPriceForUserReservations(bd(-20000.0), null);
    }

    @Test
    void getReservationByUserId_shouldThrowException_whenMaxPriceIsNegative() {
        assertInvalidPriceForUserReservations(null, bd(-10000.0));
    }

    private void assertInvalidPriceForUserReservations(BigDecimal minPrice, BigDecimal maxPrice) {
        User authenticatedUser = user(20, "user@gmail.com");

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.getAllReservationsByUserId(20, minPrice, maxPrice, Pageable.unpaged()));

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());
    }

    @Test
    void getReservationByUserId_shouldThrowException_AuthenticationCredentialsNotFoundException() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> reservationService.getAllReservationsByUserId(1, null, null, Pageable.unpaged()));
    }

    @Test
    void getReservationByUserId_shouldThrowException_UserDoesNotExistException() {
        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class,
                () -> reservationService.getAllReservationsByUserId(1, null, null, Pageable.unpaged()));
    }

    // ------------------------------------------------------------------
    // getAllReservationsByUserWithStatus
    // ------------------------------------------------------------------

    @Test
    void getAllReservationsByUserWithStatus_shouldGetAllUserReservationsByUserWithStatus() {
        User authenticatedUser = user(2, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(5),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserWithStatus(2, ReservationStatus.PENDING, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService
                .getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), null, null, PAGEABLE);

        assertUserStatusPage(page, result);

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserWithStatus(2, ReservationStatus.PENDING, PAGEABLE);
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldReturnAllUserReservations_ByStatusWithMaxPriceAndMinPrice() {
        BigDecimal minPrice = bd(1000.0);
        BigDecimal maxPrice = bd(20000.0);
        User authenticatedUser = user(2, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(5),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithStatusBetweenMinPriceAndMaxPrice(
                2, ReservationStatus.PENDING, minPrice, maxPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService
                .getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), minPrice, maxPrice, PAGEABLE);

        assertUserStatusPage(page, result);

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithStatusBetweenMinPriceAndMaxPrice(
                2, ReservationStatus.PENDING, minPrice, maxPrice, PAGEABLE);
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldReturnAllUserReservations_ByStatusWithMinPrice() {
        BigDecimal minPrice = bd(1000.0);
        User authenticatedUser = user(2, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(5),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithStatusAndMinPrice(2, ReservationStatus.PENDING, minPrice, PAGEABLE))
                .thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService
                .getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), minPrice, null, PAGEABLE);

        assertUserStatusPage(page, result);

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithStatusAndMinPrice(2, ReservationStatus.PENDING, minPrice, PAGEABLE);
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldReturnAllUserReservations_ByStatusWithMaxPrice() {
        BigDecimal maxPrice = bd(1000.0);
        User authenticatedUser = user(2, "user@gmail.com");
        Page<Reservation> page = reservationPage(authenticatedUser, resource(5),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithStatusAndMaxPrice(2, ReservationStatus.PENDING, maxPrice, PAGEABLE))
                .thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService
                .getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), null, maxPrice, PAGEABLE);

        assertUserStatusPage(page, result);

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithStatusAndMaxPrice(2, ReservationStatus.PENDING, maxPrice, PAGEABLE);
    }

    private void assertUserStatusPage(Page<Reservation> expected, PageResponse<ReservationResponse> actual) {
        assertNotNull(actual);
        assertEquals(expected.getContent().getFirst().getUser().getId(), actual.content().getFirst().getUser().getId());
        assertEquals(expected.getContent().getLast().getResource().getId(), actual.content().getLast().getResource().getId());
        assertEquals(expected.getContent().getFirst().getStatus().toString(), actual.content().getFirst().getStatus());
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenMinPriceGreaterThanMaxPrice() {
        assertUserWithStatusFailsForAuthenticatedUser(IllegalArgumentException.class, 2,
                ReservationStatus.PENDING.name(), bd(10000.0), bd(2000.0));
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenMinPriceIsNegative() {
        assertUserWithStatusFailsForAuthenticatedUser(IllegalArgumentException.class, 2,
                ReservationStatus.PENDING.name(), bd(-10000.0), null);
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenMaxPriceIsNegative() {
        assertUserWithStatusFailsForAuthenticatedUser(IllegalArgumentException.class, 2,
                ReservationStatus.PENDING.name(), null, bd(-200.0));
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_AuthorizationDeniedException() {
        // authenticated user has id 2 but asks for the reservations of user 3
        assertUserWithStatusFailsForAuthenticatedUser(AuthorizationDeniedException.class, 3,
                ReservationStatus.PENDING.name(), bd(200.0), bd(400.0));
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_InvalidStatusException() {
        assertUserWithStatusFailsForAuthenticatedUser(InvalidStatusException.class, 2,
                "COMPLETED", bd(200.0), bd(400.0));
    }

    /** Authenticated user is always id=2 / user@gmail.com. */
    private void assertUserWithStatusFailsForAuthenticatedUser(Class<? extends Throwable> expected, int userId,
                                                               String status, BigDecimal minPrice, BigDecimal maxPrice) {
        User authenticatedUser = user(2, "user@gmail.com");

        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(authenticatedUser));

        assertThrows(expected, () -> reservationService
                .getAllReservationsByUserWithStatus(userId, status, minPrice, maxPrice, Pageable.unpaged()));

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_UserDoesNotExistException() {
        authenticateWithEmail("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> reservationService
                .getAllReservationsByUserWithStatus(2, ReservationStatus.CONFIRMED.name(), bd(200.0), bd(400.0),
                        Pageable.unpaged()));

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenUserIsNotAuthenticated() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> reservationService
                .getAllReservationsByUserWithStatus(2, "CONFIRMED", null, null, Pageable.unpaged()));
    }

    // ------------------------------------------------------------------
    // getAllReservationsByResourceId (admin only)
    // ------------------------------------------------------------------

    @Test
    void getAllReservationsByResource_shouldReturnAllReservationsByResource() {
        Page<Reservation> page = reservationPage(user(1, null), resource(2),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateAsAdmin();
        when(repository.findAllByResourceId(2, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByResourceId(2, null, null, PAGEABLE);

        assertNotNull(result);
        assertEquals(page.getContent().getFirst().getResource().getId(), result.content().getFirst().getResource().getId());

        verify(repository).findAllByResourceId(2, PAGEABLE);
    }

    @Test
    void getAllReservationsByResource_shouldReturnAllReservationsMadeForAResourceBetweenMinPriceAndMaxPrice() {
        BigDecimal minPrice = bd(100.0);
        BigDecimal maxPrice = bd(20000.0);
        Page<Reservation> page = reservationPage(user(1, null), resource(1, 200.0),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateAsAdmin();
        when(repository.findAllByResourceIdWithMinPriceAndMaxPrice(1, minPrice, maxPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByResourceId(1, minPrice, maxPrice, PAGEABLE);

        assertResourcePage(page, result);

        verify(repository).findAllByResourceIdWithMinPriceAndMaxPrice(1, minPrice, maxPrice, PAGEABLE);
    }

    @Test
    void getAllReservationsByResource_shouldReturnAllReservationsMadeForAResourceWithMinPrice() {
        BigDecimal minPrice = bd(100.0);
        Page<Reservation> page = reservationPage(user(1, null), resource(1, 200.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateAsAdmin();
        when(repository.findAllByResourceIdWithMinPrice(1, minPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByResourceId(1, minPrice, null, PAGEABLE);

        assertResourcePage(page, result);

        verify(repository).findAllByResourceIdWithMinPrice(1, minPrice, PAGEABLE);
    }

    @Test
    void getAllReservationsByResource_shouldReturnAllReservationsMadeForAResourceWithMaxPrice() {
        BigDecimal maxPrice = bd(10000.0);
        Page<Reservation> page = reservationPage(user(1, null), resource(1, 200.0),
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        authenticateAsAdmin();
        when(repository.findAllByResourceIdWithMaxPrice(1, maxPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result =
                reservationService.getAllReservationsByResourceId(1, null, maxPrice, PAGEABLE);

        assertResourcePage(page, result);

        verify(repository).findAllByResourceIdWithMaxPrice(1, maxPrice, PAGEABLE);
    }

    private void assertResourcePage(Page<Reservation> expected, PageResponse<ReservationResponse> actual) {
        assertNotNull(actual);
        assertEquals(expected.getContent().getFirst().getUser().getId(), actual.content().getFirst().getUser().getId());
        assertEquals(expected.getTotalElements(), actual.totalElements());
    }

    @Test
    void getAllReservationsByResourceId_shouldThrowException_whenMinPriceGreaterThanMaxPrice() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> reservationService
                .getAllReservationsByResourceId(1, bd(10000.0), bd(1000.0), Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceId_shouldThrowException_whenMinPriceIsNegative() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> reservationService
                .getAllReservationsByResourceId(1, bd(-10000.0), null, Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceId_shouldThrowException_whenMaxPriceIsNegative() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> reservationService
                .getAllReservationsByResourceId(1, null, bd(-10000.0), Pageable.unpaged()));
    }

    @Test
    void getReservationsByResourceId_shouldThrowException_whenAuthenticatedUserIsNotAdmin() {
        authenticateAsUser();

        assertThrows(AuthorizationDeniedException.class, () -> reservationService
                .getAllReservationsByResourceId(1, null, null, Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceId_shouldThrowException_whenUserIsNotAuthenticated() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> reservationService
                .getAllReservationsByResourceId(1, null, null, Pageable.unpaged()));
    }

    // ------------------------------------------------------------------
    // getAllReservationsByResourceWithStatus (admin only)
    // ------------------------------------------------------------------

    @Test
    void getAllReservationsByResourceWithStatus_shouldReturnAllResourceReservationsWithStatus() {
        Page<Reservation> page = reservationPage(user(1, null), resource(1),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateAsAdmin();
        when(repository.findAllByResourceWithStatus(1, ReservationStatus.PENDING, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService
                .getAllReservationsByResourceWithStatus(1, ReservationStatus.PENDING.name(), null, null, PAGEABLE);

        assertResourceStatusPage(page, result);

        verify(repository).findAllByResourceWithStatus(1, ReservationStatus.PENDING, PAGEABLE);
    }

    @Test
    void getAllReservationsByResourceWithStatus_shouldReturnAllResourceReservationsWithStatusBetweenMinPriceAndMaxPrice() {
        BigDecimal minPrice = bd(100.0);
        BigDecimal maxPrice = bd(1000.0);
        Page<Reservation> page = reservationPage(user(1, null), resource(1),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateAsAdmin();
        when(repository.findAllByResourceIdWithStatusBetweenMinPriceAndMaxPrice(
                1, ReservationStatus.PENDING, minPrice, maxPrice, PAGEABLE)).thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService.getAllReservationsByResourceWithStatus(
                1, ReservationStatus.PENDING.name(), minPrice, maxPrice, PAGEABLE);

        assertResourceStatusPage(page, result);

        verify(repository).findAllByResourceIdWithStatusBetweenMinPriceAndMaxPrice(
                1, ReservationStatus.PENDING, minPrice, maxPrice, PAGEABLE);
    }

    @Test
    void getAllReservationsByResourceWithStatus_shouldReturnAllResourceReservationsWithStatusWithMinPrice() {
        BigDecimal minPrice = bd(100.0);
        Page<Reservation> page = reservationPage(user(1, null), resource(1),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateAsAdmin();
        when(repository.findAllByResourceIdWithStatusAndMinPrice(1, ReservationStatus.PENDING, minPrice, PAGEABLE))
                .thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService.getAllReservationsByResourceWithStatus(
                1, ReservationStatus.PENDING.name(), minPrice, null, PAGEABLE);

        assertResourceStatusPage(page, result);

        verify(repository).findAllByResourceIdWithStatusAndMinPrice(1, ReservationStatus.PENDING, minPrice, PAGEABLE);
    }

    @Test
    void getAllReservationsByResourceWithStatus_shouldReturnAllResourceReservationsWithStatusAndMaxPrice() {
        BigDecimal maxPrice = bd(10000.0);
        Page<Reservation> page = reservationPage(user(1, null), resource(1),
                ReservationStatus.PENDING, ReservationStatus.PENDING);

        authenticateAsAdmin();
        when(repository.findAllByResourceIdWithStatusAndMaxPrice(1, ReservationStatus.PENDING, maxPrice, PAGEABLE))
                .thenReturn(page);

        PageResponse<ReservationResponse> result = reservationService.getAllReservationsByResourceWithStatus(
                1, ReservationStatus.PENDING.name(), null, maxPrice, PAGEABLE);

        assertResourceStatusPage(page, result);

        verify(repository).findAllByResourceIdWithStatusAndMaxPrice(1, ReservationStatus.PENDING, maxPrice, PAGEABLE);
    }

    private void assertResourceStatusPage(Page<Reservation> expected, PageResponse<ReservationResponse> actual) {
        assertNotNull(actual);
        assertEquals(expected.getTotalPages(), actual.totalPages());
        assertEquals(expected.getContent().getFirst().getResource().getId(), actual.content().getFirst().getResource().getId());
    }

    @Test
    void getAllReservationsByResourceIdWithStatus_shouldThrowException_whenMinPriceIsGreaterThanMaxPrice() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> reservationService.getAllReservationsByResourceWithStatus(
                1, ReservationStatus.PENDING.name(), bd(3000.0), bd(200.0), Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceIdWithStatus_shouldThrowException_whenMinPriceIsNegative() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> reservationService.getAllReservationsByResourceWithStatus(
                1, ReservationStatus.PENDING.name(), bd(-3000.0), bd(200.0), Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceIdWithStatus_shouldThrowException_whenMaxPriceIsNegative() {
        authenticateAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> reservationService.getAllReservationsByResourceWithStatus(
                1, ReservationStatus.PENDING.name(), bd(10.0), bd(-200.0), Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceIdWithStatus_shouldThrowException_whenStatusIsInvalid() {
        authenticateAsAdmin();

        assertThrows(InvalidStatusException.class, () -> reservationService
                .getAllReservationsByResourceWithStatus(1, "COMPLETED", null, null, Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceIdWithStatus_shouldThrowException_whenUserIsNotAdmin() {
        authenticateAsUser();

        assertThrows(AuthorizationDeniedException.class, () -> reservationService
                .getAllReservationsByResourceWithStatus(1, ReservationStatus.CONFIRMED.name(), null, null,
                        Pageable.unpaged()));
    }

    @Test
    void getAllReservationsByResourceIdWithStatus_shouldThrowException_whenUserIsNotAuthenticated() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> reservationService
                .getAllReservationsByResourceWithStatus(1, ReservationStatus.CONFIRMED.name(), null, null,
                        Pageable.unpaged()));
    }
}

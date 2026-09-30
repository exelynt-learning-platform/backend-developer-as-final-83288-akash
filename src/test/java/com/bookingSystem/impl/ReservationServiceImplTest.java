package com.bookingSystem.impl;

import com.bookingSystem.dto.ReservationRequest;
import com.bookingSystem.dto.ReservationResponse;
import com.bookingSystem.entity.*;
import com.bookingSystem.exception.*;
import com.bookingSystem.helper.PageResponse;
import com.bookingSystem.repository.ReservationRepository;
import com.bookingSystem.repository.ResourceRepository;
import com.bookingSystem.repository.UserRepository;
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
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

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

    @Test
    void addReservation_shouldCreateReservationSuccessfully(){
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .resourceId(205)
                .userId(9)
                .startDate(LocalDateTime.of(2026, 10, 1, 10,0))
                .endDate(LocalDateTime.of(2026, 10,3, 18,0))
                .build();

        Resource resource = Resource.builder()
                .id(205)
                .resourceName("Sony Alpha Camera")
                .price(98000.00)
                .build();

        User user = User.builder()
                .id(9)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        when(resourceRepository.findById(205))
                .thenReturn(Optional.of(resource));

        when(userRepository.findById(9))
                .thenReturn(Optional.of(user));

        when(authentication.getName())
                .thenReturn("rohit@gmail.com");

        when(authentication.getAuthorities())
                .then( (auth) -> List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("rohit@gmail.com"))
                .thenReturn(Optional.of(user));

        Reservation savedReservation = Reservation.builder()
                .id(118)
                .status(ReservationStatus.PENDING)
                .startDate(reservationRequest.getStartDate())
                .endDate(reservationRequest.getEndDate())
                .user(user)
                .resource(resource)
                .build();

        when(repository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        ReservationResponse response = this.reservationService.addReservation(reservationRequest);

        assertNotNull(response);
        assertEquals(118, response.getId());
        assertEquals(ReservationStatus.PENDING.toString(), response.getStatus());

        verify(userRepository).findById(9);
        verify(userRepository).findByEmail("rohit@gmail.com");
        verify(resourceRepository).findById(205);

        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        verify(repository).save(captor.capture());

        Reservation saved = captor.getValue();

        assertEquals(ReservationStatus.PENDING, saved.getStatus());
        assertEquals(user, saved.getUser());
        assertEquals(resource, saved.getResource());
        SecurityContextHolder.clearContext();
    }

    @Test
    void addReservation_shouldThrowException_whenUserCreatesReservationForAnotherUser(){
        ReservationRequest request = ReservationRequest.builder()
                .resourceId(205)
                .userId(10)
                .startDate(LocalDateTime.of(2026, 10, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 10, 3, 18, 0))
                .build();

        User existingUser = User.builder()
                .id(10)
                .userName("someone")
                .email("someone@gmail.com")
                .role(UserRole.USER)
                .build();

        User authenticatedUser = User.builder()
                .id(9)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(10))
                .thenReturn(Optional.of(existingUser));

        // Authentication operations
        when(authentication.getName())
                .thenReturn("rohit@gmail.com");
        when(authentication.getAuthorities())
                .then((auth)->
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ));
        when(userRepository.findByEmail("rohit@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        assertThrows(
                AuthorizationDeniedException.class,
                ()-> reservationService.addReservation(request)
        );

        verify(resourceRepository, never()).findById(any());
        verify(repository, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void addReservation_shouldCreateReservationWhenRoleIsAdmin(){
        ReservationRequest request = ReservationRequest.builder()
                .resourceId(205)
                .userId(9)
                .startDate(LocalDateTime.of(2026, 10, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 10, 3, 18, 0))
                .build();

        User existingUser = User.builder()
                .id(9)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        Resource existingResource = Resource.builder()
                .id(205)
                .resourceName("Sony Alpha Camera")
                .price(98000.00)
                .build();

        when(resourceRepository.findById(205))
                .thenReturn(Optional.of(existingResource));

        when(userRepository.findById(9))
                .thenReturn(Optional.of(existingUser));

        // ADMIN Authentication
        when(authentication.getName())
                .thenReturn("akash@gmail.com");
        when(authentication.getAuthorities())
                .then( (auth) -> List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                ));
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        Reservation savedReservation = Reservation.builder()
                .id(118)
                .status(ReservationStatus.PENDING)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .user(existingUser)
                .resource(existingResource)
                .build();

        when(repository.save(any(Reservation.class)))
                .thenReturn(savedReservation);


        // Execute
        ReservationResponse response =
                reservationService.addReservation(request);

        // Response assertions
        assertNotNull(response);
        assertEquals(118, response.getId());
        assertEquals(ReservationStatus.PENDING.toString(), response.getStatus());

        // Repository interactions verification
        verify(userRepository).findById(9);
        verify(resourceRepository).findById(205);
        verify(repository).save(any(Reservation.class));

        // ADMIN should not need to find himself by email
        verify(userRepository, never())
                .findByEmail(any());

        // Verify actual reservation passed to save
        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        verify(repository).save(captor.capture());

        Reservation saved = captor.getValue();

        assertEquals(ReservationStatus.PENDING, saved.getStatus());
        assertEquals(existingUser, saved.getUser());
        assertEquals(existingResource, saved.getResource());

        SecurityContextHolder.clearContext();
    }

    @Test
    void addReservation_shouldThrowException_WhenUserCreateReservationForAUserWhichDoesNotExist(){
        ReservationRequest request = ReservationRequest.builder()
                .resourceId(205)
                .userId(10)
                .startDate(LocalDateTime.of(2026, 10, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 10, 3, 18, 0))
                .build();

        when(authentication.getName())
                .thenReturn("rohit@gmail.com");
        when(authentication.getAuthorities())
                .then( (auth) -> List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        when(userRepository.findById(10))
                .thenReturn(Optional.empty());

        assertThrows(
                UserDoesNotExistException.class,
                ()-> reservationService.addReservation(request)
        );

        verify(userRepository).findById(10);
        verify(resourceRepository, never())
                .findById(any());
        verify((repository), never())
                .save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void addReservation_shouldThrowException_ResourceDoesNotExist(){
        ReservationRequest request = ReservationRequest.builder()
                .userId(10)
                .resourceId(205)
                .startDate(LocalDateTime.of(2026,10,3,10,0))
                .endDate(LocalDateTime.of(2026,12,5,16,0))
                .build();

        User user = User.builder()
                .id(10)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        User authenticatedUser = User.builder()
                .id(10)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        when(authentication.getName())
                .thenReturn("rohit@gmail.com");
        when(authentication.getAuthorities())
                .then( (auth) -> List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("rohit@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        when(userRepository.findById(10))
                .thenReturn(Optional.of(user));
        when(resourceRepository.findById(205))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceDoesNotExistException.class,
                ()-> reservationService.addReservation(request)
        );

        verify(resourceRepository).findById(205);
        verify(repository, never())
                .save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void addReservation_shouldThrowException_InvalidDateException(){
        ReservationRequest request = ReservationRequest.builder()
                .userId(10)
                .resourceId(205)
                .startDate(LocalDateTime.of(2026,12,6,10,0))
                .endDate(LocalDateTime.of(2026,12,5,16,0))
                .build();

        User user = User.builder()
                .id(10)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        when(authentication.getName())
                .thenReturn("rohit@gmail.com");
        when(authentication.getAuthorities())
                .then( (auth) -> List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        when(userRepository.findById(10))
                .thenReturn(Optional.of(user));

        assertThrows(
                InvalidDateException.class,
                ()-> reservationService.addReservation(request)
        );

        verify(userRepository).findById(10);
        verify(resourceRepository, never())
                .findById(any());
        verify(userRepository, never())
                .findByEmail(any());
        verify(repository, never())
                .save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void addReservation_shouldThrowException_AuthenticationCredentialsNotFoundException(){
        ReservationRequest request = ReservationRequest.builder()
                .userId(10)
                .resourceId(205)
                .startDate(LocalDateTime.of(2026,12,3,10,0))
                .endDate(LocalDateTime.of(2026,12,5,16,0))
                .build();

        authentication = null;

        SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> reservationService.addReservation(request)
        );
        verify(userRepository, never())
                .findByEmail(any());
        verify(userRepository, never())
                .findById(any());
        verify(resourceRepository, never())
                .findById(any());
        verify(repository, never())
                .save(any());

        SecurityContextHolder.clearContext();
    }


    @Test
    void updateReservation_shouldThrowException_whenUserIsNotAdmin(){
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AuthorizationDeniedException.class,
                ()-> reservationService.updateReservation(118, new ReservationRequest())
        );

        verify(userRepository, never())
                .findByEmail(any());
        verify(userRepository, never())
                .findById(any());
        verify(resourceRepository, never())
                .findById(any());
        verify(repository, never())
                .findById(any());
        verify(repository, never())
                .save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateReservation_shouldUpdateReservationSuccessfully(){
        Integer id = 118;
        ReservationRequest updateRequest = ReservationRequest.builder()
                .userId(10)
                .resourceId(205)
                .startDate(LocalDateTime.of(2026,12,3,10,0))
                .endDate(LocalDateTime.of(2026,12,5,16,0))
                .build();

        User existingUser = User.builder()
                .id(10)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        Resource existingResource = Resource.builder()
                .id(205)
                .resourceName("Sony Alpha Camera")
                .price(98000.00)
                .build();

        Reservation existingReservation = Reservation.builder()
                .id(118)
                .status(ReservationStatus.PENDING)
                .startDate(updateRequest.getStartDate())
                .endDate(updateRequest.getEndDate())
                .user(existingUser)
                .resource(existingResource)
                .build();

        Reservation updatedReservation = Reservation.builder()
                .id(118)
                .status(ReservationStatus.CONFIRMED)
                .startDate(updateRequest.getStartDate())
                .endDate(updateRequest.getEndDate())
                .user(existingUser)
                .resource(existingResource)
                .build();

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findById(id))
                .thenReturn(Optional.of(existingReservation));
        when(reservationService.getReservationListByResourceWithStatus(existingReservation.getResource().getId(), ReservationStatus.CONFIRMED.toString()))
                .thenReturn(Collections.emptyList());

        when(repository.save(any(Reservation.class)))
                .thenReturn(updatedReservation);

        ReservationResponse response = this.reservationService.updateReservation(id, updateRequest);

        assertNotNull(response);
        assertEquals(id, response.getId());

        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        verify(repository).findById(id);
        verify(repository).save(captor.capture());

        Reservation updated = captor.getValue();

        assertEquals(ReservationStatus.CONFIRMED, updated.getStatus());
        assertEquals(existingUser, updated.getUser());
        assertEquals(existingResource, updated.getResource());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateReservation_shouldThrowException_ReservationDoesNotExist(){
        Integer id = 1;
        ReservationRequest updateRequest = ReservationRequest.builder()
                .userId(10)
                .resourceId(205)
                .startDate(LocalDateTime.of(2026,12,3,10,0))
                .endDate(LocalDateTime.of(2026,12,5,16,0))
                .build();

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ReservationDoesNotExistException.class,
                ()-> reservationService.updateReservation(id, updateRequest)
        );

        verify(repository, never())
                .save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateReservation_shouldRejectReservationUpdate_whenStatusIsConfirmed(){
        Integer id = 1;
        ReservationRequest updateRequest = ReservationRequest.builder()
                .userId(10)
                .resourceId(205)
                .startDate(LocalDateTime.of(2026,12,3,10,0))
                .endDate(LocalDateTime.of(2026,12,5,16,0))
                .build();

        User existingUser = User.builder()
                .id(10)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        Resource existingResource = Resource.builder()
                .id(205)
                .resourceName("Sony Alpha Camera")
                .price(98000.00)
                .build();

        Reservation existingReservation = Reservation.builder()
                .id(1)
                .status(ReservationStatus.CONFIRMED)
                .startDate(updateRequest.getStartDate())
                .endDate(updateRequest.getEndDate())
                .user(existingUser)
                .resource(existingResource)
                .build();

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findById(1))
                .thenReturn(Optional.of(existingReservation));

        assertThrows(
                ReservationAlreadyConfirmedException.class,
                ()-> reservationService.updateReservation(id, updateRequest)
        );

        verify(repository, never())
                .save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateReservation_shouldRejectReservationUpdate_whenStatusIsCancelled(){
        Integer id = 1;
        ReservationRequest updateRequest = ReservationRequest.builder()
                .userId(10)
                .resourceId(205)
                .startDate(LocalDateTime.of(2026,12,3,10,0))
                .endDate(LocalDateTime.of(2026,12,5,16,0))
                .build();

        User existingUser = User.builder()
                .id(10)
                .userName("rohit")
                .email("rohit@gmail.com")
                .role(UserRole.USER)
                .build();

        Resource existingResource = Resource.builder()
                .id(205)
                .resourceName("Sony Alpha Camera")
                .price(98000.00)
                .build();

        Reservation existingReservation = Reservation.builder()
                .id(1)
                .status(ReservationStatus.CANCELLED)
                .startDate(updateRequest.getStartDate())
                .endDate(updateRequest.getEndDate())
                .user(existingUser)
                .resource(existingResource)
                .build();

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findById(1))
                .thenReturn(Optional.of(existingReservation));

        assertThrows(
                ReservationAlreadyCancelledException.class,
                ()-> reservationService.updateReservation(id, updateRequest)
        );

        verify(repository, never())
                .save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteReservation_shouldDeleteExistingReservation(){
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(repository.existsById(1))
                .thenReturn(true);
        reservationService.deleteReservation(1);

        verify(repository).existsById(1);
        verify(repository).deleteById(1);

        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteReservation_shouldThrowException_ReservationDoesNotExist(){
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(repository.existsById(1))
                .thenReturn(false);

        assertThrows(
                ReservationDoesNotExistException.class,
                ()-> reservationService.deleteReservation(1)
        );

        verify(repository).existsById(1);
        verify(repository, never())
                .deleteById(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByReservationId_shouldSuccessfullyGetReservationByIdToAdmin(){
        User user = User.builder().id(20).build();
        Resource resource = Resource.builder().id(30).build();

        Reservation reservation = Reservation.builder()
                .id(2)
                .status(ReservationStatus.PENDING)
                .user(user)
                .resource(resource)
                .build();

        // ADMIN authentication
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // Existing Reservation
        when(repository.findById(any()))
                .thenReturn(Optional.of(reservation));

        // Actual instruction of test
        ReservationResponse response =
                reservationService.getReservationByReservationId(2);
        assertNotNull(response);
        assertEquals(reservation.getId(), response.getId());
        assertEquals(reservation.getUser().getId(), response.getUser().getId());
        assertEquals(reservation.getStatus().toString(), response.getStatus());
        assertEquals(reservation.getResource().getId(), response.getResource().getId());

        // verify what should change and what should not
        verify(repository).findById(2);
        verify(userRepository, never()).findByEmail(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByReservationId_shouldSuccessfullyGetReservationByIdToOwnerUser(){
        User user = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .build();

        Reservation reservation = Reservation.builder()
                .id(2)
                .status(ReservationStatus.PENDING)
                .user(user)
                .resource(resource)
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findById(2))
                .thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        ReservationResponse response = reservationService.getReservationByReservationId(2);
        assertNotNull(response);
        assertEquals(reservation.getId(), response.getId());
        assertEquals(reservation.getUser().getId(), response.getUser().getId());
        assertEquals(reservation.getUser().getEmail(), response.getUser().getEmail());
        assertEquals(reservation.getResource().getId(), response.getResource().getId());

        verify(repository).findById(2);
        verify(userRepository).findByEmail("user@gmail.com");

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByReservationId_shouldThrowException_whenUserGetReservationOfAnotherUser(){
        User user = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user20@gmail.com")
                .build();

        User authenticatedUser = User.builder()
                .id(19)
                .role(UserRole.USER)
                .email("user19@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .build();

        Reservation reservation = Reservation.builder()
                .id(2)
                .status(ReservationStatus.PENDING)
                .user(user)
                .resource(resource)
                .build();

        when(authentication.getName())
                .thenReturn("user19@gmail.com");
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findById(2))
                .thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("user19@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                AuthorizationDeniedException.class,
                ()-> reservationService.getReservationByReservationId(2)
        );

        verify(repository).findById(2);
        verify(userRepository).findByEmail("user19@gmail.com");

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByReservationId_shouldThrowException_ReservationDoesNotExistException(){
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(repository.findById(100))
                .thenReturn(Optional.empty());

        assertThrows(
                ReservationDoesNotExistException.class,
                ()-> reservationService.getReservationByReservationId(100)
        );

        verify(repository).findById(100);
        verify(authentication, never()).getName();
        verify(userRepository, never()).findByEmail(any());

        SecurityContextHolder.clearContext();
    }

    // Edge case Defencive Check
    @Test
    void getReservationByReservationId_shouldThrowException_UserDoesNotExistException(){
        User user = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user20@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .build();

        Reservation reservation = Reservation.builder()
                .id(2)
                .status(ReservationStatus.PENDING)
                .user(user)
                .resource(resource)
                .build();

        when(authentication.getName())
                .thenReturn("user20@gmail.com");
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findById(2))
                .thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("user20@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserDoesNotExistException.class,
                ()-> reservationService.getReservationByReservationId(2)
        );

        verify(repository).findById(2);
        verify(userRepository).findByEmail("user20@gmail.com");

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByReservationId_shouldThrowException_AuthenticationCredentialsNotFoundException(){
        authentication = null;

        assertThrows(
                PreAuthenticatedCredentialsNotFoundException.class,
                ()-> reservationService.getReservationByReservationId(2)
        );

        verify(repository, never()).findById(2);
        verify(userRepository, never()).findByEmail(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservations_shouldSuccessfullyGetAllReservationsForAdmin(){
        User user = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user20@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(2000.0)
                .resourceName("iPhone")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(user)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(user)
                .resource(resource)
                .build();

        Pageable pageable = PageRequest.of(0,2);

        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
        when(repository.findAll(pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservations = reservationService.getAllReservations(pageable, null, null);
        assertNotNull(allReservations);
        assertEquals(reservationPage.getContent().getFirst().getStatus().toString(), allReservations.content().getFirst().getStatus());
        assertEquals(reservationPage.getContent().getLast().getStatus().toString(), allReservations.content().getLast().getStatus());

        verify(repository).findAll(pageable);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservations_shouldThrowException_AuthorizationDeniedException(){
        Pageable pageable = PageRequest.of(0, 2);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AuthorizationDeniedException.class,
                ()-> reservationService.getAllReservations(pageable, null, null)
        );

        verify(repository, never()).findAll(pageable);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservations_shouldGetAllReservations_whenMinPriceIsGiven(){
        User user = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user20@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(20000.0)
                .resourceName("iPhone")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(user)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(user)
                .resource(resource)
                .build();

        Double minPrice = 2000.0;
        Pageable pageable = PageRequest.of(0, 2);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findAllByMinPrice(pageable, minPrice))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservations = reservationService.getAllReservations(pageable, minPrice, null);
        assertNotNull(allReservations);
        assertEquals(reservationPage.getContent().getFirst().getUser().getId(), allReservations.content().getFirst().getUser().getId());
        assertEquals(reservationPage.getContent().getLast().getResource().getId(), allReservations.content().getLast().getResource().getId());

        verify(repository).findAllByMinPrice(pageable, minPrice);
        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(),any(),any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(pageable);

        SecurityContextHolder.clearContext();
    }


    @Test
    void getAllReservations_shouldGetAllReservations_whenMaxPriceIsGiven(){
        User user = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user20@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(2000.0)
                .resourceName("iPhone")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(user)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(user)
                .resource(resource)
                .build();

        Double maxPrice = 20000.0;
        Pageable pageable = PageRequest.of(0, 2);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findAllByMaxPrice(pageable, maxPrice))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservations = reservationService.getAllReservations(pageable, null, maxPrice);
        assertNotNull(allReservations);
        assertEquals(reservationPage.getContent().getFirst().getUser().getId(), allReservations.content().getFirst().getUser().getId());
        assertEquals(reservationPage.getContent().getLast().getResource().getId(), allReservations.content().getLast().getResource().getId());

        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository).findAllByMaxPrice(pageable, maxPrice);
        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(),any(),any());
        verify(repository, never()).findAll(pageable);

        SecurityContextHolder.clearContext();
    }


    @Test
    void getAllReservations_shouldGetAllReservations_whenMinPriceAndMaxPriceGiven(){
        User user = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user20@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(2000.0)
                .resourceName("iPhone")
                .build();

        Resource resource2 = Resource.builder()
                .id(40)
                .price(20000.0)
                .resourceName("Ninja")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(user)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(user)
                .resource(resource2)
                .build();

        Double minPrice = 1000.0;
        Double maxPrice = 30000.0;
        Pageable pageable = PageRequest.of(0, 2);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(repository.findAllByMaxPriceAndMinPrice(pageable,minPrice, maxPrice))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservations = reservationService.getAllReservations(pageable, minPrice, maxPrice);
        assertNotNull(allReservations);
        assertEquals(reservationPage.getContent().getFirst().getUser().getId(), allReservations.content().getFirst().getUser().getId());
        assertEquals(reservationPage.getContent().getLast().getResource().getId(), allReservations.content().getLast().getResource().getId());

        verify(repository).findAllByMaxPriceAndMinPrice(pageable, minPrice, maxPrice);
        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(pageable);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservations_shouldThrowException_IllegalArgumentException_whenMinPriceIsGreaterThanMaxPrice(){
        Double minPrice = 3000.0;
        Double maxPrice = 2000.0;

        Pageable pageable = Pageable.unpaged();

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservations(pageable, minPrice, maxPrice)
        );

        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(), any(), any());
        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(pageable);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservations_shouldThrowException_IllegalArgumentException_whenMinPriceIsNegative(){
        Double minPrice = -3000.0;
        Pageable pageable = Pageable.unpaged();

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservations(pageable, minPrice, null)
        );

        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(), any(), any());
        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(pageable);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservations_shouldThrowException_IllegalArgumentException_whenMaxPriceIsNegative(){
        Double maxPrice = -2000.0;
        Pageable pageable = Pageable.unpaged();

        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservations(pageable, null, maxPrice)
        );

        verify(repository, never()).findAllByMaxPriceAndMinPrice(any(), any(), any());
        verify(repository, never()).findAllByMinPrice(any(), any());
        verify(repository, never()).findAllByMaxPrice(any(), any());
        verify(repository, never()).findAll(pageable);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldReturnUserReservations(){
        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(2000.0)
                .resourceName("iPhone")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(authenticatedUser)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(authenticatedUser)
                .resource(resource)
                .build();

        Pageable pageable = PageRequest.of(0, 2);

        Page<Reservation> reservationPage = new PageImpl<>(
                List.of(reservation1, reservation2),
                pageable,
                2
        );
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        when(repository.findAllByUserId(20, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserId = reservationService.getAllReservationsByUserId(20, null, null, pageable);
        assertNotNull(allReservationsByUserId);
        assertEquals(2, allReservationsByUserId.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserId(20, pageable);

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldReturnUserReservations_withMinPrice(){
        Double minPrice = 100.0;
        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(2000.0)
                .resourceName("iPhone")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(authenticatedUser)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(authenticatedUser)
                .resource(resource)
                .build();

        Pageable pageable = PageRequest.of(0, 2);

        Page<Reservation> reservationPage = new PageImpl<>(
                List.of(reservation1, reservation2),
                pageable,
                2
        );
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        when(repository.findAllByUserIdWithMinPrice(20, minPrice, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserIdWithMinPrice = reservationService.getAllReservationsByUserId(20, minPrice, null, pageable);
        assertNotNull(allReservationsByUserIdWithMinPrice);
        assertEquals(2, allReservationsByUserIdWithMinPrice.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithMinPrice(20, minPrice, pageable);
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldReturnUserReservations_withMaxPrice(){
        Double maxPrice = 100000.0;
        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(2000.0)
                .resourceName("iPhone")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(authenticatedUser)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(authenticatedUser)
                .resource(resource)
                .build();

        Pageable pageable = PageRequest.of(0, 2);

        Page<Reservation> reservationPage = new PageImpl<>(
                List.of(reservation1, reservation2),
                pageable,
                2
        );
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        when(repository.findAllByUserIdWithMaxPrice(20, maxPrice, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserIdWithMinPrice = reservationService.getAllReservationsByUserId(20, null, maxPrice, pageable);
        assertNotNull(allReservationsByUserIdWithMinPrice);
        assertEquals(2, allReservationsByUserIdWithMinPrice.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithMaxPrice(20, maxPrice, pageable);
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldReturnUserReservations_withMaxPriceAndMinPrice(){
        Double minPrice = 100.0;
        Double maxPrice = 10000.0;
        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(30)
                .price(2000.0)
                .resourceName("iPhone")
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1)
                .user(authenticatedUser)
                .status(ReservationStatus.PENDING)
                .resource(resource)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2)
                .status(ReservationStatus.CONFIRMED)
                .user(authenticatedUser)
                .resource(resource)
                .build();

        Pageable pageable = PageRequest.of(0, 2);

        Page<Reservation> reservationPage = new PageImpl<>(
                List.of(reservation1, reservation2),
                pageable,
                2
        );
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        when(repository.findAllByUserIdWithMinPriceAndMaxPrice(20, minPrice, maxPrice, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserIdWithMinPrice = reservationService.getAllReservationsByUserId(20, minPrice, maxPrice, pageable);
        assertNotNull(allReservationsByUserIdWithMinPrice);
        assertEquals(2, allReservationsByUserIdWithMinPrice.content().size());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithMinPriceAndMaxPrice(20, minPrice, maxPrice, pageable);
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldThrowException_whenMinPriceGreaterThanMaxPrice(){
        Double minPrice = 20000.0;
        Double maxPrice = 10000.0;
        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservationsByUserId(20, minPrice, maxPrice, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldThrowException_whenMinPriceIsNegative(){
        Double minPrice = -20000.0;
        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservationsByUserId(20, minPrice, null, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());

        SecurityContextHolder.clearContext();
    }


    @Test
    void getReservationByUserId_shouldThrowException_whenMaxPriceIsNegative(){
        Double maxPrice = -10000.0;
        User authenticatedUser = User.builder()
                .id(20)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservationsByUserId(20, null, maxPrice, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository, never()).findAllByUserIdWithMinPriceAndMaxPrice(any(), any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMinPrice(any(), any(), any());
        verify(repository, never()).findAllByUserIdWithMaxPrice(any(), any(), any());
        verify(repository, never()).findAllByUserId(any(), any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldThrowException_AuthenticationCredentialsNotFoundException(){
        authentication = null;
        SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> reservationService.getAllReservationsByUserId(1, null, null, Pageable.unpaged())
        );

        SecurityContextHolder.clearContext();
    }

    @Test
    void getReservationByUserId_shouldThrowException_UserDoesNotExistException(){
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.empty());
        assertThrows(
                UserDoesNotExistException.class,
                ()-> reservationService.getAllReservationsByUserId(1, null, null, Pageable.unpaged())
        );

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldGetAllUserReservationsByUserWithStatus(){
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(5)
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(3)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(4)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserWithStatus(2, ReservationStatus.PENDING, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserWithStatus = reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), null, null, pageable);
        assertNotNull(allReservationsByUserWithStatus);
        assertEquals(reservationPage.getContent().getFirst().getUser().getId(), allReservationsByUserWithStatus.content().getFirst().getUser().getId());
        assertEquals(reservationPage.getContent().getLast().getResource().getId(), allReservationsByUserWithStatus.content().getLast().getResource().getId());
        assertEquals(reservationPage.getContent().getFirst().getStatus().toString(), allReservationsByUserWithStatus.content().getFirst().getStatus());


        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserWithStatus(2, ReservationStatus.PENDING, pageable);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldReturnAllUserReservations_ByStatusWithMaxPriceAndMinPrice(){
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Double minPrice = 1000.0;
        Double maxPrice = 20000.0;

        Resource resource = Resource.builder()
                .id(5)
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(3)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(4)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);


        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithStatusBetweenMinPriceAndMaxPrice(2, ReservationStatus.PENDING, minPrice, maxPrice, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserWithStatus = reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), minPrice, maxPrice, pageable);
        assertNotNull(allReservationsByUserWithStatus);
        assertEquals(reservationPage.getContent().getFirst().getUser().getId(), allReservationsByUserWithStatus.content().getFirst().getUser().getId());
        assertEquals(reservationPage.getContent().getLast().getResource().getId(), allReservationsByUserWithStatus.content().getLast().getResource().getId());
        assertEquals(reservationPage.getContent().getFirst().getStatus().toString(), allReservationsByUserWithStatus.content().getFirst().getStatus());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithStatusBetweenMinPriceAndMaxPrice(2, ReservationStatus.PENDING, minPrice, maxPrice, pageable);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldReturnAllUserReservations_ByStatusWithMinPrice(){
        Double minPrice = 1000.0;
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(5)
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(3)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(4)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithStatusAndMinPrice(2, ReservationStatus.PENDING, minPrice, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserWithStatus = reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), minPrice, null, pageable);
        assertNotNull(allReservationsByUserWithStatus);
        assertEquals(reservationPage.getContent().getFirst().getUser().getId(), allReservationsByUserWithStatus.content().getFirst().getUser().getId());
        assertEquals(reservationPage.getContent().getLast().getResource().getId(), allReservationsByUserWithStatus.content().getLast().getResource().getId());
        assertEquals(reservationPage.getContent().getFirst().getStatus().toString(), allReservationsByUserWithStatus.content().getFirst().getStatus());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithStatusAndMinPrice(2, ReservationStatus.PENDING, minPrice, pageable);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldReturnAllUserReservations_ByStatusWithMaxPrice(){
        Double maxPrice = 1000.0;
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        Resource resource = Resource.builder()
                .id(5)
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(3)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(4)
                .user(authenticatedUser)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(repository.findAllByUserIdWithStatusAndMaxPrice(2, ReservationStatus.PENDING, maxPrice, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByUserWithStatus = reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), null, maxPrice, pageable);
        assertNotNull(allReservationsByUserWithStatus);
        assertEquals(reservationPage.getContent().getFirst().getUser().getId(), allReservationsByUserWithStatus.content().getFirst().getUser().getId());
        assertEquals(reservationPage.getContent().getLast().getResource().getId(), allReservationsByUserWithStatus.content().getLast().getResource().getId());
        assertEquals(reservationPage.getContent().getFirst().getStatus().toString(), allReservationsByUserWithStatus.content().getFirst().getStatus());

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        verify(repository).findAllByUserIdWithStatusAndMaxPrice(2, ReservationStatus.PENDING, maxPrice, pageable);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenMinPriceGreaterThanMaxPrice(){
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), 10000.0, 2000.0, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenMinPriceIsNegative(){
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), -10000.0, null, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenMaxPriceIsNegative(){
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                IllegalArgumentException.class,
                ()-> reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.PENDING.name(), null, -200.0, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_AuthorizationDeniedException(){
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                AuthorizationDeniedException.class,
                ()-> reservationService.getAllReservationsByUserWithStatus(3, ReservationStatus.PENDING.name(), 200.0, 400.0, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_InvalidStatusException(){
        User authenticatedUser = User.builder()
                .id(2)
                .role(UserRole.USER)
                .email("user@gmail.com")
                .build();

        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        assertThrows(
                InvalidStatusException.class,
                ()-> reservationService.getAllReservationsByUserWithStatus(2, "COMPLETED", 200.0, 400.0, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_UserDoesNotExistException(){
        when(authentication.getName())
                .thenReturn("user@gmail.com");
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserDoesNotExistException.class,
                ()-> reservationService.getAllReservationsByUserWithStatus(2, ReservationStatus.CONFIRMED.name(), 200.0, 400.0, Pageable.unpaged())
        );

        verify(authentication).getName();
        verify(userRepository).findByEmail("user@gmail.com");
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByUserWithStatus_shouldThrowException_whenUserIsNotAuthenticated(){
        authentication = null;
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                ()-> reservationService.getAllReservationsByUserWithStatus(2, "CONFIRMED", null, null, Pageable.unpaged())
        );
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReservationsByResource_shouldReturnAllReservationsByResource(){
        Pageable pageable = PageRequest.of(0, 2);

        User user = User.builder()
                .id(1)
                .build();

        Resource resource = Resource.builder()
                .id(2)
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(3)
                .user(user)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(4)
                .user(user)
                .resource(resource)
                .status(ReservationStatus.PENDING)
                .build();

        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation1, reservation2), pageable, 2);
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        when(authentication.getAuthorities())
                .then(
                        (auth)-> List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );
        when(repository.findAllByResourceId(2, pageable))
                .thenReturn(reservationPage);

        PageResponse<ReservationResponse> allReservationsByResourceId = reservationService.getAllReservationsByResourceId(2, null, null, pageable);
        assertNotNull(allReservationsByResourceId);
        assertEquals(reservationPage.getContent().getFirst().getResource().getId(), allReservationsByResourceId.content().getFirst().getResource().getId());

        verify(repository).findAllByResourceId(2,pageable);
        SecurityContextHolder.clearContext();
    }
}



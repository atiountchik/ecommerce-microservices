package com.ecommerce.seller.service;

import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.exceptions.*;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.request.RegisterSellerRequestDTO;
import com.ecommerce.sdk.request.SellerDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.seller.repository.dbo.SellerDBO;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.UnknownHostException;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SellerService {

    private SellerRepository sellerRepository;
    private KeycloakService keycloakService;

    @Autowired
    public SellerService(KeycloakService keycloakService, SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
        this.keycloakService = keycloakService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws HttpResponseException, AuthorizationDeniedException, UnknownHostException {
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();
        String token = this.keycloakService.obtainToken(email, password);
        return new LoginResponseDTO(token);
    }

    @Transactional
    public void register(RegisterSellerRequestDTO inputData) throws InvalidInputException, UserConflictException, DeleteUserException, UnknownHostException, WorkingHoursException {
        if (inputData == null) {
            throw new InvalidInputException("seller.api.register.error.nullInput");
        }

        String email = inputData.getEmail();
        String password = inputData.getPassword();
        UUID userId = this.keycloakService.register(email, password);

        if (this.sellerRepository.findByAuthId(userId) != null) {
            this.keycloakService.deleteUser(userId);
            throw new UserConflictException("seller.api.register.error.sellerInformationAlreadyPresent");
        }

        OffsetTime opensAt = OffsetTime.parse(inputData.getOpensAt());
        OffsetTime closesAt = OffsetTime.parse(inputData.getClosesAt());

        if (opensAt.isAfter(closesAt)) {
            throw new WorkingHoursException("seller.api.update.error.openingHourIsAfterClosing");
        }

        ZonedDateTime now = ZonedDateTime.now();
        SellerDBO entity = new SellerDBO(0l, now, now, inputData.getName(), inputData.getLatitude(), inputData.getLongitude(), inputData.getCountry(), inputData.getCountryVatNumber(), opensAt, closesAt, userId);

        this.sellerRepository.save(entity);
    }

    public SellerDBO getProfile(Authentication authentication) {
        String userId = authentication.getName();
        return this.sellerRepository.findByAuthId(UUID.fromString(userId));
    }

    private boolean isSellerAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.SELLER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }

    private UUID getSellerId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    @Transactional
    public SellerDBO updateProfile(Authentication authentication, SellerDTO sellerDto) throws UserDoesNotExistException, AccessDeniedException, WorkingHoursException {
        UUID sellerId = getSellerId(authentication);
        SellerDBO fetchedSellerDBO = this.sellerRepository.findById(sellerDto.getId()).orElse(null);
        if (fetchedSellerDBO == null) {
            throw new UserDoesNotExistException("seller.api.update.error.idNotFound");
        }
        if (!isSellerAdmin(authentication) && !fetchedSellerDBO.getAuthId().equals(sellerId)) {
            throw new AccessDeniedException("seller.api.update.error.forbidden");
        }
        fetchedSellerDBO.setCountry(sellerDto.getCountry().name());
        fetchedSellerDBO.setLastUpdateDate(ZonedDateTime.now());
        fetchedSellerDBO.setLatitude(sellerDto.getLatitude());
        fetchedSellerDBO.setLongitude(sellerDto.getLongitude());
        fetchedSellerDBO.setName(sellerDto.getName());
        OffsetTime opensAt = OffsetTime.parse(sellerDto.getOpensAt());
        OffsetTime closesAt = OffsetTime.parse(sellerDto.getClosesAt());
        if (opensAt.isAfter(closesAt)) {
            throw new WorkingHoursException("seller.api.update.error.openingHourIsAfterClosing");
        }
        fetchedSellerDBO.setOpensAt(opensAt);
        fetchedSellerDBO.setClosesAt(closesAt);
        fetchedSellerDBO.setName(sellerDto.getName());
        return this.sellerRepository.save(fetchedSellerDBO);
    }


    public void deleteProfile(Authentication authentication, UUID sellerIdToDelete) throws UserDoesNotExistException, AccessDeniedException, UnknownHostException, DeleteUserException {
        SellerDBO fetchedSellerDBO = checkPermissionsAndFetchExistingUser(authentication, sellerIdToDelete);
        this.keycloakService.deleteUser(sellerIdToDelete);
        this.sellerRepository.delete(fetchedSellerDBO);
    }

    private SellerDBO checkPermissionsAndFetchExistingUser(Authentication authentication, UUID sellerIdToFetch) throws UserDoesNotExistException {
        UUID sellerId = getSellerId(authentication);
        SellerDBO fetchedSellerDBO = this.sellerRepository.findByAuthId(sellerIdToFetch);
        if (fetchedSellerDBO == null) {
            throw new UserDoesNotExistException("seller.api.delete.error.idNotFound");
        }
        if (!isSellerAdmin(authentication) && !fetchedSellerDBO.getAuthId().equals(sellerId)) {
            throw new AccessDeniedException("seller.api.delete.error.forbidden");
        }
        return fetchedSellerDBO;
    }

    public SellerDBO getProfile(Authentication authentication, UUID sellerId) throws UserDoesNotExistException {
        return checkPermissionsAndFetchExistingUser(authentication, sellerId);
    }
}

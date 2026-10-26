package com.factory.client.service;

import com.factory.client.dto.*;
import com.factory.client.entity.Client;
import com.factory.client.entity.ClientAddress;
import com.factory.client.entity.ClientStatus;
import com.factory.client.repository.ClientAddressRepository;
import com.factory.client.repository.ClientRepository;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientAddressRepository clientAddressRepository;

    public ClientService(ClientRepository clientRepository, ClientAddressRepository clientAddressRepository) {
        this.clientRepository = clientRepository;
        this.clientAddressRepository = clientAddressRepository;
    }

    public ClientResponseDto createClient(ClientCreateDto dto, String currentUserId) {
        String normalizedGstin = normalizeString(dto.getGstin());
        if (normalizedGstin != null && clientRepository.existsByGstin(normalizedGstin)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GSTIN,
                    "A client with GSTIN '" + normalizedGstin + "' is already registered in the system.");
        }

        Client client = new Client();
        client.setCompanyName(dto.getCompanyName().trim());
        if (dto.getClientType() != null) {
            client.setClientType(dto.getClientType());
        }
        client.setGstin(normalizedGstin);
        client.setPan(normalizeString(dto.getPan()));
        client.setState(dto.getState().trim());
        client.setPrimaryContactName(dto.getPrimaryContactName().trim());
        client.setPrimaryContactDesignation(dto.getPrimaryContactDesignation());
        client.setPrimaryContactPhone(dto.getPrimaryContactPhone().trim());
        client.setPrimaryContactEmail(dto.getPrimaryContactEmail().trim());

        client.setBillingContactName(dto.getBillingContactName());
        client.setBillingContactDesignation(dto.getBillingContactDesignation());
        client.setBillingContactPhone(dto.getBillingContactPhone());
        client.setBillingContactEmail(dto.getBillingContactEmail());

        client.setCreditDays(dto.getCreditDays());
        client.setStatus(ClientStatus.ACTIVE);
        client.setCreatedBy(currentUserId);

        if (dto.getInitialAddress() != null) {
            ClientAddress address = new ClientAddress();
            mapAddressDtoToEntity(dto.getInitialAddress(), address);
            address.setDefault(true);
            client.addAddress(address);
        }

        Client saved = clientRepository.save(client);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getClientById(String id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + id));
        return mapToResponseDto(client);
    }

    @Transactional(readOnly = true)
    public PageResponse<ClientResponseDto> searchClients(String query, String state, ClientStatus status, Pageable pageable) {
        Page<Client> page = clientRepository.searchClients(query, state, status, pageable);
        List<ClientResponseDto> content = page.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    public ClientResponseDto updateClient(String id, ClientUpdateDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + id));

        String normalizedGstin = normalizeString(dto.getGstin());
        if (normalizedGstin != null && clientRepository.existsByGstinAndIdNot(normalizedGstin, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GSTIN,
                    "Another client with GSTIN '" + normalizedGstin + "' already exists.");
        }

        client.setCompanyName(dto.getCompanyName().trim());
        if (dto.getClientType() != null) {
            client.setClientType(dto.getClientType());
        }
        client.setGstin(normalizedGstin);
        client.setPan(normalizeString(dto.getPan()));
        client.setState(dto.getState().trim());
        client.setPrimaryContactName(dto.getPrimaryContactName().trim());
        client.setPrimaryContactDesignation(dto.getPrimaryContactDesignation());
        client.setPrimaryContactPhone(dto.getPrimaryContactPhone().trim());
        client.setPrimaryContactEmail(dto.getPrimaryContactEmail().trim());

        client.setBillingContactName(dto.getBillingContactName());
        client.setBillingContactDesignation(dto.getBillingContactDesignation());
        client.setBillingContactPhone(dto.getBillingContactPhone());
        client.setBillingContactEmail(dto.getBillingContactEmail());
        client.setCreditDays(dto.getCreditDays());

        Client updated = clientRepository.save(client);
        return mapToResponseDto(updated);
    }

    public ClientResponseDto updateClientStatus(String id, ClientStatus newStatus) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + id));

        client.setStatus(newStatus);
        Client updated = clientRepository.save(client);
        return mapToResponseDto(updated);
    }

    public ClientResponseDto addDeliveryAddress(String clientId, ClientAddressDto addressDto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + clientId));

        ClientAddress address = new ClientAddress();
        mapAddressDtoToEntity(addressDto, address);

        if (client.getAddresses().isEmpty() || addressDto.isDefault()) {
            client.getAddresses().forEach(a -> a.setDefault(false));
            address.setDefault(true);
        }

        client.addAddress(address);
        Client saved = clientRepository.save(client);
        return mapToResponseDto(saved);
    }

    public ClientResponseDto setDefaultDeliveryAddress(String clientId, String addressId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + clientId));

        boolean found = false;
        for (ClientAddress addr : client.getAddresses()) {
            if (addr.getId().equals(addressId)) {
                addr.setDefault(true);
                found = true;
            } else {
                addr.setDefault(false);
            }
        }

        if (!found) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Delivery address not found with id: " + addressId);
        }

        Client saved = clientRepository.save(client);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public ClientSnapshotDto generateClientSnapshot(String clientId, String deliveryAddressId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + clientId));

        ClientAddress deliveryAddress = null;
        if (deliveryAddressId != null) {
            deliveryAddress = client.getAddresses().stream()
                    .filter(a -> a.getId().equals(deliveryAddressId))
                    .findFirst()
                    .orElse(null);
        }

        if (deliveryAddress == null) {
            deliveryAddress = client.getAddresses().stream()
                    .filter(ClientAddress::isDefault)
                    .findFirst()
                    .orElse(client.getAddresses().isEmpty() ? null : client.getAddresses().get(0));
        }

        String deliveryAddressLine = deliveryAddress != null
                ? String.format("%s, %s, %s, %s - %s", deliveryAddress.getSiteName(), deliveryAddress.getAddressLine1(),
                deliveryAddress.getCity(), deliveryAddress.getState(), deliveryAddress.getPincode())
                : client.getState();

        String billingAddressLine = String.format("%s, %s (Contact: %s, Phone: %s)",
                client.getCompanyName(), client.getState(),
                client.getBillingContactName() != null ? client.getBillingContactName() : client.getPrimaryContactName(),
                client.getBillingContactPhone() != null ? client.getBillingContactPhone() : client.getPrimaryContactPhone());

        return new ClientSnapshotDto(
                client.getId(),
                client.getCompanyName(),
                client.getGstin(),
                client.getPan(),
                client.getState(),
                client.getPrimaryContactName(),
                client.getPrimaryContactPhone(),
                client.getPrimaryContactEmail(),
                billingAddressLine,
                deliveryAddressLine,
                client.getCreditDays()
        );
    }

    private String normalizeString(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        return val.trim().toUpperCase();
    }

    private void mapAddressDtoToEntity(ClientAddressDto dto, ClientAddress entity) {
        entity.setSiteName(dto.getSiteName().trim());
        entity.setAddressLine1(dto.getAddressLine1().trim());
        entity.setCity(dto.getCity().trim());
        entity.setState(dto.getState().trim());
        entity.setPincode(dto.getPincode().trim());
        entity.setContactPerson(dto.getContactPerson());
        entity.setPhone(dto.getPhone());
    }

    private ClientAddressDto mapAddressEntityToDto(ClientAddress entity) {
        return new ClientAddressDto(
                entity.getId(),
                entity.getSiteName(),
                entity.getAddressLine1(),
                entity.getCity(),
                entity.getState(),
                entity.getPincode(),
                entity.getContactPerson(),
                entity.getPhone(),
                entity.isDefault()
        );
    }

    public ClientResponseDto mapToResponseDto(Client client) {
        ClientResponseDto dto = new ClientResponseDto();
        dto.setId(client.getId());
        dto.setCompanyName(client.getCompanyName());
        dto.setClientType(client.getClientType());
        dto.setGstin(client.getGstin());
        dto.setPan(client.getPan());
        dto.setState(client.getState());

        dto.setPrimaryContact(new ClientResponseDto.ContactDto(
                client.getPrimaryContactName(),
                client.getPrimaryContactDesignation(),
                client.getPrimaryContactPhone(),
                client.getPrimaryContactEmail()
        ));

        if (client.getBillingContactName() != null || client.getBillingContactEmail() != null) {
            dto.setBillingContact(new ClientResponseDto.ContactDto(
                    client.getBillingContactName(),
                    client.getBillingContactDesignation(),
                    client.getBillingContactPhone(),
                    client.getBillingContactEmail()
            ));
        }

        dto.setCreditDays(client.getCreditDays());
        dto.setStatus(client.getStatus());
        dto.setCreatedBy(client.getCreatedBy());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());

        if (client.getAddresses() != null) {
            dto.setDeliveryAddresses(client.getAddresses().stream()
                    .map(this::mapAddressEntityToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}

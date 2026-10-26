package com.factory.client;

import com.factory.client.dto.*;
import com.factory.client.entity.ClientStatus;
import com.factory.client.entity.ClientType;
import com.factory.client.service.ClientService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ClientServiceIntegrationTests {

    @Autowired
    private ClientService clientService;

    @Test
    @DisplayName("Create client with valid GSTIN and initial delivery address succeeds")
    void testCreateClient_Success() {
        ClientCreateDto createDto = new ClientCreateDto();
        createDto.setCompanyName("Mahyco Private Limited");
        createDto.setClientType(ClientType.SEED_PRODUCER);
        createDto.setGstin("27AABCM1234F1Z5");
        createDto.setPan("AABCM1234F");
        createDto.setState("Maharashtra");
        createDto.setPrimaryContactName("Rajesh Patil");
        createDto.setPrimaryContactDesignation("Senior Procurement Head");
        createDto.setPrimaryContactPhone("+91 98220 11223");
        createDto.setPrimaryContactEmail("rajesh.patil@mahyco.com");
        createDto.setCreditDays(45);

        ClientAddressDto addressDto = new ClientAddressDto();
        addressDto.setSiteName("Jalna Seed Processing Unit #1");
        addressDto.setAddressLine1("Plot B-12, Additional MIDC Phase 2");
        addressDto.setCity("Jalna");
        addressDto.setState("Maharashtra");
        addressDto.setPincode("431213");
        addressDto.setContactPerson("Suresh Kulkarni");
        addressDto.setPhone("+91 94221 44556");
        createDto.setInitialAddress(addressDto);

        ClientResponseDto response = clientService.createClient(createDto, "user-test-owner");

        assertNotNull(response.getId());
        assertEquals("Mahyco Private Limited", response.getCompanyName());
        assertEquals("27AABCM1234F1Z5", response.getGstin());
        assertEquals("AABCM1234F", response.getPan());
        assertEquals(ClientStatus.ACTIVE, response.getStatus());
        assertEquals(45, response.getCreditDays());
        assertEquals(1, response.getDeliveryAddresses().size());
        assertTrue(response.getDeliveryAddresses().get(0).isDefault());
        assertEquals("Jalna Seed Processing Unit #1", response.getDeliveryAddresses().get(0).getSiteName());
    }

    @Test
    @DisplayName("Create client rejects duplicate GSTIN with ErrorCode.DUPLICATE_GSTIN")
    void testCreateClient_DuplicateGstin_ThrowsConflict() {
        ClientCreateDto dto1 = new ClientCreateDto();
        dto1.setCompanyName("Kaveri Seed Company Ltd");
        dto1.setGstin("36AABCK5555F1Z2");
        dto1.setState("Telangana");
        dto1.setPrimaryContactName("Venkatesh Rao");
        dto1.setPrimaryContactPhone("+91 98490 33445");
        dto1.setPrimaryContactEmail("venkatesh@kaveri.com");

        clientService.createClient(dto1, "user-test");

        ClientCreateDto dto2 = new ClientCreateDto();
        dto2.setCompanyName("Kaveri Subsidiary");
        dto2.setGstin("  36aabck5555f1z2  "); // Lowercase and padded whitespace
        dto2.setState("Telangana");
        dto2.setPrimaryContactName("Venkatesh Rao");
        dto2.setPrimaryContactPhone("+91 98490 33445");
        dto2.setPrimaryContactEmail("venkatesh.sub@kaveri.com");

        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.createClient(dto2, "user-test"));
        assertEquals(ErrorCode.DUPLICATE_GSTIN, ex.getErrorCode());
    }

    @Test
    @DisplayName("Search clients with query and state filtering")
    void testSearchClients_WithFiltersAndPagination() {
        ClientCreateDto client1 = new ClientCreateDto();
        client1.setCompanyName("Nuziveedu Seeds Limited");
        client1.setGstin("36AABCN1111F1Z8");
        client1.setState("Telangana");
        client1.setPrimaryContactName("Srinivas Reddy");
        client1.setPrimaryContactPhone("+91 98480 12345");
        client1.setPrimaryContactEmail("srinivas@nuziveedu.com");
        clientService.createClient(client1, "user-test");

        ClientCreateDto client2 = new ClientCreateDto();
        client2.setCompanyName("Rasi Seeds Private Ltd");
        client2.setGstin("33AABCR2222F1Z1");
        client2.setState("Tamil Nadu");
        client2.setPrimaryContactName("Ramasamy");
        client2.setPrimaryContactPhone("+91 94430 54321");
        client2.setPrimaryContactEmail("ramasamy@rasi.com");
        clientService.createClient(client2, "user-test");

        // Filter by state Telangana
        PageResponse<ClientResponseDto> telanganaClients = clientService.searchClients(
                null, "Telangana", ClientStatus.ACTIVE, PageRequest.of(0, 10));
        assertTrue(telanganaClients.getItems().stream().anyMatch(c -> c.getCompanyName().contains("Nuziveedu")));
        assertFalse(telanganaClients.getItems().stream().anyMatch(c -> c.getCompanyName().contains("Rasi")));

        // Search by query
        PageResponse<ClientResponseDto> queryResult = clientService.searchClients(
                "Rasi", null, null, PageRequest.of(0, 10));
        assertEquals(1, queryResult.getItems().size());
        assertEquals("Rasi Seeds Private Ltd", queryResult.getItems().get(0).getCompanyName());
    }

    @Test
    @DisplayName("Add secondary delivery address and switch default delivery site")
    void testAddDeliveryAddress_AndSetDefault() {
        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("Advanta Seeds Holdings");
        clientDto.setGstin("27AABCA9999F1Z0");
        clientDto.setState("Maharashtra");
        clientDto.setPrimaryContactName("Anand Joshi");
        clientDto.setPrimaryContactPhone("+91 98230 99887");
        clientDto.setPrimaryContactEmail("anand@advanta.com");

        ClientAddressDto addr1 = new ClientAddressDto();
        addr1.setSiteName("Aurangabad Depot");
        addr1.setAddressLine1("MIDC Waluj Sector 4");
        addr1.setCity("Aurangabad");
        addr1.setState("Maharashtra");
        addr1.setPincode("431136");
        clientDto.setInitialAddress(addr1);

        ClientResponseDto created = clientService.createClient(clientDto, "user-test");
        String clientId = created.getId();
        String addr1Id = created.getDeliveryAddresses().get(0).getId();

        // Add second site
        ClientAddressDto addr2 = new ClientAddressDto();
        addr2.setSiteName("Hyderabad Logistics Hub");
        addr2.setAddressLine1("Kukatpally Industrial Area");
        addr2.setCity("Hyderabad");
        addr2.setState("Telangana");
        addr2.setPincode("500072");
        addr2.setDefault(false);

        ClientResponseDto withSecondAddr = clientService.addDeliveryAddress(clientId, addr2);
        assertEquals(2, withSecondAddr.getDeliveryAddresses().size());

        String addr2Id = withSecondAddr.getDeliveryAddresses().stream()
                .filter(a -> a.getSiteName().equals("Hyderabad Logistics Hub"))
                .findFirst()
                .orElseThrow()
                .getId();

        // Switch default to Hyderabad
        ClientResponseDto updatedDefault = clientService.setDefaultDeliveryAddress(clientId, addr2Id);
        ClientAddressDto updatedAddr1 = updatedDefault.getDeliveryAddresses().stream()
                .filter(a -> a.getId().equals(addr1Id)).findFirst().orElseThrow();
        ClientAddressDto updatedAddr2 = updatedDefault.getDeliveryAddresses().stream()
                .filter(a -> a.getId().equals(addr2Id)).findFirst().orElseThrow();

        assertFalse(updatedAddr1.isDefault());
        assertTrue(updatedAddr2.isDefault());
    }

    @Test
    @DisplayName("Generate immutable client snapshot for purchase orders")
    void testGenerateClientSnapshot() {
        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("Syngenta India Limited");
        clientDto.setGstin("27AABCS7777F1Z3");
        clientDto.setPan("AABCS7777F");
        clientDto.setState("Maharashtra");
        clientDto.setPrimaryContactName("Vikas Sharma");
        clientDto.setPrimaryContactPhone("+91 99220 88776");
        clientDto.setPrimaryContactEmail("vikas.sharma@syngenta.com");
        clientDto.setCreditDays(60);

        ClientAddressDto addr = new ClientAddressDto();
        addr.setSiteName("Pune R&D Facility");
        addr.setAddressLine1("Amar Paradigm, Baner Road");
        addr.setCity("Pune");
        addr.setState("Maharashtra");
        addr.setPincode("411045");
        clientDto.setInitialAddress(addr);

        ClientResponseDto created = clientService.createClient(clientDto, "user-test");

        ClientSnapshotDto snapshot = clientService.generateClientSnapshot(created.getId(), null);
        assertNotNull(snapshot);
        assertEquals(created.getId(), snapshot.getClientId());
        assertEquals("Syngenta India Limited", snapshot.getCompanyName());
        assertEquals("27AABCS7777F1Z3", snapshot.getGstin());
        assertEquals("AABCS7777F", snapshot.getPan());
        assertEquals(60, snapshot.getCreditDays());
        assertTrue(snapshot.getDeliveryAddressLine().contains("Pune R&D Facility"));
    }

    @Test
    @DisplayName("Soft toggle client status between ACTIVE and INACTIVE")
    void testClientStatusToggle() {
        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("JK Agri Genetics Ltd");
        clientDto.setGstin("36AABCJ4444F1Z7");
        clientDto.setState("Telangana");
        clientDto.setPrimaryContactName("Murali Mohan");
        clientDto.setPrimaryContactPhone("+91 98499 11223");
        clientDto.setPrimaryContactEmail("murali@jkagri.com");

        ClientResponseDto created = clientService.createClient(clientDto, "user-test");
        assertEquals(ClientStatus.ACTIVE, created.getStatus());

        ClientResponseDto deactivated = clientService.updateClientStatus(created.getId(), ClientStatus.INACTIVE);
        assertEquals(ClientStatus.INACTIVE, deactivated.getStatus());

        ClientResponseDto reactivated = clientService.updateClientStatus(created.getId(), ClientStatus.ACTIVE);
        assertEquals(ClientStatus.ACTIVE, reactivated.getStatus());
    }
}

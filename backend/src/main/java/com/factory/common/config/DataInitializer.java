package com.factory.common.config;

import com.factory.client.entity.Client;
import com.factory.client.entity.ClientAddress;
import com.factory.client.entity.ClientStatus;
import com.factory.client.entity.ClientType;
import com.factory.client.repository.ClientRepository;
import com.factory.office.entity.Office;
import com.factory.office.entity.OfficeType;
import com.factory.office.repository.OfficeRepository;
import com.factory.user.entity.Role;
import com.factory.user.entity.User;
import com.factory.user.repository.UserRepository;
import com.factory.product.entity.*;
import com.factory.product.repository.ProductRepository;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.entity.StockLedgerEntry;
import com.factory.inventory.entity.StockTransactionType;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.inventory.repository.StockLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final OfficeRepository officeRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(OfficeRepository officeRepository,
                           UserRepository userRepository,
                           ClientRepository clientRepository,
                           ProductRepository productRepository,
                           InventoryRepository inventoryRepository,
                           StockLedgerRepository stockLedgerRepository,
                           PasswordEncoder passwordEncoder) {
        this.officeRepository = officeRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockLedgerRepository = stockLedgerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (officeRepository.count() == 0) {
            log.info("Bootstrapping baseline Agrisep offices and godowns...");

            Office hq = new Office(
                    "off-hq-jalna",
                    "Jalna Corporate Headquarters",
                    OfficeType.HEADQUARTERS,
                    "Maharashtra",
                    "Jalna",
                    "Plot No. B-12, MIDC Phase 2, Jalna, Maharashtra 431203",
                    "27AAACI4921C1Z4",
                    "+91 2482 234567",
                    "hq@agrisep.com"
            );

            Office godown = new Office(
                    "off-godown-jalna",
                    "Jalna Central Machinery Godown #1",
                    OfficeType.WAREHOUSE_GODOWN,
                    "Maharashtra",
                    "Jalna",
                    "Gut No. 104, Nagewadi Industrial Area, Jalna, Maharashtra 431213",
                    "27AAACI4921C1Z4",
                    "+91 2482 234568",
                    "godown.jalna@agrisep.com"
            );

            Office gujarat = new Office(
                    "off-reg-gujarat",
                    "Gujarat Regional Sales & Dispatch Office",
                    OfficeType.REGIONAL_OFFICE,
                    "Gujarat",
                    "Ahmedabad",
                    "GIDC Naroda, Ahmedabad, Gujarat 382330",
                    "24AAACI4921C1Z1",
                    "+91 79 4001 2345",
                    "gujarat@agrisep.com"
            );

            Office ap = new Office(
                    "off-reg-ap",
                    "Andhra Pradesh Regional Operations",
                    OfficeType.REGIONAL_OFFICE,
                    "Andhra Pradesh",
                    "Vijayawada",
                    "Autonagar Industrial Estate, Vijayawada, Andhra Pradesh 520007",
                    "37AAACI4921C1Z3",
                    "+91 866 245 6789",
                    "ap@agrisep.com"
            );

            officeRepository.saveAll(List.of(hq, godown, gujarat, ap));
            log.info("Initialized {} baseline operational offices.", 4);
        }

        if (userRepository.count() == 0) {
            log.info("Bootstrapping initial factory personnel accounts...");

            Office hq = officeRepository.findById("off-hq-jalna").orElseThrow();
            Office godown = officeRepository.findById("off-godown-jalna").orElseThrow();
            Office ap = officeRepository.findById("off-reg-ap").orElseThrow();
            Office gujarat = officeRepository.findById("off-reg-gujarat").orElseThrow();

            Set<Office> allOffices = new HashSet<>(officeRepository.findAll());

            // 1. OWNER
            User owner = new User(
                    "usr-owner-001",
                    "owner@agrisep.com",
                    passwordEncoder.encode("Owner@123"),
                    "Suresh Patel",
                    "+91 98220 11111",
                    Role.OWNER,
                    hq
            );
            owner.setAllowedOffices(allOffices);

            // 2. ADMIN
            User admin = new User(
                    "usr-admin-001",
                    "admin@agrisep.com",
                    passwordEncoder.encode("Admin@123"),
                    "Anita Sharma",
                    "+91 98220 22222",
                    Role.ADMIN,
                    hq
            );
            admin.setAllowedOffices(allOffices);

            // 3. MAIN_OFFICE
            User mainOffice = new User(
                    "usr-main-001",
                    "mainoffice@agrisep.com",
                    passwordEncoder.encode("Main@123"),
                    "Rajesh Verma",
                    "+91 98220 33333",
                    Role.MAIN_OFFICE,
                    hq
            );
            mainOffice.setAllowedOffices(Set.of(hq, godown));

            // 4. OUTER_STATE_OFFICE (AP)
            User outerState = new User(
                    "usr-outer-001",
                    "kiran.reddy@agrisep.com",
                    passwordEncoder.encode("Outer@123"),
                    "Kiran Reddy",
                    "+91 98480 44444",
                    Role.OUTER_STATE_OFFICE,
                    ap
            );
            outerState.setAllowedOffices(Set.of(ap));

            // 5. GODOWN_MANAGER
            User godownManager = new User(
                    "usr-godown-001",
                    "godown@agrisep.com",
                    passwordEncoder.encode("Godown@123"),
                    "Ramesh Yadav",
                    "+91 98220 55555",
                    Role.GODOWN_MANAGER,
                    godown
            );
            godownManager.setAllowedOffices(Set.of(godown, hq));

            userRepository.saveAll(List.of(owner, admin, mainOffice, outerState, godownManager));
            log.info("Initialized 5 baseline user roles (OWNER, ADMIN, MAIN_OFFICE, OUTER_STATE_OFFICE, GODOWN_MANAGER).");
        }

        if (clientRepository.count() == 0) {
            log.info("Bootstrapping baseline corporate seed processor accounts...");

            // 1. Advanta Seeds
            Client client1 = new Client();
            client1.setId("cli-advanta-001");
            client1.setCompanyName("Advanta Seeds Holding Ltd");
            client1.setClientType(ClientType.SEED_PRODUCER);
            client1.setGstin("36AABCA1234F1Z9");
            client1.setPan("AABCA1234F");
            client1.setState("Telangana");
            client1.setPrimaryContactName("Venkatesh Rao");
            client1.setPrimaryContactDesignation("Chief Procurement Officer");
            client1.setPrimaryContactPhone("+91 98490 12345");
            client1.setPrimaryContactEmail("v.rao@advantaseeds.com");
            client1.setCreditDays(45);
            client1.setStatus(ClientStatus.ACTIVE);
            client1.setCreatedBy("usr-owner-001");

            ClientAddress addr1 = new ClientAddress();
            addr1.setId("addr-advanta-01");
            addr1.setSiteName("Hyderabad Central Processing Depot");
            addr1.setAddressLine1("Plot 42, IDA Jeedimetla Phase III");
            addr1.setCity("Hyderabad");
            addr1.setState("Telangana");
            addr1.setPincode("500055");
            addr1.setContactPerson("Venkatesh Rao");
            addr1.setPhone("+91 98490 12345");
            addr1.setDefault(true);
            client1.addAddress(addr1);

            // 2. Mahyco
            Client client2 = new Client();
            client2.setId("cli-mahyco-002");
            client2.setCompanyName("Maharashtra Hybrid Seeds Co. (Mahyco)");
            client2.setClientType(ClientType.SEED_PRODUCER);
            client2.setGstin("27AABCM2222F1Z7");
            client2.setPan("AABCM2222F");
            client2.setState("Maharashtra");
            client2.setPrimaryContactName("Rajesh Patil");
            client2.setPrimaryContactDesignation("Plant Head Machinery Procurement");
            client2.setPrimaryContactPhone("+91 98220 33445");
            client2.setPrimaryContactEmail("rajesh.patil@mahyco.com");
            client2.setCreditDays(30);
            client2.setStatus(ClientStatus.ACTIVE);
            client2.setCreatedBy("usr-owner-001");

            ClientAddress addr2 = new ClientAddress();
            addr2.setId("addr-mahyco-01");
            addr2.setSiteName("Jalna Seed Conditioning Plant");
            addr2.setAddressLine1("Old MIDC Road, Beside Railway Station");
            addr2.setCity("Jalna");
            addr2.setState("Maharashtra");
            addr2.setPincode("431203");
            addr2.setContactPerson("Rajesh Patil");
            addr2.setPhone("+91 98220 33445");
            addr2.setDefault(true);
            client2.addAddress(addr2);

            clientRepository.saveAll(List.of(client1, client2));
            log.info("Initialized {} baseline corporate clients with delivery sites.", 2);
        }

        if (productRepository.count() == 0) {
            log.info("Bootstrapping baseline Agrisep® seed processing machinery master models...");

            // 1. AG-GS-10: Specific Gravity Separator GS-10
            Product gs10 = new Product();
            gs10.setId("prod-gs-10");
            gs10.setSku("AG-GS-10");
            gs10.setName("Agrisep® Precision Specific Gravity Separator GS-10");
            gs10.setCategory(ProductCategory.GRAVITY_SEPARATOR);
            gs10.setUnit(ProductUnit.SET);
            gs10.setDescription("Flagship high-capacity vibro-pneumatic density separator for cereal, pulse, and oil seeds.");
            gs10.setBasePriceINR(new BigDecimal("1250000.00"));
            gs10.setLeadTimeWeeks(6);
            gs10.setWebsiteVisibility(WebsiteVisibility.PUBLIC);
            gs10.setInternalStatus(ProductInternalStatus.ACTIVE);

            ProductSpec specGs10 = new ProductSpec();
            specGs10.setId("spec-gs-10");
            specGs10.setCapacity("10 - 12 TPH (Wheat / Paddy)");
            specGs10.setMotorPower("15 HP Main Blower + 1.5 HP Vibro Deck");
            specGs10.setMotorRating("TEFC IP55 Siemens/ABB");
            specGs10.setDimensions("2800 x 1850 x 2100 mm");
            specGs10.setWeight("2450 kg");
            specGs10.setAirVolume("12,500 m3/hr");
            specGs10.setScreenArea("4.2 m2");
            specGs10.setSeedCompatibility("Wheat, Paddy, Soybean, Corn, Millets");
            gs10.setSpecs(specGs10);

            // 2. AG-SC-04: Multi-Screen Seed Cleaner SC-4
            Product sc04 = new Product();
            sc04.setId("prod-sc-04");
            sc04.setSku("AG-SC-04");
            sc04.setName("Agrisep® Fine Seed Cleaner & Grader SC-4");
            sc04.setCategory(ProductCategory.SEED_CLEANER);
            sc04.setUnit(ProductUnit.SET);
            sc04.setDescription("Triple-deck screen sifter with aspiration pre-cleaning channel.");
            sc04.setBasePriceINR(new BigDecimal("680000.00"));
            sc04.setLeadTimeWeeks(4);
            sc04.setWebsiteVisibility(WebsiteVisibility.PUBLIC);
            sc04.setInternalStatus(ProductInternalStatus.ACTIVE);

            ProductSpec specSc04 = new ProductSpec();
            specSc04.setId("spec-sc-04");
            specSc04.setCapacity("4 - 5 TPH");
            specSc04.setMotorPower("5 HP Main Screen Drive + 3 HP Aspiration Blower");
            specSc04.setMotorRating("TEFC IP55");
            specSc04.setDimensions("2400 x 1500 x 2200 mm");
            specSc04.setWeight("1850 kg");
            specSc04.setAirVolume("8,000 m3/hr");
            specSc04.setScreenArea("3.6 m2");
            specSc04.setSeedCompatibility("Wheat, Paddy, Mustard, Sunflower, Pulses");
            sc04.setSpecs(specSc04);

            // 3. AG-DST-05: Seed Destoner DST-5
            Product dst05 = new Product();
            dst05.setId("prod-dst-05");
            dst05.setSku("AG-DST-05");
            dst05.setName("Agrisep® Pressure Type Seed Destoner DST-5");
            dst05.setCategory(ProductCategory.DESTONER);
            dst05.setUnit(ProductUnit.SET);
            dst05.setDescription("Fluidized-bed stone and heavy-impurity extraction separator.");
            dst05.setBasePriceINR(new BigDecimal("450000.00"));
            dst05.setLeadTimeWeeks(4);
            dst05.setWebsiteVisibility(WebsiteVisibility.PUBLIC);
            dst05.setInternalStatus(ProductInternalStatus.ACTIVE);

            ProductSpec specDst05 = new ProductSpec();
            specDst05.setId("spec-dst-05");
            specDst05.setCapacity("5 TPH");
            specDst05.setMotorPower("5 HP Centrifugal Fan + 0.5 HP Eccentric Drive");
            specDst05.setDimensions("1900 x 1200 x 1800 mm");
            specDst05.setWeight("920 kg");
            specDst05.setSeedCompatibility("Paddy, Wheat, Soybean, Gram");
            dst05.setSpecs(specDst05);

            productRepository.saveAll(List.of(gs10, sc04, dst05));
            log.info("Initialized {} baseline Agrisep® machinery models with specifications.", 3);
        }

        if (inventoryRepository.count() == 0) {
            log.info("Bootstrapping baseline godown stock levels and ledger...");

            Office jalnaGodown = officeRepository.findById("off-godown-jalna").orElseThrow();
            Product gs10 = productRepository.findById("prod-gs-10").orElseThrow();
            Product sc04 = productRepository.findById("prod-sc-04").orElseThrow();
            Product dst05 = productRepository.findById("prod-dst-05").orElseThrow();

            Inventory inv1 = new Inventory(jalnaGodown, gs10, 8, 2);
            Inventory inv2 = new Inventory(jalnaGodown, sc04, 5, 2);
            Inventory inv3 = new Inventory(jalnaGodown, dst05, 6, 2);

            inventoryRepository.saveAll(List.of(inv1, inv2, inv3));

            for (Inventory inv : List.of(inv1, inv2, inv3)) {
                StockLedgerEntry entry = new StockLedgerEntry();
                entry.setInventory(inv);
                entry.setOffice(jalnaGodown);
                entry.setProduct(inv.getProduct());
                entry.setTransactionType(StockTransactionType.INITIAL_STOCK);
                entry.setQuantityDelta(inv.getOnHandQuantity());
                entry.setOnHandBefore(0);
                entry.setOnHandAfter(inv.getOnHandQuantity());
                entry.setReservedBefore(0);
                entry.setReservedAfter(0);
                entry.setAvailableBefore(0);
                entry.setAvailableAfter(inv.getOnHandQuantity());
                entry.setReferenceType("SYSTEM_BOOTSTRAP");
                entry.setNotes("Initial baseline stock bootstrap for Agrisep® seed processing machinery.");
                entry.setCreatedBy("usr-owner-001");
                stockLedgerRepository.save(entry);
            }
            log.info("Initialized baseline stock for 3 machinery products in Jalna Central Godown.");
        }
    }
}

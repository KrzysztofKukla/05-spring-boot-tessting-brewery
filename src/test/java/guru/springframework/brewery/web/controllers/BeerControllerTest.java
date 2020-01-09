package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerService;
import guru.springframework.brewery.web.model.BeerDto;
import guru.springframework.brewery.web.model.BeerPagedList;
import guru.springframework.brewery.web.model.BeerStyleEnum;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Krzysztof Kukla
 */
@ExtendWith(MockitoExtension.class)
class BeerControllerTest {

    @Mock
    private BeerService beerService;

    @InjectMocks
    private BeerController beerController;

    private MockMvc mockMvc;

    private BeerDto beerDto1;

    @BeforeEach
    void setUp() {
        beerDto1 = BeerDto.builder()
            .id(UUID.randomUUID())
            .beerName("first beer")
            .beerStyle(BeerStyleEnum.LAGER)
            .upc(1L)
            .quantityOnHand(10)
            .price(new BigDecimal("12.99"))
            .createdDate(OffsetDateTime.now())
            .lastModifiedDate(OffsetDateTime.now())
            .build();

        mockMvc = MockMvcBuilders.standaloneSetup(beerController).build();
    }

    @Test
    void listBeers() {
    }

    @Test
    void getBeerById() throws Exception {
        BDDMockito.when(beerService.findBeerById(ArgumentMatchers.any())).thenReturn(beerDto1);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/{beerId}", UUID.randomUUID()))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", Matchers.is(beerDto1.getId().toString())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.beerName", Matchers.is(beerDto1.getBeerName())));
    }

    @DisplayName(value = "List operations-> ")
    @Nested
    class TestListOperation {

        @Captor
        private ArgumentCaptor<String> beerNameCaptor;

        @Captor
        private ArgumentCaptor<BeerStyleEnum> beerStyleCaptor;

        @Captor
        private ArgumentCaptor<PageRequest> pageRequestCaptor;

        BeerPagedList beerPagedList;

        BeerDto beerDto2;

        @BeforeEach
        void setUp() {
            beerDto2 = BeerDto.builder()
                .id(UUID.randomUUID())
                .version(2)
                .beerName("second beer")
                .beerStyle(BeerStyleEnum.LAGER)
                .price(new BigDecimal(23))
                .upc(10L)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .quantityOnHand(10)
                .build();

            List<BeerDto> beerDtoList = Lists.list(beerDto1, beerDto2);

            BeerPagedList beerPagedList = new BeerPagedList(beerDtoList, PageRequest.of(1, 1), 2L);

            BDDMockito.given(beerService.listBeers(beerNameCaptor.capture(), beerStyleCaptor.capture(),
                pageRequestCaptor.capture())).willReturn(beerPagedList);

        }

        @DisplayName(value = "Test list beers without any parameters-> ")
        @Test
        void noParameters() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))
                //to be aware this works only for specific order like here
                //if we are getting order from database there is no guarantees such orders
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].beerName", Matchers.is(beerDto1.getBeerName())))
                .andExpect(jsonPath("$.content[1].version", Matchers.is(beerDto2.getVersion())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].beerStyle", Matchers.is(beerDto2.getBeerStyle().name())));

            PageRequest pageRequestCaptured = pageRequestCaptor.getValue();
            Assertions.assertNotNull(pageRequestCaptured);
            Assertions.assertEquals(BeerController.DEFAULT_PAGE_NUMBER.intValue(), pageRequestCaptured.getPageNumber());
            Assertions.assertEquals(BeerController.DEFAULT_PAGE_SIZE.intValue(), pageRequestCaptured.getPageSize());
            Assertions.assertNull(beerNameCaptor.getValue());
            Assertions.assertNull(beerStyleCaptor.getValue());
        }

    }

}
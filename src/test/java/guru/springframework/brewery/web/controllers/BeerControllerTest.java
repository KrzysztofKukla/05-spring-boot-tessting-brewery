package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerService;
import guru.springframework.brewery.web.model.BeerDto;
import guru.springframework.brewery.web.model.BeerStyleEnum;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    private BeerDto beerDto;

    @BeforeEach
    void setUp() {
        beerDto = BeerDto.builder()
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
        BDDMockito.when(beerService.findBeerById(ArgumentMatchers.any())).thenReturn(beerDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/{beerId}",UUID.randomUUID()))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", Matchers.is(beerDto.getId().toString())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.beerName",Matchers.is(beerDto.getBeerName())));
    }

}
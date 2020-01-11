package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.BeerOrderDto;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import guru.springframework.brewery.web.model.OrderStatusEnum;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Krzysztof Kukla
 */
@WebMvcTest(controllers = BeerOrderController.class)
class BeerOrderControllerTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    @MockBean
    private BeerOrderService beerOrderService;

    @Autowired
    private MockMvc mockMvc;

    private BeerOrderDto beerOrderDto1;

    @BeforeEach
    void setUp() {
        beerOrderDto1 = BeerOrderDto.builder()
            .id(UUID.randomUUID())
            .version(10)
            .createdDate(OffsetDateTime.now())
            .lastModifiedDate(OffsetDateTime.now())
            .customerId(UUID.randomUUID())
            .customerRef("customerRef1")
            .beerOrderLines(Lists.emptyList())
            .orderStatus(OrderStatusEnum.NEW)
            .orderStatusCallbackUrl("orderStatusCallbackUrl1")
            .build();
    }

    @AfterEach
    void tearDown() {
        BDDMockito.reset(beerOrderService);
    }

    @DisplayName(value = "List order test-> ")
    @Nested
    class ListOrderTests {

        @Captor
        private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

        @BeforeEach
        void setUp() {
            BeerOrderDto beerOrderDto2 = createBeerOrderDto();
            List<BeerOrderDto> beerOrderList = Lists.list(beerOrderDto1, beerOrderDto2);
            PageRequest pageRequest = PageRequest.of(2, 1);
            BeerOrderPagedList beerOrderPagedList = new BeerOrderPagedList(beerOrderList, pageRequest, 1);

            BDDMockito.given(beerOrderService.listOrders(any(UUID.class), pageRequestArgumentCaptor.capture())).willReturn(beerOrderPagedList);
        }

        @Test
        void listOrderNoParameters() throws Exception {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/{customerId}/orders", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.pageable.pageNumber", Matchers.is(2)))
                .andExpect(jsonPath("$.pageable.pageSize", Matchers.is(1)))
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[0].version", Matchers.is(10)))
                .andReturn();
            System.out.println(mvcResult.getResponse().getContentAsString());

            Assertions.assertNotNull(pageRequestArgumentCaptor.getValue());
            Assertions.assertEquals(BeerOrderController.DEFAULT_PAGE_NUMBER.intValue(), pageRequestArgumentCaptor.getValue().getPageNumber());
            Assertions.assertEquals(BeerOrderController.DEFAULT_PAGE_SIZE.intValue(), pageRequestArgumentCaptor.getValue().getPageSize());
        }

        @Test
        void listOrdersWithParameters() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/{customerId}/orders", UUID.randomUUID())
                .param("pageNumber", "30")
                .param("pageSize", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[1].customerRef", Matchers.is("customerRef2")));

            Assertions.assertNotNull(pageRequestArgumentCaptor.getValue());
            Assertions.assertEquals(30, pageRequestArgumentCaptor.getValue().getPageNumber());
            Assertions.assertEquals(4, pageRequestArgumentCaptor.getValue().getPageSize());
        }

        private BeerOrderDto createBeerOrderDto() {
            return BeerOrderDto.builder()
                .id(UUID.randomUUID())
                .version(20)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .customerId(UUID.randomUUID())
                .customerRef("customerRef2")
                .beerOrderLines(Lists.emptyList())
                .orderStatus(OrderStatusEnum.READY)
                .orderStatusCallbackUrl("orderStatusCallbackUrl2")
                .build();
        }

    }

    @Test
    void getOrder() throws Exception {
        BDDMockito.given(beerOrderService.getOrderById(any(UUID.class), any(UUID.class)))
            .willReturn(beerOrderDto1);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/{customerId}/orders/{orderId}",
            UUID.randomUUID(), UUID.randomUUID()))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.jsonPath("$.beerOrderLines", Matchers.hasSize(0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.version", Matchers.is(beerOrderDto1.getVersion())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.createdDate",
                Matchers.is(DATE_TIME_FORMATTER.format(beerOrderDto1.getCreatedDate()))))
            .andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

}

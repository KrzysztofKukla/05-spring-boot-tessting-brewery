package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.domain.BeerOrder;
import guru.springframework.brewery.domain.Customer;
import guru.springframework.brewery.repositories.BeerOrderRepository;
import guru.springframework.brewery.repositories.CustomerRepository;
import guru.springframework.brewery.web.model.BeerOrderDto;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author Krzysztof Kukla
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BeerOrderControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BeerOrderRepository beerOrderRepository;

    @Test
    void listOrdersTest() throws Exception {
        Customer customer = customerRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Customer does not exist"));
        String url = "/api/v1/customers/" + customer.getId() + "/orders";
        BeerOrderPagedList beerOrderPagedList = testRestTemplate.getForObject(url, BeerOrderPagedList.class);

        Assertions.assertNotNull(beerOrderPagedList);
        Assertions.assertEquals(1, beerOrderPagedList.getContent().size());
    }

    @Test
    void getOrderTest() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().get(0);
        String url = "/api/v1/customers/" + beerOrder.getCustomer().getId() + "/orders" + beerOrder.getId();
        BeerOrderDto beerOrderDto = testRestTemplate.getForObject(url, BeerOrderDto.class);

        Assertions.assertNotNull(beerOrderDto);
    }

}
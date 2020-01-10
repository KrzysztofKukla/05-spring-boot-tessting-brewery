package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.web.model.BeerPagedList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author Krzysztof Kukla
 */
//we create Web environment and embedded server here
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BeerControllerIntegrationIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void listBeers() throws Exception {
        BeerPagedList beerPagedList = testRestTemplate.getForObject("/api/v1/beer", BeerPagedList.class);

        //content for beerPageList initialized in bootstrap
        Assertions.assertThat(beerPagedList.getContent()).hasSize(3);
    }

}

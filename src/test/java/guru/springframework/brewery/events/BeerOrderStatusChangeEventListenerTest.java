package guru.springframework.brewery.events;

import com.github.jenspiegsa.wiremockextension.Managed;
import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import guru.springframework.brewery.domain.BeerOrder;
import guru.springframework.brewery.domain.OrderStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author Krzysztof Kukla
 */
// Wiremock allows to test a client
@ExtendWith(WireMockExtension.class)
class BeerOrderStatusChangeEventListenerTest {

    //it will be manage by WireMockExtension, so it will sets up everything needed for WireMock
    @Managed
    private WireMockServer wireMockServer = ManagedWireMockServer.with(WireMockConfiguration.wireMockConfig().dynamicPort());

    private BeerOrderStatusChangeEventListener listener;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        listener = new BeerOrderStatusChangeEventListener(restTemplateBuilder);
    }

    @Test
    void listen() {
        //here we define stub for wiremock for that 'url'
        //we are configuring wiremockserver to accept post with 'update' url to return 'Ok' with it
        wireMockServer.stubFor(WireMock.post("/update").willReturn(WireMock.ok()));

        //here we simulate change status from NEW to READY
        BeerOrder beerOrder = BeerOrder.builder()
            .orderStatus(OrderStatusEnum.READY)
            .orderStatusCallbackUrl("http://localhost:" + wireMockServer.port() + "/update")
            .createdDate(Timestamp.valueOf(LocalDateTime.now()))
            .build();

        BeerOrderStatusChangeEvent event = new BeerOrderStatusChangeEvent(beerOrder, OrderStatusEnum.NEW);
        listener.listen(event);

        WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/update")));
    }

}
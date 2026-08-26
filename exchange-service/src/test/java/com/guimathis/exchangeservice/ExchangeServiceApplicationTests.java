package com.guimathis.exchangeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = ExchangeServiceApplication.class)
class ExchangeServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}

package com.guimathis.service;

import com.guimathis.dto.ExchangeDTO;
import com.guimathis.environment.InstanceInformationService;
import com.guimathis.model.Book;
import com.guimathis.proxy.ExchangeClient;
import com.guimathis.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    @Autowired
    private InstanceInformationService informationService;

    @Autowired
    private BookRepository repository;

    @Autowired
    private ExchangeClient exchangeClient;

    public Book findBook(Long id, String currency) {
        String port = informationService.retrieveServerPort();

        var book = repository.findById(id).orElseThrow();

          ExchangeDTO exchange = exchangeClient.getExchange(book.getPrice(), "USD", currency);

        if (exchange == null) {
            throw new RuntimeException("Failed to retrieve exchange rate");
        }
//        book.setEnvironment(port + "FeignClient");
        book.setEnvironment("Book PORT: " + port + " Exchange PORT: " + exchange.getEnvironment());
        book.setPrice(exchange.getConvertedValue());
        book.setCurrency(currency);
        return book;
    }

    // Antes do feignClient
//    public Book findBook(Long id, String currency) {
//        String port = informationService.retrieveServerPort();
//
//        var book = repository.findById(id).orElseThrow();
//
//        HashMap<String, String> params = new HashMap<>();
//        params.put("amount", book.getPrice().toString());
//        params.put("from", "USD");
//        params.put("to", currency);
//
//        var response = new RestTemplate()
//                .getForEntity(API_URL, ExchangeDTO.class, params);
//
//        ExchangeDTO exchange = response.getBody();
//        if (exchange == null) {
//            throw new RuntimeException("Failed to retrieve exchange rate");
//        }
//        book.setEnvironment(port);
//        book.setPrice(exchange.getConvertedValue());
//        book.setCurrency(currency);
//        return book;
//    }
}

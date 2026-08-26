package com.guimathis.service;

import com.guimathis.exception.ReviewGenerationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    public static final int MAX_REVIEW_LENGTH = 500;

    private final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ChatClient chatClient;

    public ReviewService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Retry(name = "review-service")
    @CircuitBreaker(name = "review-service", fallbackMethod = "getReviewFallback")
    @RateLimiter(name = "review-service")
    public String generateReview(String title) {
        try {
            String prompt = """
                    Escreva um resumo objetivo e direto do livro "%s".
                    Não inclua opiniões e não repita o título na resposta.
                    O idioma da resposta deve ser português.
                    A resposta deve ter no máximo "%s" caracteres.
                    """.formatted(title, MAX_REVIEW_LENGTH);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response != null && response.length() > MAX_REVIEW_LENGTH) {
                return response.substring(0, MAX_REVIEW_LENGTH);
            }

            return response;
        } catch (Exception e) {
            throw new ReviewGenerationException("Erro ao gerar review para o livro: " + title, e);
        }
    }

    private String getReviewFallback(String title, Throwable e) {
        logger.warn("Fallback ativado para o review do livro {}. Causa: {}", title, e.getMessage());
        return null;
    }
}

package com.cheocharm.MapZ.common.client.webclient;

import com.cheocharm.MapZ.common.exception.jwt.InvalidJwtException;
import com.cheocharm.MapZ.common.oauth.OauthUrl;
import com.cheocharm.MapZ.user.presentation.dto.response.GoogleIdTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class GoogleAuthWebClient {

    private final WebClient webClient;

    public GoogleAuthWebClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(OauthUrl.GOOGLE.getUrl())
                .build();
    }

    private static final long RETRY_COUNT = 2;
    private static final long DELAY_SECOND = 3;

    public GoogleIdTokenResponse getGoogleAuth(String token) {
        Mono<GoogleIdTokenResponse> googleIdTokenResponseMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("id_token", token)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new InvalidJwtException()))
                .bodyToMono(GoogleIdTokenResponse.class)
                .retryWhen(Retry.backoff(RETRY_COUNT, Duration.ofSeconds(DELAY_SECOND))
                        .filter(throwable -> !(throwable instanceof InvalidJwtException))
                );

        return googleIdTokenResponseMono.block();
    }
}

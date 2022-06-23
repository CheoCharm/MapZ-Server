package com.cheocharm.MapZ.common.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
public class OauthApi {

    private final RestTemplate restTemplate;

    public ResponseEntity<String> callGoogle(OauthUrl url, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url.getUrl()).queryParam("id_token", token);

        return restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                request,
                String.class
        );
    }
}


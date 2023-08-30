package com.mapz.api.common.oauth;

import com.mapz.api.common.exception.jwt.InvalidJwtException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Deprecated
@RequiredArgsConstructor
@Component
public class OauthApi {

    private final RestTemplate restTemplate;

    public ResponseEntity<String> callGoogle(OauthUrl url, String token) {

        HttpHeaders headers = setHeaders();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url.getUrl()).queryParam("id_token", token);

        final ResponseEntity<String> response = restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                request,
                String.class
        );
        checkHttpStatus(response);
        return response;
    }

    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void checkHttpStatus(ResponseEntity<String> response) {
        if (ObjectUtils.notEqual(response.getStatusCode(), HttpStatus.OK)) {
            throw new InvalidJwtException();
        }
    }
}


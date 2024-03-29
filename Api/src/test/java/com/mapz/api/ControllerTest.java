package com.mapz.api;

import com.mapz.api.common.fixtures.UserFixtures;
import com.mapz.api.common.jwt.JwtCreateUtils;
import com.mapz.domain.domains.user.enums.UserProvider;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.mapz.api.common.fixtures.UserFixtures.VALID_EMAIL;
import static com.mapz.api.common.fixtures.UserFixtures.VALID_USERNAME;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ControllerTest {

    protected static final String AUTHORIZATION_HEADER_NAME = "accessToken";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtCreateUtils jwtCreateUtils;

    @Autowired
    protected UserRepository userRepository;

    protected MockMultipartFile getMockMultipartFile(String name) {
        return new MockMultipartFile(name, new byte[0]);
    }

    protected String getAccessToken() {
        return jwtCreateUtils.createAccessToken(
                VALID_EMAIL,
                VALID_USERNAME,
                UserProvider.MAPZ
        );
    }
}

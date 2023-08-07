package com.cheocharm.MapZ;

import com.cheocharm.MapZ.common.jwt.JwtCreateUtils;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
                "mapzbest@gmail.com",
                "최강맵지",
                UserProvider.MAPZ
        );
    }
}

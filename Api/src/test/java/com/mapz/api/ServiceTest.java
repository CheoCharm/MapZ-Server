package com.mapz.api;

import com.mapz.api.common.image.ImageHandler;
import com.mapz.domain.domains.agreement.repository.AgreementRepository;
import com.mapz.domain.domains.diary.repository.DiaryImageRepository;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.mapz.domain.domains.usergroup.repository.UserGroupRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class ServiceTest {

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected GroupRepository groupRepository;

    @MockBean
    protected UserGroupRepository userGroupRepository;

    @MockBean
    protected DiaryRepository diaryRepository;

    @MockBean
    protected DiaryImageRepository diaryImageRepository;

    @MockBean
    protected AgreementRepository agreementRepository;

    @MockBean
    protected ImageHandler imageHandler;

    protected MockMultipartFile getMockMultipartFile(String name) {
        return new MockMultipartFile(name, new byte[0]);
    }

    protected MockMultipartFile getMockMultipartFileHasContent(String name) {
        return new MockMultipartFile(name, new byte[3]);
    }
}

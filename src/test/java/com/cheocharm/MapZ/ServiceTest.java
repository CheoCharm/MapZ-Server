package com.cheocharm.MapZ;

import com.cheocharm.MapZ.common.image.ImageHandler;
import com.cheocharm.MapZ.diary.domain.repository.DiaryImageRepository;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.usergroup.domain.repository.UserGroupRepository;
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
    protected ImageHandler imageHandler;

    protected MockMultipartFile getMockMultipartFile(String name) {
        return new MockMultipartFile(name, new byte[0]);
    }
}

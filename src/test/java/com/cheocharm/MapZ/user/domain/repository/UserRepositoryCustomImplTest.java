package com.cheocharm.MapZ.user.domain.repository;

import com.cheocharm.MapZ.RepositoryTest;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.UserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

import static com.cheocharm.MapZ.common.util.PagingUtils.*;
import static org.assertj.core.api.Assertions.*;

@RepositoryTest
class UserRepositoryCustomImplTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity userEntity1;
    private UserEntity userEntity2;

    @BeforeEach
    void BeforeEach() {
        userEntity1 = userRepository.save(
                UserEntity.builder()
                        .email("gildong@gmail.com")
                        .username("홍길동")
                        .password("pass")
                        .bio("자기소개를 입력해주세요")
                        .refreshToken("1234")
                        .fcmToken("1235")
                        .userImageUrl("11")
                        .userProvider(UserProvider.GOOGLE)
                        .build()
        );

        userEntity2 = userRepository.save(
                UserEntity.builder()
                        .email("test10@naver.com")
                        .username("테스트")
                        .password("password")
                        .bio("자기소개를 입력해주세요")
                        .refreshToken("1236")
                        .fcmToken("1237")
                        .userProvider(UserProvider.MAPZ)
                        .build()
        );

    }

    @DisplayName("유저 아이디 리스트로 유저 엔티티 리스트 조회")
    @Test
    void getUserEntityListByUserIdList() {

        //given
        ArrayList<Long> userIdList = new ArrayList<>();
        userIdList.add(userEntity1.getId());
        userIdList.add(userEntity2.getId());

        //when
        List<UserEntity> userEntityList = userRepository.getUserEntityListByUserIdList(userIdList);

        //then
        assertThat(userEntityList.size()).isEqualTo(userIdList.size());
    }

    @DisplayName("유저 조회하는 페이징 메서드")
    @Test
    void fetchByUserEntityAndSearchName() {

        //given
        ArrayList<UserEntity> list = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            list.add(
                    UserEntity.builder()
                            .email(String.valueOf(i).concat("gildong@gmail.com"))
                            .username("테스트".concat(String.valueOf(i)))
                            .password("pass")
                            .bio("자기소개를 입력해주세요")
                            .refreshToken("1234")
                            .fcmToken("1235")
                            .userImageUrl("11")
                            .userProvider(UserProvider.MAPZ)
                            .build()
            );
        }
        userRepository.saveAll(list);

        //when
        Slice<UserEntity> firstUserEntitySlice = userRepository.fetchByUserEntityAndSearchName(
                userEntity2,
                "테스트",
                applyCursorId(0L),
                applyDescPageConfigBy(0, USER_SIZE, FIELD_CREATED_AT)
        );
        List<UserEntity> firstContent = firstUserEntitySlice.getContent();

        Slice<UserEntity> secondUserEntitySlice = userRepository.fetchByUserEntityAndSearchName(
                userEntity2,
                "테스트",
                applyCursorId(firstContent.get(firstContent.size() - 1).getId()),
                applyDescPageConfigBy(1, USER_SIZE, FIELD_CREATED_AT)
        );
        List<UserEntity> secondContent = secondUserEntitySlice.getContent();

        //then
        assertThat(firstContent.size()).isEqualTo(USER_SIZE);
        assertThat(secondContent.size()).isEqualTo(6);
        assertThat(secondUserEntitySlice.hasNext()).isFalse();
    }
}
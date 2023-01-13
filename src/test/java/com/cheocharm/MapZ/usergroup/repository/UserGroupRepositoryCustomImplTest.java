package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.RepositoryTest;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.usergroup.InvitationStatus;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.UserRole;
import com.cheocharm.MapZ.usergroup.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.repository.vo.CountUserGroupVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@RepositoryTest
class UserGroupRepositoryCustomImplTest {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private List<GroupEntity> groupEntityList;
    private UserEntity userEntity;

    @BeforeEach
    void beforeEach() {
        userEntity = UserEntity.builder()
                .email("test10@naver.com")
                .username("최강")
                .password("password")
                .bio("자기소개를 입력해주세요")
                .refreshToken("1236")
                .fcmToken("1237")
                .userProvider(UserProvider.MAPZ)
                .build();
        userRepository.save(userEntity);

        groupEntityList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            GroupEntity groupEntity = GroupEntity.builder()
                    .groupName(String.valueOf(i).concat("테스트"))
                    .bio("bio")
                    .groupImageUrl("imageUrl")
                    .openStatus(true)
                    .groupUUID(String.valueOf(i))
                    .build();
            groupEntityList.add(groupEntity);

            userGroupRepository.save(
                    UserGroupEntity.builder()
                            .groupEntity(groupEntity)
                            .userEntity(userEntity)
                            .userRole(UserRole.CHIEF)
                            .invitationStatus(InvitationStatus.ACCEPT)
                            .build()
            );
        }
        groupRepository.saveAll(groupEntityList);
    }

    @DisplayName("UserEntity 로 내가 속한 그룹 조회")
    @Test
    void getGroupEntityList() {

        //given
        UserEntity anotherUser = UserEntity.builder()
                .email("mapz10@naver.com")
                .username("최강맵지")
                .password("password")
                .bio("자기소개를 입력해주세요")
                .refreshToken("1238")
                .fcmToken("1239")
                .userProvider(UserProvider.MAPZ)
                .build();
        userRepository.save(anotherUser);

        //when
        List<GroupEntity> list = userGroupRepository.getGroupEntityList(userEntity);
        List<GroupEntity> groupListOfAnotherUser = userGroupRepository.getGroupEntityList(anotherUser);

        //then
        assertThat(list.size()).isEqualTo(groupEntityList.size());
        assertThat(groupListOfAnotherUser.size()).isEqualTo(0);
    }

    @DisplayName("그룹별 그룹장 이미지 조회")
    @Test
    void findChiefUserImage() {

        //given, when
        List<ChiefUserImageVO> chiefUserImageList = userGroupRepository.findChiefUserImage(groupEntityList);

        //then
        assertThat(chiefUserImageList.size()).isEqualTo(groupEntityList.size());

    }

    @DisplayName("groupId에 해당하는 그룹의 유저, 검색어로 조회")
    @Test
    void findBySearchNameAndGroupId() {

        //given
        Random rand = new Random();
        long groupId = groupEntityList.get(rand.nextInt(groupEntityList.size() - 1)).getId();
        GroupEntity groupEntity = groupRepository.findById(groupId)
                .orElseThrow(NoSuchElementException::new);

        ArrayList<UserEntity> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(UserEntity.builder()
                    .email(String.valueOf(i).concat("test10@naver.com"))
                    .username(String.valueOf(i).concat("테스트"))
                    .password("password")
                    .bio("자기소개를 입력해주세요")
                    .refreshToken("1236")
                    .fcmToken("1237")
                    .userProvider(UserProvider.MAPZ)
                    .build()
            );
        }
        userRepository.saveAll(list);
        for (int i = 0; i < 5; i++) {
            userGroupRepository.save(
                    UserGroupEntity.builder()
                            .userEntity(list.get(i))
                            .groupEntity(groupEntity)
                            .userRole(UserRole.MEMBER)
                            .invitationStatus(InvitationStatus.ACCEPT)
                            .build()
            );
        }

        //when
        List<UserGroupEntity> userGroupEntityList = userGroupRepository
                .findBySearchNameAndGroupId("테스트", groupId);

        //then
        assertThat(userGroupEntityList.size()).isEqualTo(list.size());

    }

    @DisplayName("그룹별 유저수, 그룹 아이디 조회")
    @Test
    void countByGroupEntity() {

        //given
        Random rand = new Random();
        HashMap<Long, Long> map = new HashMap<>();
        for (GroupEntity groupEntity : groupEntityList) {
            ArrayList<UserEntity> userEntityList = new ArrayList<>();
            for (int i = 0; i < rand.nextInt(5) + 1; i++) {
                userEntityList.add(UserEntity.builder()
                        .email(UUID.randomUUID().toString())
                        .username(UUID.randomUUID().toString())
                        .password("password")
                        .bio("자기소개를 입력해주세요")
                        .refreshToken("1236")
                        .fcmToken("1237")
                        .userProvider(UserProvider.MAPZ)
                        .build()
                );
            }
            userRepository.saveAll(userEntityList);

            for (UserEntity user : userEntityList) {
                userGroupRepository.save(
                        UserGroupEntity.builder()
                                .userEntity(user)
                                .groupEntity(groupEntity)
                                .invitationStatus(InvitationStatus.ACCEPT)
                                .userRole(UserRole.MEMBER)
                                .build()
                );
            }
            map.put(groupEntity.getId(), (long) userEntityList.size() + 1);
        }
        //when
        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroupEntity(groupEntityList);

        //then
        for (CountUserGroupVO countUserGroupVO : countUserGroupVOS) {
            assertThat(map.get(countUserGroupVO.getId())).isEqualTo(countUserGroupVO.getCnt());
        }
    }
}
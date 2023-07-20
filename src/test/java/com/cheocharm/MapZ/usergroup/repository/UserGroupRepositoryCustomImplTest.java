package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.RepositoryTest;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
import com.cheocharm.MapZ.usergroup.domain.UserRole;
import com.cheocharm.MapZ.usergroup.domain.repository.UserGroupRepository;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
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

    private List<Group> groupList;
    private User user;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .email("test10@naver.com")
                .username("최강")
                .password("password")
                .bio("자기소개를 입력해주세요")
                .refreshToken("1236")
                .fcmToken("1237")
                .userProvider(UserProvider.MAPZ)
                .build();
        userRepository.save(user);

        groupList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Group group = Group.builder()
                    .groupName(String.valueOf(i).concat("테스트"))
                    .bio("bio")
                    .groupImageUrl("imageUrl")
                    .openStatus(true)
                    .groupUUID(String.valueOf(i))
                    .build();
            groupList.add(group);

            userGroupRepository.save(
                    UserGroup.builder()
                            .group(group)
                            .user(user)
                            .userRole(UserRole.CHIEF)
                            .invitationStatus(InvitationStatus.ACCEPT)
                            .build()
            );
        }
        groupRepository.saveAll(groupList);
    }

    @DisplayName("UserEntity 로 내가 속한 그룹 조회")
    @Test
    void getGroupEntityList() {

        //given
        User anotherUser = User.builder()
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
        List<Group> list = userGroupRepository.getGroups(user);
        List<Group> groupListOfAnotherUser = userGroupRepository.getGroups(anotherUser);

        //then
        assertThat(list.size()).isEqualTo(groupList.size());
        assertThat(groupListOfAnotherUser.size()).isEqualTo(0);
    }

    @DisplayName("그룹별 그룹장 이미지 조회")
    @Test
    void findChiefUserImage() {

        //given, when
        List<ChiefUserImageVO> chiefUserImageList = userGroupRepository.findChiefUserImage(groupList);

        //then
        assertThat(chiefUserImageList.size()).isEqualTo(groupList.size());

    }

    @DisplayName("groupId에 해당하는 그룹의 유저, 검색어로 조회")
    @Test
    void findBySearchNameAndGroupId() {

        //given
        Random rand = new Random();
        long groupId = groupList.get(rand.nextInt(groupList.size() - 1)).getId();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(NoSuchElementException::new);

        ArrayList<User> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(User.builder()
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
                    UserGroup.builder()
                            .user(list.get(i))
                            .group(group)
                            .userRole(UserRole.MEMBER)
                            .invitationStatus(InvitationStatus.ACCEPT)
                            .build()
            );
        }

        //when
        List<UserGroup> userGroups = userGroupRepository
                .findBySearchNameAndGroupId("테스트", groupId);

        //then
        assertThat(userGroups.size()).isEqualTo(list.size());

    }

    @DisplayName("그룹별 유저수, 그룹 아이디 조회")
    @Test
    void countByGroupEntity() {

        //given
        Random rand = new Random();
        HashMap<Long, Long> map = new HashMap<>();
        for (Group group : groupList) {
            ArrayList<User> userList = new ArrayList<>();
            for (int i = 0; i < rand.nextInt(5) + 1; i++) {
                userList.add(User.builder()
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
            userRepository.saveAll(userList);

            for (User user : userList) {
                userGroupRepository.save(
                        UserGroup.builder()
                                .user(user)
                                .group(group)
                                .invitationStatus(InvitationStatus.ACCEPT)
                                .userRole(UserRole.MEMBER)
                                .build()
                );
            }
            map.put(group.getId(), (long) userList.size() + 1);
        }
        //when
        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroup(groupList);

        //then
        for (CountUserGroupVO countUserGroupVO : countUserGroupVOS) {
            assertThat(map.get(countUserGroupVO.getId())).isEqualTo(countUserGroupVO.getCnt());
        }
    }
}
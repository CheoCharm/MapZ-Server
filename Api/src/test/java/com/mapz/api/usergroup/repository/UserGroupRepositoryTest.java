package com.mapz.api.usergroup.repository;

import com.mapz.api.RepositoryTest;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.mapz.domain.domains.usergroup.enums.InvitationStatus;
import com.mapz.domain.domains.usergroup.entity.UserGroup;
import com.mapz.domain.domains.usergroup.enums.UserRole;
import com.mapz.domain.domains.usergroup.repository.UserGroupRepository;
import com.mapz.domain.domains.usergroup.vo.ChiefUserImageVO;
import com.mapz.domain.domains.usergroup.vo.CountUserGroupVO;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.*;

@RepositoryTest
class UserGroupRepositoryTest {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private final EasyRandom easyRandom = new EasyRandom();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
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


    @Test
    @DisplayName("groupId를 조건으로 group에 속한 유저의 수를 조회한다.")
    void countUserByGroupIdFromUserGroupTable() {

        //given
        Group group = groupList.get(0);
        int randNumber = random.nextInt(10) + 1;
        for (int i = 0; i < randNumber; i++) {
            userGroupRepository.save(
                    UserGroup.of(
                            group,
                            userRepository.save(User.builder().id(i + 3L).build()),
                            InvitationStatus.ACCEPT,
                            UserRole.MEMBER
                    )
            );

        }
        //when
        Long userCount = userGroupRepository.countByGroupId(group.getId());

        //then
        assertThat(userCount).isEqualTo(randNumber + 1);
    }

    @Test
    @DisplayName("userId를 조건으로 그룹의 초대장들을 페이징하여 조회한다.")
    void getInvitationSliceByUserIdFromUserGroupTable() {

    }

    @Test
    @DisplayName("userId를 조건으로 Group의 Id를 조회한다.")
    void getGroupIdByUserIdFromUserGroupTable() {

        //given
        Long userId = user.getId();

        //when
        List<Long> groupIds = userGroupRepository.getGroupIdByUserId(userId);

        //then
        assertThat(groupIds.size()).isEqualTo(groupList.size());
    }

}
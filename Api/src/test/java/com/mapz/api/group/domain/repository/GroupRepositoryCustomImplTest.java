package com.mapz.api.group.domain.repository;

import com.mapz.api.RepositoryTest;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.group.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.GROUP_SIZE;
import static com.mapz.api.common.util.PagingUtils.applyCursorId;
import static com.mapz.api.common.util.PagingUtils.applyDescPageConfigBy;
import static org.assertj.core.api.Assertions.*;

@RepositoryTest
class GroupRepositoryCustomImplTest {

    @Autowired
    private GroupRepository groupRepository;


    @BeforeEach
    void beforeEach() {

    }

    @DisplayName("그룹명으로 그룹 조회한다. openStatus False이면 검색되지 않는다.")
    @Test
    void findByGroupName() {
        //given
        ArrayList<Group> list = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            list.add(
                    Group.builder()
                            .groupName(String.valueOf(i).concat("테스트"))
                            .bio("bio")
                            .groupImageUrl("imageUrl")
                            .openStatus(true)
                            .groupUUID(String.valueOf(i))
                            .build()
            );
        }
        groupRepository.saveAll(list);
        groupRepository.save(
                Group.builder()
                        .groupName(("17테스트"))
                        .bio("bio")
                        .groupImageUrl("imageUrl")
                        .openStatus(false)
                        .groupUUID("17")
                        .build()
        );

        //when
        Slice<Group> firstGroupEntitySlice = groupRepository.findByGroupName(
                "테스트",
                applyCursorId(0L),
                applyDescPageConfigBy(0, GROUP_SIZE, FIELD_CREATED_AT)
        );
        List<Group> firstContent = firstGroupEntitySlice.getContent();

        Slice<Group> secondEntitySlice = groupRepository.findByGroupName(
                "테스트",
                applyCursorId(firstContent.get(firstContent.size() - 1).getId()),
                applyDescPageConfigBy(1, GROUP_SIZE, FIELD_CREATED_AT)
        );
        List<Group> secondContent = secondEntitySlice.getContent();

        //then
        assertThat(firstContent.size()).isEqualTo(GROUP_SIZE);
        assertThat(secondContent.size()).isEqualTo(6);
        assertThat(secondEntitySlice.hasNext()).isFalse();
        assertThat(firstContent.stream()
                .filter(groupEntity -> !groupEntity.isOpenStatus())
                .findAny()
                .isEmpty()).isTrue();
        assertThat(secondContent.stream()
                .filter(groupEntity -> !groupEntity.isOpenStatus())
                .findAny()
                .isEmpty()).isTrue();

    }
}
package com.cheocharm.MapZ.common.fixtures;

import com.cheocharm.MapZ.group.domain.Group;

public class GroupFixtures {

    /* 검색가능한 그룹 */
    public static final String 그룹명 = "맵지그룹";
    public static final String 그룹소개 = "맵지의 대표 그룹입니다.";
    public static final String 그룹_UUID = "uuid";
    public static final Group openGroup = new Group(그룹명, 그룹소개, null, 그룹_UUID, true);

}

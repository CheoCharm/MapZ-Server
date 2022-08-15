package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long>, DiaryRepositoryCustom{
    List<DiaryEntity> findAllByUserEntityAndGroupEntity(UserEntity userEntity, GroupEntity groupEntity);
}

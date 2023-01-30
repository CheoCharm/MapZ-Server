package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.DiaryLikeEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLikeEntity, Long>, DiaryLikeRepositoryCustom {
    Optional<DiaryLikeEntity> findByDiaryEntityAndUserEntity(DiaryEntity diaryEntity, UserEntity userEntity);
}

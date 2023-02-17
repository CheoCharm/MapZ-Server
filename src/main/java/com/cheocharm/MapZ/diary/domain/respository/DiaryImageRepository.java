package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryImageRepository extends JpaRepository<DiaryImageEntity, Long>, DiaryImageRepositoryCustom {
}

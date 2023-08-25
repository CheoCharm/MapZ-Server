package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.diary.domain.DiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryImageRepository extends JpaRepository<DiaryImage, Long>, DiaryImageRepositoryCustom {
}

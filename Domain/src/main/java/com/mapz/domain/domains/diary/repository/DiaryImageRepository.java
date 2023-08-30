package com.mapz.domain.domains.diary.repository;

import com.mapz.domain.domains.diary.entity.DiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryImageRepository extends JpaRepository<DiaryImage, Long>, DiaryImageRepositoryCustom {
}

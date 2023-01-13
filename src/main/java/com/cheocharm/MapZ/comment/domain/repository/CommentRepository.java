package com.cheocharm.MapZ.comment.domain.repository;

import com.cheocharm.MapZ.comment.domain.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
}

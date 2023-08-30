package com.mapz.domain.domains.comment.repository;

import com.mapz.domain.domains.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom{
    @Modifying
    @Query("delete from Comment c where c.parentId in :id")
    void deleteAllByIdInQuery(@Param("id") Long id);
}

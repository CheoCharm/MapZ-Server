package com.cheocharm.MapZ.common.fixtures;

import com.cheocharm.MapZ.comment.domain.Comment;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.user.domain.User;

public class CommentFixtures {
    public static final String COMMENT_CONTENT_EXCEED_LIMIT = "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ" +
            "댓글 내용이 글자수 제한을 넘어부렸어요.아마도요..ㅎㅎ";

    public static final String VALID_COMMENT = "댓글을 작성함~!";

    public static Comment createComment(Long parentId, User user, Diary diary) {
        return Comment.builder()
                .content(VALID_COMMENT)
                .parentId(parentId)
                .user(user)
                .diary(diary)
                .build();
    }
}

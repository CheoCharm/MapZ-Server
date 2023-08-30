package com.mapz.domain.domains.diary.entity;


import com.mapz.domain.domains.comment.entity.Comment;
import com.mapz.domain.domains.BaseEntity;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.domain.domains.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Table(name = "Diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Diary extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Point point;

    private String address;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @OneToMany(mappedBy = "diary")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "diary")
    private List<DiaryLike> diaryLikes = new ArrayList<>();

    @OneToMany(mappedBy = "diary")
    private List<DiaryImage> diaryImages = new ArrayList<>();

    @Builder
    public Diary(Long id, String title, String content, Point point, String address, User user, Group group) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.point = point;
        this.address = address;
        this.user = user;
        this.group = group;
    }

    public static Diary of(User user, Group group, @NotNull String address, Point point) {
        return Diary.builder()
                .user(user)
                .group(group)
                .address(address)
                .point(point)
                .build();
    }

    public void write(String title, String content) {
        this.title = title;
        this.content = content;
    }
}

package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.exception.diary.AlreadyLikedDiaryException;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.dto.*;
import com.cheocharm.MapZ.diary.domain.respository.DiaryLikeRepository;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public void likeDiary(LikeDiaryDto likeDiaryDto) {
        final UserEntity userEntity = UserThreadLocal.get();
        final DiaryEntity diaryEntity = diaryRepository.findById(likeDiaryDto.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        diaryLikeRepository.findByDiaryEntityAndUserEntity(diaryEntity, userEntity)
                .ifPresent(diaryLikeEntity -> {
                    throw new AlreadyLikedDiaryException();
                });

        DiaryLikeEntity diaryLikeEntity = DiaryLikeEntity.builder()
                .diaryEntity(diaryEntity)
                .userEntity(userEntity)
                .build();

        diaryLikeRepository.save(diaryLikeEntity);
    }

    @Transactional
    public void writeDiary(WriteDiaryDto writeDiaryDto) {
        UserEntity userEntity = UserThreadLocal.get();

        GroupEntity groupEntity = groupRepository.findById(writeDiaryDto.getGroupId())
                .orElseThrow(NotFoundGroupException::new);

        String pointWKT = String.format("POINT(%s %s)", writeDiaryDto.getLatitude(), writeDiaryDto.getLongitude());

        try {
            Point point = (Point) new WKTReader().read(pointWKT);
            diaryRepository.save(
                    DiaryEntity.builder()
                            .userEntity(userEntity)
                            .groupEntity(groupEntity)
                            .title(writeDiaryDto.getTitle())
                            .content(writeDiaryDto.getContent())
                            .point(point)
                            .build()
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public GetDiaryListDto getDiary(Long groupId) {
        UserEntity userEntity = UserThreadLocal.get();
        List<DiaryEntity> diaryEntities = diaryRepository.findByUserIdAndGroupId(userEntity.getId(), groupId);


        List<GetDiaryListDto.DiaryList> list = diaryEntities.stream()
                .map(diaryEntity -> GetDiaryListDto.DiaryList.builder()
                        .x(diaryEntity.getPoint().getX())
                        .y(diaryEntity.getPoint().getY())
                        .diaryId(diaryEntity.getId())
                        .title(diaryEntity.getTitle())
                        .content(diaryEntity.getContent())
                        .build()
                )
                .collect(Collectors.toList());

        return new GetDiaryListDto(true, list);
    }

    @Transactional
    public void deleteDiary(DeleteDiaryDto deleteDiaryDto) {
        UserEntity userEntity = UserThreadLocal.get();

        if (!deleteDiaryDto.getUserId().equals(userEntity.getId())) {
            throw new NoPermissionUserException();
        }

        diaryRepository.deleteById(deleteDiaryDto.getDiaryId());

    }

    public List<DiaryLikePeopleDto> getDiaryLikePeople(Long diaryId) {
        List<DiaryLikeEntity> diaryLikeEntities = diaryLikeRepository.findByDiaryId(diaryId);

        return diaryLikeEntities.stream()
                .map(diaryLikeEntity -> {
                    UserEntity userEntity = diaryLikeEntity.getUserEntity();
                    return DiaryLikePeopleDto.builder()
                            .userImageUrl(userEntity.getUserImageUrl())
                            .username(userEntity.getUsername())
                            .build();
                    }
                )
                .collect(Collectors.toList());
    }
}

package com.cheocharm.MapZ.diary.application;

import com.cheocharm.MapZ.common.exception.common.FailParseException;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.DiaryImageEntity;
import com.cheocharm.MapZ.diary.domain.respository.vo.DiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.DiarySliceVO;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteTempDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryDetailResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.GetDiaryListResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.MyDiaryResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.cheocharm.MapZ.diary.domain.respository.DiaryImageRepository;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.diary.domain.respository.vo.MyDiaryVO;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryResponse;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static com.cheocharm.MapZ.common.util.PagingUtils.MY_DIARY_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;

@RequiredArgsConstructor
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final GroupRepository groupRepository;

    private final S3Utils s3Utils;

    @Transactional
    public WriteDiaryResponse writeDiary(WriteDiaryRequest writeDiaryRequest) {
        DiaryEntity diaryEntity = diaryRepository.findById(writeDiaryRequest.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        diaryEntity.write(writeDiaryRequest.getTitle(), writeDiaryRequest.getContent());

        return new WriteDiaryResponse(diaryEntity.getId());
    }

    @Transactional(readOnly = true)
    public GetDiaryListResponse getDiary(Long groupId, Long cursorId, Integer page) {
        UserEntity userEntity = UserThreadLocal.get();
        final Slice<DiarySliceVO> diarySlice = diaryRepository.getDiarySlice(
                userEntity.getId(),
                groupId,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_DIARY_SIZE, FIELD_CREATED_AT)
        );
        final List<DiarySliceVO> diaries = diarySlice.getContent();

        final List<GetDiaryListResponse.DiaryList> list = diaries.stream()
                .map(diarySliceVO -> new GetDiaryListResponse.DiaryList(
                        diarySliceVO.getDiaryId(),
                        diarySliceVO.getTitle(),
                        getTextFromContent(diarySliceVO.getContent()),
                        diarySliceVO.getAddress(),
                        diarySliceVO.getCreatedAt(),
                        diarySliceVO.getUsername(),
                        diarySliceVO.getUserImageURL(),
                        diarySliceVO.getLikeCount(),
                        diarySliceVO.isLike(),
                        diarySliceVO.getCommentCount(),
                        diarySliceVO.isWriter()
                ))
                .collect(Collectors.toList());

        return new GetDiaryListResponse(true, list);
    }

    private String getTextFromContent(String content) {
        String brReplaceRegex = "<br\\s*/?>";
        content = content.replaceAll(brReplaceRegex, " ");
        String htmlReplaceRegex = "<.*?>";
        return content.replaceAll(htmlReplaceRegex, "");
    }

    @Transactional
    public void deleteDiary(DeleteDiaryRequest deleteDiaryRequest) {
        UserEntity userEntity = UserThreadLocal.get();

        if (ObjectUtils.notEqual(deleteDiaryRequest.getUserId(), userEntity.getId())) {
            throw new NoPermissionUserException();
        }

        diaryRepository.deleteById(deleteDiaryRequest.getDiaryId());

    }

    @Transactional(readOnly = true)
    public MyDiaryResponse getMyDiary(Long cursorId, Integer page) {
        UserEntity userEntity = UserThreadLocal.get();

        Slice<MyDiaryVO> content = diaryRepository.findByUserId(
                userEntity.getId(),
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_DIARY_SIZE, FIELD_CREATED_AT)
        );
        List<MyDiaryVO> diaryVOS = content.getContent();

        List<MyDiaryResponse.Diary> diaryList = diaryVOS.stream()
                .map(myDiaryVO ->
                        MyDiaryResponse.Diary.builder()
                                .title(myDiaryVO.getTitle())
                                .createdAt(myDiaryVO.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                                .diaryId(myDiaryVO.getDiaryId())
                                .groupId(myDiaryVO.getGroupId())
                                .commentCount(myDiaryVO.getCommentCount())
                                .build()
                )
                .collect(Collectors.toList());

        return new MyDiaryResponse(content.hasNext(), diaryList);
    }

    @Transactional
    public WriteDiaryImageResponse writeDiaryImage(WriteDiaryImageRequest writeDiaryImageRequest, List<MultipartFile> files) {
        UserEntity userEntity = UserThreadLocal.get();

        GroupEntity groupEntity = groupRepository.findById(writeDiaryImageRequest.getGroupId())
                .orElseThrow(NotFoundGroupException::new);

        String pointWKT = String.format("POINT(%s %s)", writeDiaryImageRequest.getLatitude(), writeDiaryImageRequest.getLongitude());

        Point point;
        try {
            point = (Point) new WKTReader().read(pointWKT);
        } catch (ParseException e) {
            throw new FailParseException(e);
        }
        DiaryEntity diaryEntity = diaryRepository.save(
                DiaryEntity.builder()
                        .userEntity(userEntity)
                        .groupEntity(groupEntity)
                        .address(writeDiaryImageRequest.getAddress())
                        .point(point)
                        .build()
        );
        Long diaryId = diaryEntity.getId();

        List<String> imageURLs = s3Utils.uploadDiaryImage(files, diaryId);
        ArrayList<DiaryImageEntity> diaryImageEntities = new ArrayList<>();
        int imageOrder = 1;
        for (String imageURL : imageURLs) {
            diaryImageEntities.add(
                    DiaryImageEntity.builder()
                            .diaryEntity(diaryEntity)
                            .diaryImageUrl(imageURL)
                            .imageOrder(imageOrder)
                            .build()
            );
            imageOrder += 1;
        }
        diaryImageRepository.saveAll(diaryImageEntities);

        return new WriteDiaryImageResponse(diaryId, imageURLs);
    }

    @Transactional
    public void deleteTempDiary(DeleteTempDiaryRequest deleteTempDiaryRequest) {
        Long diaryId = deleteTempDiaryRequest.getDiaryId();
        List<String> diaryImageURLs = diaryImageRepository.findAllByDiaryId(diaryId);

        s3Utils.deleteImages(diaryImageURLs);
        diaryImageRepository.deleteAllByDiaryId(diaryId);
        diaryRepository.deleteById(diaryId);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiaryDetail(Long diaryId) {
        final UserEntity userEntity = UserThreadLocal.get();

        final DiaryDetailVO diaryDetail = diaryRepository.getDiaryDetail(diaryId, userEntity.getId());

        return new DiaryDetailResponse(
                diaryDetail.getTitle(),
                diaryDetail.getContent(),
                diaryDetail.getAddress(),
                diaryDetail.getCreatedAt(),
                diaryDetail.getUsername(),
                diaryDetail.getUserImageURL(),
                diaryDetail.getLikeCount(),
                diaryDetail.isLike(),
                diaryDetail.getCommentCount(),
                diaryDetail.isWriter()
        );

    }
}

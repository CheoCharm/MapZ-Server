package com.cheocharm.MapZ.diary.application;

import com.cheocharm.MapZ.common.exception.common.FailParseException;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.DiaryImageEntity;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryCoordinateVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryImagePreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryPreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiarySliceVO;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteTempDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryCoordinateResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryDetailResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryPreviewDetailResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryPreviewResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.GetDiaryListResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.MyDiaryResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.cheocharm.MapZ.diary.domain.repository.DiaryImageRepository;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.diary.domain.repository.vo.MyDiaryVO;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryResponse;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.domain.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
    private final UserGroupRepository userGroupRepository;

    private final S3Utils s3Utils;

    private static final int SEARCH_RADIUS_DISTANCE = 1_500;
    private static final int ZOOM_LEVEL_ONE_DISTANCE = 24_576_000;
    private static final int WGS84_STANDARD_SRID = 4_326;
    private static final int MAX_GROUP_COUNT = 2;
    private static final int MIN_ZOOM_LEVEL = 15;

    @Transactional
    public WriteDiaryResponse writeDiary(WriteDiaryRequest request) {
        DiaryEntity diaryEntity = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        diaryEntity.write(request.getTitle(), request.getContent());

        return WriteDiaryResponse.from(diaryEntity.getId());
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

        return GetDiaryListResponse.of(diarySlice.hasNext(), diaries);
    }

    @Transactional
    public void deleteDiary(DeleteDiaryRequest request) {
        UserEntity userEntity = UserThreadLocal.get();
        validateSameUser(request.getDiaryId(), userEntity.getId());
        diaryRepository.deleteById(request.getDiaryId());
    }

    private void validateSameUser(Long diaryId, Long userId) {
        final DiaryEntity diary = diaryRepository.findById(diaryId)
                .orElseThrow(NotFoundDiaryException::new);
        if (ObjectUtils.notEqual(diary.getUserEntity().getId(), userId)) {
            throw new NoPermissionUserException();
        }
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

        return MyDiaryResponse.of(content.hasNext(), diaryVOS);
    }

    @Transactional
    public WriteDiaryImageResponse writeDiaryImage(WriteDiaryImageRequest request, List<MultipartFile> files) {
        UserEntity userEntity = UserThreadLocal.get();
        GroupEntity groupEntity = groupRepository.findById(request.getGroupId())
                .orElseThrow(NotFoundGroupException::new);

        DiaryEntity diaryEntity = diaryRepository.save(
                DiaryEntity.of(
                        userEntity,
                        groupEntity,
                        request.getAddress(),
                        getPoint(request.getLongitude(), request.getLatitude())
                )
        );

        List<String> imageURLs = s3Utils.uploadDiaryImage(files, diaryEntity.getId());
        saveDiaryImages(diaryEntity, imageURLs);

        return new WriteDiaryImageResponse(diaryEntity.getId(), imageURLs, getImageName(files));
    }

    private void saveDiaryImages(DiaryEntity diaryEntity, List<String> imageURLs) {
        ArrayList<DiaryImageEntity> diaryImageEntities = new ArrayList<>();
        int imageOrder = 1;
        for (String imageURL : imageURLs) {
            diaryImageEntities.add(
                    DiaryImageEntity.of(diaryEntity, imageURL, imageOrder)
            );
            imageOrder += 1;
        }
        diaryImageRepository.saveAll(diaryImageEntities);
    }

    private List<String> getImageName(List<MultipartFile> files) {
        return files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTempDiary(DeleteTempDiaryRequest request) {
        Long diaryId = request.getDiaryId();
        List<String> diaryImageURLs = diaryImageRepository.findAllByDiaryId(diaryId);

        s3Utils.deleteImages(diaryImageURLs);
        diaryImageRepository.deleteAllByDiaryId(diaryId);
        diaryRepository.deleteById(diaryId);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiaryDetail(Long diaryId) {
        final UserEntity userEntity = UserThreadLocal.get();

        final DiaryDetailVO diaryDetail = diaryRepository.getDiaryDetail(diaryId, userEntity.getId());

        return DiaryDetailResponse.of(diaryDetail);
    }

    @Transactional(readOnly = true)
    public List<DiaryCoordinateResponse> getDiaryCoordinate(Double longitude, Double latitude) {
        final UserEntity userEntity = UserThreadLocal.get();

        final List<DiaryCoordinateVO> diaryCoordinateVOS = diaryRepository.findByDiaryCoordinate(
                getPoint(longitude, latitude),
                getGroupIds(userEntity.getId()),
                SEARCH_RADIUS_DISTANCE
        );
        return DiaryCoordinateResponse.from(diaryCoordinateVOS);
    }

    @Transactional(readOnly = true)
    public List<DiaryPreviewResponse> getDiaryByMap(Double longitude, Double latitude, Double zoomLevel) {
        final UserEntity userEntity = UserThreadLocal.get();

        final List<DiaryCoordinateVO> diaryCoordinateVOS = diaryRepository.findByDiaryCoordinate(
                getPoint(longitude, latitude),
                getGroupIds(userEntity.getId()),
                getDistance(zoomLevel)
        );

        final List<DiaryImagePreviewVO> previewImageVOS = getDiaryImagePreview(diaryCoordinateVOS);

        return DiaryPreviewResponse.of(previewImageVOS, diaryCoordinateVOS);
    }

    private double getDistance(Double zoomLevel) {
        if (zoomLevel <= MIN_ZOOM_LEVEL) { // 줌 레벨 1~15는 반경 1500을 넘어서 1500으로 고정
            return SEARCH_RADIUS_DISTANCE;
        }
        return ZOOM_LEVEL_ONE_DISTANCE / Math.pow(2, zoomLevel - 1);
    }

    private List<DiaryImagePreviewVO> getDiaryImagePreview(List<DiaryCoordinateVO> diaryCoordinateVOS) {
        final ArrayList<Long> diaryIds = new ArrayList<>();
        for (DiaryCoordinateVO diaryCoordinateVO : diaryCoordinateVOS) {
            diaryIds.add(diaryCoordinateVO.getDiaryId());
        }
        return diaryImageRepository.findPreviewImage(diaryIds);
    }

    @Transactional(readOnly = true)
    public DiaryPreviewDetailResponse getDiaryPreviewDetail(Long diaryId) {
        final UserEntity userEntity = UserThreadLocal.get();

        final List<DiaryPreviewVO> diaryPreviewVOS = diaryImageRepository.getDiaryPreview(diaryId, userEntity.getId());
        return DiaryPreviewDetailResponse.of(diaryPreviewVOS);
    }

    private List<Long> getGroupIds(Long userId) {
        List<Long> groupIds = userGroupRepository.getGroupIdByUserId(userId);
        if (groupIds.size() > MAX_GROUP_COUNT) {
            groupIds = getRandomGroupIds(groupIds);
        }
        return groupIds;
    }

    private List<Long> getRandomGroupIds(List<Long> groupIds) {
        final HashSet<Long> set = new HashSet<>();
        final ThreadLocalRandom current = ThreadLocalRandom.current();
        while (set.size() <= MAX_GROUP_COUNT) {
            set.add(current.nextLong(groupIds.size()));
        }
        return new ArrayList<>(set);
    }

    private Point getPoint(Double longitude, Double latitude) {
        final String pointWKT = String.format("POINT(%s %s)", longitude, latitude);
        try {
            Point point = (Point) new WKTReader().read(pointWKT);
            point.setSRID(WGS84_STANDARD_SRID);
            return point;
        } catch (ParseException e) {
            throw new FailParseException(e);
        }
    }
}

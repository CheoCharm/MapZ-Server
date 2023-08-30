package com.mapz.api.diary.application;

import com.mapz.api.common.exception.common.FailParseException;
import com.mapz.api.common.exception.diary.NotFoundDiaryException;
import com.mapz.api.common.exception.group.NotFoundGroupException;
import com.mapz.api.common.exception.user.NoPermissionUserException;
import com.mapz.api.common.image.ImageDirectory;
import com.mapz.api.common.image.ImageHandler;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.api.diary.presentation.dto.response.DiaryCoordinateResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryDetailResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryPreviewDetailResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryPreviewResponse;
import com.mapz.api.diary.presentation.dto.response.GetDiaryListResponse;
import com.mapz.api.diary.presentation.dto.response.MyDiaryResponse;
import com.mapz.api.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.mapz.api.diary.presentation.dto.response.WriteDiaryResponse;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.entity.DiaryImage;
import com.mapz.domain.domains.diary.vo.DiaryCoordinateVO;
import com.mapz.domain.domains.diary.vo.DiaryDetailVO;
import com.mapz.domain.domains.diary.vo.DiaryImagePreviewVO;
import com.mapz.domain.domains.diary.vo.DiaryPreviewVO;
import com.mapz.domain.domains.diary.vo.DiarySliceVO;
import com.mapz.api.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.mapz.api.diary.presentation.dto.request.WriteDiaryRequest;
import com.mapz.domain.domains.diary.repository.DiaryImageRepository;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.diary.vo.MyDiaryVO;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.usergroup.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.MY_DIARY_SIZE;
import static com.mapz.api.common.util.PagingUtils.applyCursorId;
import static com.mapz.api.common.util.PagingUtils.applyDescPageConfigBy;


@Slf4j
@RequiredArgsConstructor
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ImageHandler imageHandler;

    private static final int SEARCH_RADIUS_DISTANCE = 1_500;
    private static final int ZOOM_LEVEL_ONE_DISTANCE = 24_576_000;
    private static final int WGS84_STANDARD_SRID = 4_326;
    private static final int MAX_GROUP_COUNT = 2;
    private static final int MIN_ZOOM_LEVEL = 15;

    @Transactional
    public WriteDiaryResponse writeDiary(WriteDiaryRequest request) {
        Diary diary = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        diary.write(request.getTitle(), request.getContent());

        return WriteDiaryResponse.from(request.getDiaryId());
    }

    @Transactional(readOnly = true)
    public GetDiaryListResponse getDiary(Long groupId, Long cursorId, Integer page) {
        User user = UserThreadLocal.get();
        final Slice<DiarySliceVO> diarySlice = diaryRepository.getDiarySlice(
                user.getId(),
                groupId,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_DIARY_SIZE, FIELD_CREATED_AT)
        );
        final List<DiarySliceVO> diaries = diarySlice.getContent();

        return GetDiaryListResponse.of(diarySlice.hasNext(), diaries);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        User user = UserThreadLocal.get();
        validateSameUser(diaryId, user.getId());
        //Soft Delete or Hard Delete 로직 필요

        diaryRepository.deleteById(diaryId);
    }

    private void validateSameUser(Long diaryId, Long userId) {
        final Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(NotFoundDiaryException::new);
        if (ObjectUtils.notEqual(diary.getUser().getId(), userId)) {
            throw new NoPermissionUserException();
        }
    }

    @Transactional(readOnly = true)
    public MyDiaryResponse getMyDiary(Integer page, Long cursorId) {
        User user = UserThreadLocal.get();

        Slice<MyDiaryVO> content = diaryRepository.findByUserId(
                user.getId(),
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_DIARY_SIZE, FIELD_CREATED_AT)
        );
        List<MyDiaryVO> diaryVOS = content.getContent();

        return MyDiaryResponse.of(content.hasNext(), diaryVOS);
    }

    @Transactional
    public WriteDiaryImageResponse writeDiaryImage(WriteDiaryImageRequest request, List<MultipartFile> files) {
        User user = UserThreadLocal.get();
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(NotFoundGroupException::new);

        Diary diary = saveDiaryAndReturn(request, user, group);

        final List<CompletableFuture<String>> futures = new ArrayList<>();
        for (MultipartFile file : files) {
            futures.add(imageHandler.uploadImageAsync(file, ImageDirectory.DIARY));
        }

        final List<String> imageURLs = getImageURLs(futures);
        saveDiaryImages(diary, imageURLs);
        return WriteDiaryImageResponse.of(diary.getId(), imageURLs, files);
    }

    private Diary saveDiaryAndReturn(WriteDiaryImageRequest request, User user, Group group) {
        return diaryRepository.save(
                Diary.of(user, group, request.getAddress(), getPoint(request.getLongitude(), request.getLatitude()))
        );
    }

    private List<String> getImageURLs(List<CompletableFuture<String>> futures) {
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private void saveDiaryImages(Diary diary, List<String> imageURLs) {
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        int imageOrder = 1;
        for (String imageURL : imageURLs) {
            diaryImages.add(
                    DiaryImage.of(diary, imageURL, imageOrder)
            );
            imageOrder += 1;
        }
        diaryImageRepository.saveAll(diaryImages);
    }

    @Transactional
    public void deleteTempDiary(Long diaryId) {
        List<DiaryImage> diaryImages = diaryImageRepository.findAllByDiaryId(diaryId);

        deleteImages(diaryImages, diaryId);
        diaryRepository.deleteById(diaryId);
    }

    private void deleteImages(List<DiaryImage> diaryImages, Long diaryId) {
        for (DiaryImage diaryImage : diaryImages) {
            eventPublisher.publishEvent(diaryImage);
        }
        diaryImageRepository.deleteAllByDiaryId(diaryId);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiaryDetail(Long diaryId) {
        final User user = UserThreadLocal.get();

        final DiaryDetailVO diaryDetail = diaryRepository.getDiaryDetail(diaryId, user.getId());

        return DiaryDetailResponse.of(diaryDetail);
    }

    @Transactional(readOnly = true)
    public List<DiaryCoordinateResponse> getDiaryCoordinate(Double longitude, Double latitude) {
        final User user = UserThreadLocal.get();

        final List<DiaryCoordinateVO> diaryCoordinateVOS = diaryRepository.findByDiaryCoordinate(
                getPoint(longitude, latitude),
                getGroupIds(user.getId()),
                SEARCH_RADIUS_DISTANCE
        );
        return DiaryCoordinateResponse.from(diaryCoordinateVOS);
    }

    @Transactional(readOnly = true)
    public List<DiaryPreviewResponse> getDiaryByMap(Double longitude, Double latitude, Double zoomLevel) {
        final User user = UserThreadLocal.get();

        final List<DiaryCoordinateVO> diaryCoordinateVOS = diaryRepository.findByDiaryCoordinate(
                getPoint(longitude, latitude),
                getGroupIds(user.getId()),
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
        final User user = UserThreadLocal.get();

        final List<DiaryPreviewVO> diaryPreviewVOS = diaryImageRepository.getDiaryPreview(diaryId, user.getId());
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

package com.cheocharm.MapZ.diary.application;

import com.cheocharm.MapZ.common.exception.common.FailParseException;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.image.ImageHandler;
import com.cheocharm.MapZ.common.image.ImageDirectory;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.DiaryImage;
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
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.usergroup.domain.repository.UserGroupRepository;
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

import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static com.cheocharm.MapZ.common.util.PagingUtils.MY_DIARY_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;

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
    public void deleteDiary(DeleteDiaryRequest request) {
        User user = UserThreadLocal.get();
        validateSameUser(request.getDiaryId(), user.getId());
        //Soft Delete or Hard Delete 로직 필요

        diaryRepository.deleteById(request.getDiaryId());
    }

    private void validateSameUser(Long diaryId, Long userId) {
        final Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(NotFoundDiaryException::new);
        if (ObjectUtils.notEqual(diary.getUser().getId(), userId)) {
            throw new NoPermissionUserException();
        }
    }

    @Transactional(readOnly = true)
    public MyDiaryResponse getMyDiary(Long cursorId, Integer page) {
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
    public void deleteTempDiary(DeleteTempDiaryRequest request) {
        Long diaryId = request.getDiaryId();
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

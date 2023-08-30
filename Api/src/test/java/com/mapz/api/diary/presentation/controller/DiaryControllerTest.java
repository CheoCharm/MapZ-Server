package com.mapz.api.diary.presentation.controller;

import com.mapz.api.ControllerTest;
import com.mapz.api.common.fixtures.DiaryFixtures;
import com.mapz.api.common.fixtures.UserFixtures;
import com.mapz.api.diary.application.DiaryService;
import com.mapz.api.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.mapz.api.diary.presentation.dto.request.WriteDiaryRequest;
import com.mapz.api.diary.presentation.dto.response.DiaryCoordinateResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryDetailResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryPreviewDetailResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryPreviewResponse;
import com.mapz.api.diary.presentation.dto.response.GetDiaryListResponse;
import com.mapz.api.diary.presentation.dto.response.MyDiaryResponse;
import com.mapz.api.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.mapz.api.diary.presentation.dto.response.WriteDiaryResponse;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static com.mapz.api.common.fixtures.UserFixtures.VALID_EMAIL;
import static com.mapz.api.common.fixtures.UserFixtures.VALID_PASSWORD;
import static com.mapz.api.common.fixtures.UserFixtures.VALID_USERNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DiaryControllerTest extends ControllerTest {

    @MockBean
    private DiaryService diaryService;

    private static EasyRandom easyRandom = new EasyRandom();
    private static User user;

    @BeforeEach
    void beforeEach() {
        user = userRepository.save(
                User.builder()
                        .username(VALID_USERNAME)
                        .email(VALID_EMAIL)
                        .password(VALID_PASSWORD)
                        .refreshToken("refreshTokenValue")
                        .userProvider(UserProvider.MAPZ)
                        .build()
        );
    }

    @Test
    @DisplayName("일기 작성을 위해 이미지를 먼저 업로드 한다")
    void uploadImageForWriteDiary() throws Exception {

        //given
        String accessToken = getAccessToken();
        WriteDiaryImageRequest request = easyRandom.nextObject(WriteDiaryImageRequest.class);
        String dtoJson = objectMapper.writeValueAsString(request);
        MockMultipartFile dto = new MockMultipartFile(
                "dto", "dto", "application/json",
                dtoJson.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = getMockMultipartFile("files");
        given(diaryService.writeDiaryImage(any(), anyList()))
                .willReturn(new WriteDiaryImageResponse(
                        1L,
                        List.of("imageURL1", "ImageURL2"),
                        List.of("imageName1", "imageName2")
                ));
        //when, then
        mockMvc.perform(multipart("/api/diary/image")
                        .file(dto)
                        .file(file)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일기 작성을 완료하면 내용을 업데이트한다.")
    void writeDiary() throws Exception{

        //given
        String accessToken = getAccessToken();
        WriteDiaryRequest request = new WriteDiaryRequest("title", "content", 1L);
        given(diaryService.writeDiary(request))
                .willReturn(new WriteDiaryResponse(request.getDiaryId()));

        //when, then
        mockMvc.perform(post("/api/diary/write")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일기 작성 도중 작성을 취소하면 업로드했던 이미지를 삭제한다.")
    void deleteTempDiaryImage() throws Exception {

        //given
        String accessToken = getAccessToken();
        willDoNothing()
                .given(diaryService).deleteTempDiary(anyLong());

        //when, then
        mockMvc.perform(delete("/api/diary/image/{diaryId}", 1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("그룹에 속해 있는 일기들을 조회한다.")
    void getDiary() throws Exception {

        //given
        String accessToken = getAccessToken();
        GetDiaryListResponse response = new GetDiaryListResponse(false, Collections.emptyList());
        given(diaryService.getDiary(anyLong(), anyLong(), anyInt()))
                .willReturn(response);

        //when, then
        mockMvc.perform(get("/api/diary/{page}", 0)
                        .param("groupId", "1")
                        .param("cursorId", "0")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일기를 삭제한다.")
    void deleteDiary() throws Exception {

        //given
        String accessToken = getAccessToken();
        willDoNothing()
                .given(diaryService).deleteDiary(anyLong());

        //when, then
        mockMvc.perform(delete("/api/diary/{diaryId}", 1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내가 쓴 일기들을 조회한다.")
    void getMyDiary() throws Exception {

        //given
        String accessToken = getAccessToken();
        given(diaryService.getMyDiary(anyInt(), anyLong()))
                .willReturn(new MyDiaryResponse(
                        true,
                        List.of(
                                easyRandom.nextObject(MyDiaryResponse.Diary.class),
                                easyRandom.nextObject(MyDiaryResponse.Diary.class)
                        )
                ));

        //when, then
        mockMvc.perform(get("/api/diary/my/{page}", 1)
                        .param("cursorId", "21")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일기 하나에 대해 상세 조회를 할 수 있다.")
    void getDiaryDetail() throws Exception {

        //given
        String accessToken = getAccessToken();
        DiaryDetailResponse response = easyRandom.nextObject(DiaryDetailResponse.class);
        given(diaryService.getDiaryDetail(anyLong()))
                .willReturn(response);
        //when, then
        mockMvc.perform(get("/api/diary/detail/{diaryId}",1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일기 줌 레벨이 낮은 경우 다이어리 ID, 위도,경도 정보를 받는다.")
    void getDiaryCoordinate() throws Exception {

        //given
        String accessToken = getAccessToken();
        given(diaryService.getDiaryCoordinate(anyDouble(), anyDouble())).willReturn(List.of(
                easyRandom.nextObject(DiaryCoordinateResponse.class),
                easyRandom.nextObject(DiaryCoordinateResponse.class)
        ));

        //when, then
        mockMvc.perform(get("/api/diary/low")
                        .param("longitude", DiaryFixtures.CURRENT_PLACE_LONGITUDE)
                        .param("latitude", DiaryFixtures.CURRENT_PLACE_LATITUDE)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일기 줌 레벨이 높은 경우 다이어리 ID, 대표이미지, 위도, 경도 정보를 받는다.")
    void getDiaryCoordinateWithTitleImage() throws Exception {

        //given
        String accessToken = getAccessToken();
        given(diaryService.getDiaryByMap(anyDouble(), anyDouble(), anyDouble()))
                .willReturn(List.of(
                        easyRandom.nextObject(DiaryPreviewResponse.class),
                        easyRandom.nextObject(DiaryPreviewResponse.class)
                ));
        //when, then
        mockMvc.perform(get("/api/diary/high")
                        .param(DiaryFixtures.LONGITUDE_PARAM_NAME, DiaryFixtures.CURRENT_PLACE_LONGITUDE)
                        .param(DiaryFixtures.LATITUDE_PARAM_NAME, DiaryFixtures.CURRENT_PLACE_LATITUDE)
                        .param(DiaryFixtures.ZOOM_LEVEL_PARAM_NAME, DiaryFixtures.CURRENT_ZOOM_LEVEL)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지도에서 일기를 클릭해 정보를 조회한다.")
    void getDiaryPreviewDetail() throws Exception {

        //given
        String accessToken = getAccessToken();

        given(diaryService.getDiaryPreviewDetail(anyLong()))
                .willReturn(new DiaryPreviewDetailResponse(
                        Collections.emptyList(),
                        "address",
                        true
                ));
        //when,then
        mockMvc.perform(get("/api/diary/preview/{diaryId}", 1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}
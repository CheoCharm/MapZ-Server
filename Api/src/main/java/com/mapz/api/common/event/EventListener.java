package com.mapz.api.common.event;

import com.mapz.api.common.image.ImageHandler;
import com.mapz.domain.domains.diary.entity.DiaryImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class EventListener {

    private final ImageHandler imageHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void deleteS3ImageAfterCommit(DiaryImage diaryImage) {
        imageHandler.deleteImage(diaryImage.getDiaryImageUrl());
    }
}

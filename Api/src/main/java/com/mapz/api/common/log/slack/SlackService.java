package com.mapz.api.common.log.slack;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import com.slack.api.webhook.WebhookPayloads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SlackService {

    @Value("${webhook.slack.url}")
    private String SLACK_WEBHOOK_URL;

    private final Logger logger = LoggerFactory.getLogger(SlackService.class);

    private final Slack slackClient = Slack.getInstance();
    private static final String BASE_TEXT = "예외 발생";
    private static final String REQUEST_URL = "Request URL";
    private static final String REQUEST_METHOD = "Request Method";
    private static final String EXCEPTION_NAME = "Exception Class Name";
    private static final String EXCEPTION_MESSAGE = "Exception Message";
    private static final String EXCEPTION_CAUSE = "Exception Cause";
    private static final String ATTACHMENT_COLOR_WARNING = "warning";

    private static final String FAIL_MESSAGE = "슬랙 알림 전달 실패";

    @Async
    public void sendExceptionMessage(Exception exception, HttpServletRequest httpServletRequest) {
        try {
            slackClient.send(SLACK_WEBHOOK_URL, WebhookPayloads.payload(payloadBuilder -> payloadBuilder
                            .text(BASE_TEXT)
                            .attachments(
                                    List.of(createAttachment(exception, httpServletRequest))
                            )
                    )
            );
        } catch (IOException ioException) {
            logger.error(FAIL_MESSAGE, ioException);
        }
    }

    @Async
    public void sendExceptionMessageWithCause(Exception exception, HttpServletRequest httpServletRequest, Throwable cause) {
        try {
            slackClient.send(SLACK_WEBHOOK_URL, WebhookPayloads.payload(payloadBuilder -> payloadBuilder
                            .text(BASE_TEXT)
                            .attachments(
                                    List.of(createAttachmentWithCause(exception, httpServletRequest, cause))
                            )
                    )
            );
        } catch (IOException ioException) {
            logger.error(FAIL_MESSAGE, ioException);
        }
    }

    private Attachment createAttachment(Exception exception, HttpServletRequest httpServletRequest) {
        return Attachment.builder()
                .color(ATTACHMENT_COLOR_WARNING)
                .title(LocalDateTime.now().toString())
                .fields(List.of(
                        createField(REQUEST_URL, httpServletRequest.getRequestURI()),
                        createField(REQUEST_METHOD, httpServletRequest.getMethod()),
                        createField(EXCEPTION_NAME, exception.getClass().getName()),
                        createField(EXCEPTION_MESSAGE, exception.getMessage())
                ))
                .build();
    }
    private Attachment createAttachmentWithCause(Exception exception, HttpServletRequest httpServletRequest, Throwable cause) {
        return Attachment.builder()
                .color(ATTACHMENT_COLOR_WARNING)
                .title(LocalDateTime.now().toString())
                .fields(List.of(
                        createField(REQUEST_URL, httpServletRequest.getRequestURI()),
                        createField(REQUEST_METHOD, httpServletRequest.getMethod()),
                        createField(EXCEPTION_NAME, exception.getClass().getName()),
                        createField(EXCEPTION_MESSAGE, exception.getMessage()),
                        createField(EXCEPTION_CAUSE, cause.toString())
                ))
                .build();
    }

    private Field createField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .build();
    }
}

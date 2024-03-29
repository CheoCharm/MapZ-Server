package com.mapz.api.common.log.response;

import com.mapz.api.common.CommonResponse;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Component
@Aspect
public class ResponseLogAspect {
    private final Logger logger = LoggerFactory.getLogger(ResponseLogAspect.class);

    @Pointcut("execution(* com.mapz.api.user.presentation.controller.UserController.*(..))")
    public void userControllerPointcut() {}

    @Pointcut("execution(* com.mapz.api.diary.presentation.controller.DiaryController.*(..))")
    public void diaryControllerPointcut() {}

    @Pointcut("execution(* com.mapz.api.group.presentation.controller.GroupController.*(..))")
    public void groupControllerPointcut() {}

    @Pointcut("execution(* com.mapz.api.like.presentation.controller.LikeController.*(..))")
    public void likeControllerPointcut() {}

    @AfterReturning(pointcut = "userControllerPointcut() || diaryControllerPointcut() || groupControllerPointcut() || likeControllerPointcut()", returning = "result")
    public void userResponseLog(Object result) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        CommonResponse response = (CommonResponse) result;

        ResponseLogSchema logSchema = ResponseLogSchema.createLogSchema(request, response);

        logger.info("{}", logSchema.toString());
    }

}

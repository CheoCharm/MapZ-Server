package com.cheocharm.MapZ.common.log.response;

import com.cheocharm.MapZ.common.CommonResponse;
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

    @Pointcut("execution(* com.cheocharm.MapZ.user.presentation.controller.UserController.*(..))")
    public void userControllerPointcut() {}

    @Pointcut("execution(* com.cheocharm.MapZ.diary.presentation.DiaryController.*(..))")
    public void diaryControllerPointcut() {}

    @Pointcut("execution(* com.cheocharm.MapZ.group.presentation.controller.GroupController.*(..))")
    public void groupControllerPointcut() {}

    @Pointcut("execution(* com.cheocharm.MapZ.like.presentation.controller.LikeController.*(..))")
    public void likeControllerPointcut() {}

    @AfterReturning(pointcut = "userControllerPointcut() || diaryControllerPointcut() || groupControllerPointcut() || likeControllerPointcut()", returning = "result")
    public void userResponseLog(Object result) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        CommonResponse response = (CommonResponse) result;

        ResponseLogSchema logSchema = ResponseLogSchema.createLogSchema(request, response);

        logger.info("{}", logSchema.toString());
    }

}

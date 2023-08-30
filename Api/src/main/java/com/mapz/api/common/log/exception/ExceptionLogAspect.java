package com.mapz.api.common.log.exception;

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
public class ExceptionLogAspect {
    private final Logger logger = LoggerFactory.getLogger(ExceptionLogAspect.class);

    @Pointcut("execution(* com.mapz.api.common.exception.GlobalExceptionHandler.*(..))")
    public void exceptionPointcut() {}

    @AfterReturning(pointcut = "exceptionPointcut()", returning = "result")
    public void customExceptionLog(Object result) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        CommonResponse response = (CommonResponse) result;

        ExceptionLogSchema logSchema = ExceptionLogSchema.createLogSchema(request, response);

        logger.error("{}", logSchema.toString());

    }

}

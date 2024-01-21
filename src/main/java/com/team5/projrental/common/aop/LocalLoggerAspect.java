package com.team5.projrental.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@Slf4j
@Profile({"local", "hyunmin"})
public class LocalLoggerAspect {
    private long startTime;
    private long endTime;

    @Pointcut("execution(* com.team5.projrental..*Controller*.*(..))")
    public void controller() {
    }

    @Pointcut("execution(* com.team5.projrental..*Service*.*(..))")
    public void service() {
    }

    @Pointcut("execution(* com.team5.projrental..*Repository*.*(..))")
    public void repository() {
    }

    @Pointcut("execution(* com.team5.projrental..*Mapper*.*(..))")
    public void mapper() {
    }

    @Before("controller() || service() || repository() || mapper()")
    public void beforeAll(JoinPoint joinPoint) {


        log.info("\n\nCALL\n\t{} \n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tㄴ> {} \nARGS \n\t{}\nON\n\t{}\n",
                joinPoint.getSignature(),
                joinPoint.getTarget(),
                joinPoint.getArgs(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        this.startTime = System.currentTimeMillis();
    }

    @AfterReturning(value = "controller() || service() || repository() || mapper()",
            returning = "returnVal")
    public void afterReturn(JoinPoint joinPoint, Object returnVal) {
        boolean flag = false;
        if (joinPoint.getSignature().getDeclaringTypeName().contains("ontroller")) {
            this.endTime = System.currentTimeMillis();
            flag = true;
        }
        log.info("\nRETURN \n\t{}\nRETURN VAL \n\t{}\n", joinPoint.getSignature(), returnVal);
        if (flag) {
            log.info("\nDURATION \n\t{}ms", this.endTime - this.startTime);
        }
    }


}

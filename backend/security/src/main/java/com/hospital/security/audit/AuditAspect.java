package com.hospital.security.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.Instant;

import com.hospital.security.audit.AuditService;
import com.hospital.security.audit.AuditEvent;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    // Constructor injection preferred (helps IDEs/LSP and makes the class easier to test)
    public AuditAspect(final AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(com.hospital.security.audit.Audited) || @within(com.hospital.security.audit.Audited)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) request = attrs.getRequest();
        } catch (Exception e) {
            // ignore
        }

        String user = "anonymous"; // TODO: extract from security context
        String ip = request != null ? request.getRemoteAddr() : "unknown";
        String action = joinPoint.getSignature().toShortString();
        String details = "";

        AuditEvent event = new AuditEvent(user, action, Instant.now(), ip, details);
        auditService.log(event);

        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            // Optionally log error in audit trail
            auditService.log(new AuditEvent(user, action + "::error", Instant.now(), ip, t.getMessage()));
            throw t;
        }
    }
}

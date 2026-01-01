package com.hospital.common.config;

/**
* Small helper for auditing integration. The common module provides a reflective
* helper to obtain the current auditor name without depending on Spring Data JPA
* or Spring Security at compile time. Services that enable JPA auditing should
* provide their own {@code AuditorAware<String>} bean that delegates to
* {@link AuditingUtils#currentAuditor()}.
*/
final class AuditingUtils {

  private AuditingUtils() {
  }

  public static String currentAuditor() {
    try {
      final Object auth = getAuthenticationReflective();
      if (auth == null) {
        return "system";
      }
      try {
        final Object name = auth.getClass().getMethod("getName").invoke(auth);
        if (name instanceof String s) {
          return s;
        }
      } catch (NoSuchMethodException ignored) {
        // noop
      }
      return "system";
    } catch (ReflectiveOperationException ex) {
      return "system";
    }
  }

  private static Object getAuthenticationReflective() {
    try {
      final Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
      final Object ctx = holder.getMethod("getContext").invoke(null);
      if (ctx == null) {
        return null;
      }
      return ctx.getClass().getMethod("getAuthentication").invoke(ctx);
    } catch (ClassNotFoundException cnf) {
      return null;
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }
}

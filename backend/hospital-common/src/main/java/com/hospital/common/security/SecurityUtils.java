package com.hospital.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common security utilities used by services. Lightweight, static helpers only.
 * <p>
 * Encryption helpers require an environment variable `HOSPITAL_ENCRYPTION_KEY` to be set
 * (it will be hashed to 256 bits). No keys are hardcoded here.
 */
public final class SecurityUtils {
  private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private SecurityUtils() {
  }

  public static Optional<String> getCurrentUsername() {
    try {
      final Object auth = getAuthenticationReflective();
      if (auth == null) {
        return Optional.empty();
      }
      try {
        final Object name = auth.getClass().getMethod("getName").invoke(auth);
        if (name instanceof String s) {
          return Optional.of(s);
        }
      } catch (NoSuchMethodException ignored) {
        // noop
      }
      try {
        final Object principal = auth.getClass().getMethod("getPrincipal").invoke(auth);
        if (principal instanceof String p) {
          return Optional.of(p);
        }
      } catch (NoSuchMethodException ignored) {
        // noop
      }
      return Optional.empty();
    } catch (ReflectiveOperationException ex) {
      return Optional.empty();
    }
  }

  public static boolean isAuthenticated() {
    try {
      final Object auth = getAuthenticationReflective();
      if (auth == null) {
        return false;
      }
      try {
        final Object isAuthenticated = auth.getClass().getMethod("isAuthenticated").invoke(auth);
        if (isAuthenticated instanceof Boolean b) {
          return b;
        }
      } catch (NoSuchMethodException ignored) {
        // noop
      }
      return false;
    } catch (ReflectiveOperationException ex) {
      return false;
    }
  }

  public static String hashPassword(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  public static boolean matchesPassword(String rawPassword, String encodedPassword) {
    if (rawPassword == null || encodedPassword == null) {
      return false;
    }
    return BCrypt.checkpw(rawPassword, encodedPassword);
  }

  public static List<String> getCurrentAuthorities() {
    try {
      final Object auth = getAuthenticationReflective();
      if (auth == null) {
        return List.of();
      }
      try {
        final Object authorities = auth.getClass().getMethod("getAuthorities").invoke(auth);
        if (authorities instanceof Iterable<?> iterable) {
          final List<String> result = StreamSupport.stream(iterable.spliterator(), false)
              .map(o -> {
                try {
                  final Object a = o.getClass().getMethod("getAuthority").invoke(o);
                  if (a instanceof String s) return s;
                  return null;
                } catch (ReflectiveOperationException e) {
                  return null;
                }
              })
              .filter(s -> s != null)
              .collect(Collectors.toList());
          return result;
        }
      } catch (NoSuchMethodException ignored) {
        // noop
      }
      return List.of();
    } catch (ReflectiveOperationException ex) {
      return List.of();
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

  public static boolean hasRole(String role) {
    if (role == null) {
      return false;
    }
    return getCurrentAuthorities().stream()
        .anyMatch(a -> a.equals(role) || a.equals("ROLE_" + role));
  }

  /**
  * Encrypts the input text using AES-GCM with a key derived from environment variable
  * HOSPITAL_ENCRYPTION_KEY. The resulting payload is Base64(iv||ciphertext).
  *
  * @param plain plaintext to encrypt
  * @return Base64 encoded iv||ciphertext
  */
  public static String encrypt(String plain) {
    try {
      final byte[] key = deriveKeyFromEnv();
      final byte[] iv = new byte[GCM_IV_LENGTH];
      RANDOM.nextBytes(iv);

      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
      final GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
      final byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

      final byte[] out = new byte[iv.length + ct.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(ct, 0, out, iv.length, ct.length);
      return Base64.getEncoder().encodeToString(out);
    } catch (java.security.GeneralSecurityException ex) {
      log.error("Encryption failed", ex);
      throw new RuntimeException("Encryption failed", ex);
    }
  }

  /**
  * Decrypts the Base64(iv||ciphertext) produced by {@link #encrypt(String)}.
  *
  * @param base64 Base64 encoded iv||ciphertext
  * @return decrypted plaintext
  */
  public static String decrypt(String base64) {
    try {
      final byte[] key = deriveKeyFromEnv();
      final byte[] data = Base64.getDecoder().decode(base64);
      if (data.length < GCM_IV_LENGTH + 1) {
        throw new IllegalArgumentException("Invalid cipher payload");
      }
      final byte[] iv = new byte[GCM_IV_LENGTH];
      System.arraycopy(data, 0, iv, 0, iv.length);
      final byte[] ct = new byte[data.length - iv.length];
      System.arraycopy(data, iv.length, ct, 0, ct.length);

      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
      final GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
      final byte[] plain = cipher.doFinal(ct);
      return new String(plain, StandardCharsets.UTF_8);
    } catch (java.security.GeneralSecurityException ex) {
      log.error("Decryption failed", ex);
      throw new RuntimeException("Decryption failed", ex);
    }
  }

  private static byte[] deriveKeyFromEnv() {
    try {
      final String raw = System.getenv("HOSPITAL_ENCRYPTION_KEY");
      if (raw == null || raw.isBlank()) {
        throw new IllegalStateException("Environment variable HOSPITAL_ENCRYPTION_KEY is not set");
      }
      final MessageDigest sha = MessageDigest.getInstance("SHA-256");
      return sha.digest(raw.getBytes(StandardCharsets.UTF_8));
    } catch (java.security.NoSuchAlgorithmException ex) {
      log.error("Failed to derive encryption key from environment", ex);
      throw new RuntimeException(ex);
    }
  }
}

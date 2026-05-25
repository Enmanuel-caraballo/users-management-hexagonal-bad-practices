package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

@Log
@RequiredArgsConstructor
public final class EmailNotificationService {

  private static final String SUBJECT_CREATED = "Tu cuenta ha sido creada — Gestión de Usuarios";
  private static final String SUBJECT_UPDATED =
      "Tu cuenta ha sido actualizada — Gestión de Usuarios";

  private static final String TOKEN_NAME     = "name";
  private static final String TOKEN_EMAIL    = "email";
  private static final String TOKEN_PASSWORD = "password";
  private static final String TOKEN_ROLE     = "role";
  private static final String TOKEN_STATUS   = "status";

  private final EmailSenderPort emailSenderPort;

  public void notifyUserCreated(final UserModel user, final String plainPassword) {
    sendNotification(user, SUBJECT_CREATED, "user-created.html", buildCreatedTokens(user, plainPassword));
  }

  public void notifyUserUpdated(final UserModel user) {
    sendNotification(user, SUBJECT_UPDATED, "user-updated.html", buildUpdatedTokens(user));
  }

  private void sendNotification(
      final UserModel user,
      final String subject,
      final String templateName,
      final Map<String, String> tokens) {
    final String body = renderTemplate(loadTemplate(templateName), tokens);
    sendEmail(buildDestination(user, subject, body));
  }

  private static Map<String, String> buildCreatedTokens(
      final UserModel user, final String plainPassword) {
    return Map.of(
        TOKEN_NAME,     user.getName().value(),
        TOKEN_EMAIL,    user.getEmail().value(),
        TOKEN_PASSWORD, plainPassword,
        TOKEN_ROLE,     user.getRole().name());
  }

  private static Map<String, String> buildUpdatedTokens(final UserModel user) {
    return Map.of(
        TOKEN_NAME,   user.getName().value(),
        TOKEN_EMAIL,  user.getEmail().value(),
        TOKEN_ROLE,   user.getRole().name(),
        TOKEN_STATUS, user.getStatus().name());
  }

  private static EmailDestinationModel buildDestination(
      final UserModel user, final String subject, final String body) {
    return new EmailDestinationModel(
        user.getEmail().value(), user.getName().value(), subject, body);
  }

  private String loadTemplate(final String templateName) {
    final String path = "/templates/" + templateName;
    try (InputStream inputStream = openResourceStream(path)) {
      if (Objects.isNull(inputStream)) {
        throw EmailSenderException.becauseSendFailed(
            new IllegalStateException("Template not found: " + path));
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException ioException) {
      throw EmailSenderException.becauseSendFailed(ioException);
    }
  }

  InputStream openResourceStream(final String path) {
    return getClass().getResourceAsStream(path);
  }

  private static String renderTemplate(String template, final Map<String, String> values) {
    String result = template;
    for (final Map.Entry<String, String> tokenEntry : values.entrySet()) {
      final String token = "{{" + tokenEntry.getKey() + "}}";
      result = result.replace(token, tokenEntry.getValue());
    }
    return result;
  }

  private void sendEmail(final EmailDestinationModel destination) {
    try {
      emailSenderPort.send(destination);
    } catch (final EmailSenderException senderException) {
      log.log(
          Level.WARNING,
          "[EmailNotificationService] No se pudo enviar correo. Causa: {0}",
          new Object[] {senderException.getMessage()});
      throw senderException;
    }
  }
}

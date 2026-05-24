package com.jcaa.usersmanagement.domain.model;

import lombok.Value;

@Value
public class EmailDestinationModel {

  String destinationEmail;
  String destinationName;
  String subject;
  String body;

  private static final String ERR_EMAIL_REQ = "El email del destinatario es requerido.";
  private static final String ERR_NAME_REQ = "El nombre del destinatario es requerido.";
  private static final String ERR_SUBJECT_REQ = "El asunto es requerido.";
  private static final String ERR_BODY_REQ = "El cuerpo del mensaje es requerido.";

  public EmailDestinationModel(
      final String destinationEmail,
      final String destinationName,
      final String subject,
      final String body) {
    this.destinationEmail = validateNotBlank(destinationEmail, ERR_EMAIL_REQ);
    this.destinationName  = validateNotBlank(destinationName,  ERR_NAME_REQ);
    this.subject          = validateNotBlank(subject,          ERR_SUBJECT_REQ);
    this.body             = validateNotBlank(body,             ERR_BODY_REQ);
  }

  private static String validateNotBlank(final String value, final String errorMessage) {
    java.util.Objects.requireNonNull(value, errorMessage);
    if (value.trim().isEmpty()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return value;
  }
}

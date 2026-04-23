package com.jcaa.usersmanagement.domain.exception;

public final class UserAlreadyExistsException extends DomainException {

  private UserAlreadyExistsException(final String message) {
    super(message);
  }
  private static final String EMAIL_ALREADY_EXIST_MSG = "Already exists an user with this email.";
  public static UserAlreadyExistsException becauseEmailAlreadyExists() {
    // VIOLACIÓN Regla 10: texto de error hardcodeado directamente en el método fábrica.
    // Debe usarse una constante con nombre descriptivo en lugar de un String literal.
    return new UserAlreadyExistsException(EMAIL_ALREADY_EXIST_MSG);

  }
}

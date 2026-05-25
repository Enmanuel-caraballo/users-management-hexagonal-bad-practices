package com.jcaa.usersmanagement.domain.exception;

public final class InvalidCredentialsException extends DomainException {

  private static final String INVALID_CREDENTIALS_MSG = "Correo o contraseña incorrectos.";
  private static final String USER_NOT_ACTIVE_MSG =
      "Tu cuenta no está activa. Contacta al administrador.";

  private InvalidCredentialsException(final String message) {
    super(message);
  }

  public static InvalidCredentialsException becauseCredentialsAreInvalid() {
    return new InvalidCredentialsException(INVALID_CREDENTIALS_MSG);
  }

  public static InvalidCredentialsException becauseUserIsNotActive() {
    return new InvalidCredentialsException(USER_NOT_ACTIVE_MSG);
  }
}

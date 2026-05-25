package com.jcaa.usersmanagement.domain.exception;

public final class InvalidUserEmailException extends DomainException {

  private static final String EMPTY_MSG = "The user email must not be empty.";
  private static final String INVALID_FORMAT_TEMPLATE = "The user email format is invalid: '%s'.";

  private InvalidUserEmailException(final String message) {
    super(message);
  }

  public static InvalidUserEmailException becauseValueIsEmpty() {
    return new InvalidUserEmailException(EMPTY_MSG);
  }

  public static InvalidUserEmailException becauseFormatIsInvalid(final String email) {
    return new InvalidUserEmailException(String.format(INVALID_FORMAT_TEMPLATE, email));
  }
}

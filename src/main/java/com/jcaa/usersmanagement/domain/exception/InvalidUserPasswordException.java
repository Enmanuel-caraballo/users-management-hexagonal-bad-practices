package com.jcaa.usersmanagement.domain.exception;

public final class InvalidUserPasswordException extends DomainException {

  private static final String EMPTY_MSG = "The user password must not be empty.";
  private static final String TOO_SHORT_TEMPLATE =
      "The user password must have at least %d characters.";

  private InvalidUserPasswordException(final String message) {
    super(message);
  }

  public static InvalidUserPasswordException becauseValueIsEmpty() {
    return new InvalidUserPasswordException(EMPTY_MSG);
  }

  public static InvalidUserPasswordException becauseLengthIsTooShort(final int minimumLength) {
    return new InvalidUserPasswordException(String.format(TOO_SHORT_TEMPLATE, minimumLength));
  }
}

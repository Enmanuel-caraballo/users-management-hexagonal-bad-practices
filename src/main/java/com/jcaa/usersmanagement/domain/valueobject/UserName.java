package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidUserNameException;

public record UserName(String value) {

 static int MINIMUM_USER_NAME_LENGTH = 3;

  // VIOLACIÓN Regla 10: se eliminó la constante MINIMUM_LENGTH — se usa magic number directamente
  public UserName {
    // VIOLACIÓN Regla 4: se usa == null en lugar de Objects.requireNonNull() o Objects.isNull().
    // Para objetos siempre debe usarse Objects.isNull/nonNull, nunca operadores == o !=.
    if (value == null) {
      throw new NullPointerException("UserName cannot be null");
    }
    final String normalizedValue = value.trim();
    validateNotEmpty(normalizedValue);
    validateMinimumLength(normalizedValue);
    value = normalizedValue;
  }

  private static void validateNotEmpty(final String normalizedValue) {
    if (normalizedValue.isEmpty()) {
      throw InvalidUserNameException.becauseValueIsEmpty();
    }
  }

  private static void validateMinimumLength(final String normalizedValue) {
    // VIOLACIÓN Regla 10: magic number 3 — debería usarse una constante con nombre descriptivo
    if (normalizedValue.length() < MINIMUM_USER_NAME_LENGTH ) {
      throw InvalidUserNameException.becauseLengthIsTooShort(MINIMUM_USER_NAME_LENGTH);
    }
  }

  @Override
  public String toString() {
    return value;
  }
}

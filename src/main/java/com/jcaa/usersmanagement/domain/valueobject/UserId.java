package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidUserIdException;

import java.util.Objects;

public record UserId(String value) {

  public UserId {
    // VIOLACIÓN Regla 4: se usa == null en lugar de Objects.requireNonNull() o Objects.isNull().
    // Para objetos siempre debe usarse Objects.isNull/nonNull, nunca operadores == o !=.
//    if (Objects.isNull(value)) {
//      throw new NullPointerException("UserId cannot be null");
//    }

    Objects.requireNonNull(value,"UserId cannot be null");

    final String normalizedValue = value.trim();
    validateNotEmpty(normalizedValue);
    // asigna el valor normalizado al componente
    value = normalizedValue;
  }

  private static void validateNotEmpty(final String normalizedValue) {
    if (normalizedValue.isEmpty()) {
      throw InvalidUserIdException.becauseValueIsEmpty();
    }
  }

  @Override
  public String toString() {
    return value;
  }
}

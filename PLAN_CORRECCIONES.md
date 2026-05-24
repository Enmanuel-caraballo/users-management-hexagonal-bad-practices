# Plan de correcciones — users-management-hexagonal-bad-practices

Cada sección corresponde a **un commit / una rama**. El flujo de trabajo para cada una es:

```
git pull origin main
git switch -c fix/reglaX/violacionY
# hacer los cambios descritos
mvn compile
mvn test          # deben pasar 193 tests
git add <archivos>
git commit -m "fix/reglaX/violacionY: descripción"
git push -u origin fix/reglaX/violacionY
git switch main
git merge fix/reglaX/violacionY
```

---

## Tabla 1 — Reglas de arquitectura y estilo Java

---

### Rama: `fix/regla1-2-3/violacion1`
**Archivo:** `src/main/java/.../application/port/out/` (los 6 puertos)
**Qué hacer:** Eliminar los métodos `static` con cuerpo de todas las interfaces de puerto de salida. Las interfaces sólo deben declarar el contrato abstracto.

**Archivos afectados y cambio:**

`DeleteUserPort.java` — eliminar el método estático `delete(UserId)` con cuerpo (el que lanzaba `UnsupportedOperationException`).

`GetAllUsersPort.java` — eliminar el método estático `getAll()` con cuerpo.

`GetUserByEmailPort.java` — eliminar el método estático `getByEmail(UserEmail)` con cuerpo.

`GetUserByIdPort.java` — eliminar el método estático `getById(UserId)` con cuerpo.

`SaveUserPort.java` — eliminar el método estático `save(UserModel)` con cuerpo.

`UpdateUserPort.java` — eliminar el método estático `update(UserModel)` con cuerpo.

**Resultado esperado:** Cada interfaz queda con una sola línea: la firma del método sin cuerpo, p. ej.:
```java
public interface SaveUserPort {
    UserModel save(UserModel user);
}
```

**También ajustar los servicios** que llamaban al método estático del puerto. Cambiar `SaveUserPort.save(user)` → `saveUserPort.save(user)` (llamada de instancia). Lo mismo para los 6 servicios que usen algún puerto estático.

**También:** `GetAllUsersService` no tenía campo del puerto. Agregar:
```java
private final GetAllUsersPort getAllUsersPort;
```
y cambiar el cuerpo de `execute()` para usar ese campo en lugar de `GetAllUsersPort.getAll()`.

**`DependencyContainer.java`** — pasar `userRepository` también como `GetAllUsersPort` al constructor de `GetAllUsersService`.

**`UserResponsePrinter.java`** — cambiar `response.getId()`, `response.getName()`, etc. por los accessors de record: `response.id()`, `response.name()`, `response.email()`, `response.role()`, `response.status()`.

---

### Rama: `fix/regla5/violacion1`
**Archivo:** `src/main/java/.../application/service/GetAllUsersService.java`
**Qué hacer:** El método `execute()` retornaba `null` cuando no había usuarios. Reemplazar por `Collections.emptyList()`.

Antes:
```java
return result.isEmpty() ? null : result;
```
Después:
```java
return result;
```
(y asegurarse de que el puerto siempre retorna lista vacía, nunca null)

**Test:** `GetAllUsersServiceTest` — actualizar la prueba de lista vacía para verificar que retorna lista vacía, no null.

---

### Rama: `fix/regla6/violacion1`
**Archivos:**
- `src/main/java/.../application/service/CreateUserService.java`
- `src/main/java/.../infrastructure/entrypoint/desktop/cli/handler/CreateUserHandler.java`
- `src/main/java/.../infrastructure/entrypoint/desktop/cli/handler/LoginHandler.java`

**Qué hacer:** Eliminar logs que incluyen datos de PII (email, nombre de usuario).

`CreateUserService.java` — eliminar la línea:
```java
log.info("Creando usuario con email=" + command.email() + ", nombre=" + command.name());
```
También eliminar `@Log` de Lombok si ya no se usa.

`CreateUserHandler.java` — eliminar `@Log`, el `Logger` manual, y el `log.log(Level.WARNING, ...)`. Reemplazar por:
```java
console.println("  Error: " + exception.getMessage());
```

`LoginHandler.java` — mismo cambio que `CreateUserHandler`.

---

### Rama: `fix/regla9/violacion1`
**Archivo:** `src/main/java/.../infrastructure/entrypoint/desktop/controller/UserController.java`
**Qué hacer:** Los métodos `createUser`, `deleteUser` y `login` construían los comandos/queries directamente en el controlador. Delegar esa construcción a `UserDesktopMapper`.

Antes en `createUser`:
```java
final var command = new CreateUserCommand(request.id(), request.name(), ...);
```
Después:
```java
final var command = UserDesktopMapper.toCreateCommand(request);
```

Aplicar el mismo patrón para `deleteUser` → `UserDesktopMapper.toDeleteCommand(id)` y `login` → `UserDesktopMapper.toLoginCommand(request)`.

**Agregar en `UserDesktopMapper.java`** los métodos estáticos correspondientes si no existen:
```java
public static CreateUserCommand toCreateCommand(CreateUserRequest request) { ... }
public static DeleteUserCommand toDeleteCommand(String id) { ... }
public static LoginCommand toLoginCommand(LoginRequest request) { ... }
```

---

### Rama: `fix/regla3/violacion1`
**Archivos:** todos los DTOs de comando/query en `src/main/java/.../application/service/dto/`

**Qué hacer:**

1. Quitar `@Builder` de los records que lo tengan (los records ya tienen constructor canónico).
2. Quitar el atributo `message = "..."` de las anotaciones de validación (`@NotBlank`, `@Size`, `@Email`, etc.). Deben quedar sin mensaje personalizado.

Ejemplo antes:
```java
@Builder
public record CreateUserCommand(
    @NotBlank(message = "El id no puede estar vacío") String id,
    @NotBlank(message = "...") @Size(min = 3, message = "...") String name,
    ...
) {}
```
Ejemplo después:
```java
public record CreateUserCommand(
    @NotBlank String id,
    @NotBlank @Size(min = 3) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String role
) {}
```

Aplicar en: `CreateUserCommand`, `UpdateUserCommand`, `DeleteUserCommand`, `LoginCommand`, `GetUserByIdQuery`.

---

### Rama: `fix/regla4/violacion1`
**Archivo:** `src/main/java/.../infrastructure/adapter/persistence/config/DatabaseConnectionFactory.java`
**Qué hacer:** La clase tenía un constructor público y el método `createConnection` era de instancia. Convertirla a clase utilitaria con `@UtilityClass` de Lombok y el método `static`.

```java
@UtilityClass
public class DatabaseConnectionFactory {
    public static Connection createConnection(final DatabaseConfig config) { ... }
}
```

**Test:** `DatabaseConnectionFactoryTest` — cambiar `factory.createConnection(config)` por `DatabaseConnectionFactory.createConnection(config)` en todos los puntos donde se llame.

---

### Rama: `fix/regla4/violacion2`
**Archivo:** `src/main/java/.../infrastructure/config/AppProperties.java`
**Qué hacer:**

1. Reemplazar `stream == null` por `Objects.requireNonNull(stream, "...")`.
2. Renombrar variable `props` → `properties`.
3. Renombrar variable `val` → `value`.

---

### Rama: `fix/regla4/violacion3`
**Archivos varios con variables abreviadas:**

`UpdateUserHandler.java`:
- `pw` → `password`
- `upd` → `updatedUser`

`UserManagementCli.java`:
- `opt` → `menuOption`
- Reemplazar el literal duplicado del borde del menú por una constante `MENU_BORDER`.

`UserController.java`:
- `usrs` → `users` en `listAllUsers()`.

---

### Rama: `fix/regla4/violacion4`
**Archivos:**

`EmailNotificationService.java` — hacer `renderTemplate` estático:
```java
private static String renderTemplate(String template, Map<String, String> values) { ... }
```

`EmailDestinationModel.java` — reemplazar `== null` por `Objects.requireNonNull(...)` y extraer los mensajes de error a constantes privadas estáticas:
```java
private static final String DESTINATION_EMAIL_REQUIRED = "El email del destinatario es requerido.";
// etc.
```

`UserRepositoryMySQL.java` — eliminar:
- El campo `private boolean initialized`
- El método `public void init()`
- El método `private UserModel saveWithFields(...)` (que solo lanzaba `UnsupportedOperationException`)
- Los comentarios redundantes dentro de `save()`

`DependencyContainer.java` — eliminar la línea `userRepository.init()`.

---

### Rama: `fix/regla10/violacion1`
**Archivo:** `src/main/java/.../domain/valueobject/UserPassword.java`
**Qué hacer:** Reemplazar los números mágicos `8` y `12` por constantes con nombre:
```java
private static final int MIN_LENGTH = 8;
private static final int BCRYPT_COST = 12;
```

---

### Rama: `fix/regla10/violacion2`
**Archivo:** `src/main/java/.../domain/valueobject/UserName.java`
**Qué hacer:** Reemplazar el número mágico `3` por una constante:
```java
private static final int MIN_LENGTH = 3;
```
También reemplazar el `== null` en la validación por `Objects.requireNonNull(...)`.

---

### Rama: `fix/regla10/violacion3`
**Archivos:** todas las excepciones de dominio en `src/main/java/.../domain/exception/`

**Qué hacer:** Extraer los strings literales hardcodeados en los factory methods a constantes privadas estáticas. Ejemplo:

`UserNotFoundException.java`:
```java
private static final String ID_NOT_FOUND_TEMPLATE = "No se encontró usuario con id '%s'.";

public static UserNotFoundException becauseIdWasNotFound(String id) {
    return new UserNotFoundException(String.format(ID_NOT_FOUND_TEMPLATE, id));
}
```

Aplicar el mismo patrón en: `InvalidCredentialsException`, `InvalidUserEmailException`, `InvalidUserIdException`, `InvalidUserNameException`, `InvalidUserRoleException`, `InvalidUserStatusException`, `UserAlreadyExistsException`, `EmailSenderException`, `ConfigurationException`, `PersistenceException`.

---

### Rama: `fix/regla9/violacion2`
**Archivo:** `src/main/java/.../domain/exception/EmailSenderException.java`
**Qué hacer:** Los dos constructores son `public`. Hacerlos `private`:
```java
private EmailSenderException(final String message) { super(message); }
private EmailSenderException(final String message, final Throwable cause) { super(message, cause); }
```
Solo los factory methods `becauseSmtpFailed` y `becauseSendFailed` deben ser públicos.

---

### Rama: `fix/regla11/violacion1` — LoginServiceTest
**Archivo:** `src/test/java/.../application/service/LoginServiceTest.java`
**Qué hacer:**
- Agregar `@DisplayName` a los métodos que no lo tengan.
- Reemplazar `assertTrue(result != null)` → `assertNotNull(result)`.
- Reemplazar `assertTrue(result == activeUser)` → `assertSame(activeUser, result)`.
- Agregar comentarios `// Arrange`, `// Act`, `// Assert` en todos los métodos.

---

### Rama: `fix/regla11/violacion2` — GetAllUsersServiceTest
**Archivo:** `src/test/java/.../application/service/GetAllUsersServiceTest.java`
**Qué hacer:** Mismas correcciones que violacion1 (DisplayName, assertNotNull, AAA).

---

### Rama: `fix/regla11/violacion3` — UpdateUserServiceTest
**Archivo:** `src/test/java/.../application/service/UpdateUserServiceTest.java`
**Qué hacer:** Mismas correcciones.

---

### Rama: `fix/regla11/violacion4` — CreateUserServiceTest y GetUserByIdServiceTest
**Archivos:**
- `src/test/java/.../application/service/CreateUserServiceTest.java`
- `src/test/java/.../application/service/GetUserByIdServiceTest.java`
**Qué hacer:** Mismas correcciones.

---

## Tabla 2 — Reglas Clean Code

---

### Rama: `fix/regla-cc6/violacion1`
**Archivo:** `src/main/java/.../application/service/UpdateUserService.java`
**Qué hacer:** Eliminar el método `notifyIfRequired(boolean notify)` que usaba un flag booleano para decidir si notificar. Reemplazar por una llamada directa:
```java
emailNotificationService.notifyUserUpdated(updatedUser);
```
También eliminar `@Log` y el `log.info(...)` de PII si todavía existe.

---

### Rama: `fix/regla-cc6/violacion2`
**Archivo:** `src/main/java/.../application/service/EmailNotificationService.java`
**Qué hacer:** Eliminar el método `sendNotificationWithFlag(UserModel user, boolean isCreated, String plainPassword)` que usaba un flag booleano para alternar comportamiento. Reemplazar con dos métodos separados: `notifyUserCreated` y `notifyUserUpdated` (o extraer un método privado `sendNotification` compartido).

---

### Rama: `fix/regla-cc7/violacion1`
**Archivo:** `src/main/java/.../application/service/EmailNotificationService.java`
**Qué hacer:** Renombrar `sendOrLog` → `sendEmail`. El nombre anterior prometía "enviar o loguear" pero el comportamiento real es siempre intentar enviar y loguear solo si falla.

Cambiar la definición:
```java
private void sendEmail(final EmailDestinationModel destination) { ... }
```
Y actualizar la llamada en `sendNotification`.

---

### Rama: `fix/regla-cc8/violacion1`
**Archivo:** `src/main/java/.../application/service/LoginService.java`
**Qué hacer:** El método `getAndValidateUser` mezclaba consulta y validación (violación CQS). Separar en dos métodos:

```java
private UserModel findUserOrFail(final UserEmail email) {
    return getUserByEmailPort
        .getByEmail(email)
        .orElseThrow(InvalidCredentialsException::becauseCredentialsAreInvalid);
}

private void validateCredentials(final UserModel user, final String plainPassword) {
    if (!user.passwordMatches(plainPassword)) {
        throw InvalidCredentialsException.becauseCredentialsAreInvalid();
    }
    if (user.getStatus() != UserStatus.ACTIVE) {
        throw InvalidCredentialsException.becauseUserIsNotActive();
    }
}
```

Y en `execute()`:
```java
final UserModel user = findUserOrFail(email);
validateCredentials(user, command.password());
return user;
```

---

### Rama: `fix/regla-cc11/violacion1`
**Archivo:** `src/main/java/.../application/service/EmailNotificationService.java`
**Qué hacer:** Los métodos `notifyUserCreated` y `notifyUserUpdated` tenían lógica duplicada (cargar template, renderizar, construir destino, enviar). Extraer un método privado compartido:

```java
private void sendNotification(UserModel user, String subject,
        String templateName, Map<String, String> tokens) {
    final String body = renderTemplate(loadTemplate(templateName), tokens);
    sendEmail(buildDestination(user, subject, body));
}
```

Refactorizar `notifyUserCreated` y `notifyUserUpdated` para que deleguen a `sendNotification`.

---

### Rama: `fix/regla-cc13/violacion1`
**Archivo:** `src/main/java/.../application/service/UserValidationUtils.java`
**Qué hacer:** Esta clase no tiene ningún llamador en el proyecto. **Eliminarla por completo.**

---

### Rama: `fix/regla-cc14/violacion1`
**Archivo:** `src/main/java/.../application/service/LoginService.java`
**Qué hacer:** Violación de Ley de Demeter. Reemplazar:
```java
user.getPassword().verifyPlain(plainPassword)
```
Por un método delegado en `UserModel`:
```java
user.passwordMatches(plainPassword)
```

**Agregar en `UserModel.java`:**
```java
public boolean passwordMatches(final String plainPassword) {
    return password.verifyPlain(plainPassword);
}
```

---

### Rama: `fix/regla-cc16/violacion1`
**Archivo:** `src/main/java/.../infrastructure/entrypoint/desktop/cli/io/UserResponsePrinter.java`
**Qué hacer:** Reemplazar la cadena `if/else if` en `getStatusLabel` por un `Map.of(...)`:

```java
private static final Map<String, String> STATUS_LABELS = Map.of(
    "ACTIVE",   "Activo",
    "INACTIVE", "Inactivo",
    "PENDING",  "Pendiente de activacion",
    "BLOCKED",  "Bloqueado",
    "DELETED",  "Eliminado"
);

private static String getStatusLabel(final String status) {
    return STATUS_LABELS.getOrDefault(status, "Estado desconocido");
}
```

---

### Rama: `fix/regla-cc17/violacion1`
**Archivo:** `src/main/java/.../application/service/LoginService.java`
**Qué hacer:** Simplificar la condición redundante de 4 ramas. Antes había algo como:
```java
if (status == ACTIVE) { ... }
else if (status == INACTIVE) { throw ... }
else if (status == PENDING) { throw ... }
else if (status == BLOCKED) { throw ... }
```
Después:
```java
if (user.getStatus() != UserStatus.ACTIVE) {
    throw InvalidCredentialsException.becauseUserIsNotActive();
}
```

---

### Rama: `fix/regla-cc17/violacion2`
**Archivo:** `src/main/java/.../application/service/UpdateUserService.java`
**Qué hacer:** Simplificar la condición compleja en `ensureEmailIsNotTakenByAnotherUser`. Reescribir usando `Optional.map().orElse(false)` en lugar de múltiples null-checks anidados.

```java
private void ensureEmailIsNotTakenByAnotherUser(UserEmail newEmail, UserId ownerId) {
    final boolean takenByAnotherUser = getUserByEmailPort.getByEmail(newEmail)
        .map(existing -> !existing.getId().equals(ownerId))
        .orElse(false);
    if (takenByAnotherUser) {
        throw UserAlreadyExistsException.becauseEmailAlreadyExists();
    }
}
```

---

### Rama: `fix/regla-cc21/violacion1`
**Archivo:** `src/main/java/.../application/service/mapper/UserApplicationMapper.java`
**Qué hacer:** El método `roleToCode` retornaba `-1` como centinela de error. Reemplazar por lanzar `IllegalArgumentException`:

```java
public static int roleToCode(final String role) {
    if (Objects.isNull(role) || role.isBlank()) {
        throw new IllegalArgumentException("Role cannot be null or blank");
    }
    if ("ADMIN".equalsIgnoreCase(role))    return 1;
    if ("MEMBER".equalsIgnoreCase(role))   return 2;
    if ("REVIEWER".equalsIgnoreCase(role)) return 3;
    throw new IllegalArgumentException("Unknown role: " + role);
}
```

---

### Rama: `fix/regla-cc24/violacion1`
**Archivo:** `src/main/java/.../application/service/mapper/UserApplicationMapper.java`
**Qué hacer:** El mismo concepto (email del usuario) se llamaba `correo` en `fromCreateCommandToModel` y `correoElectronico` en `fromUpdateCommandToModel`. Renombrar ambos a `email`.

---

### Rama: `fix/regla-cc25-26/violacion1`
**Archivo:** `src/main/java/.../application/service/EmailNotificationService.java`
**Qué hacer:** Los `Map.of(...)` inline en `notifyUserCreated` y `notifyUserUpdated` eran demasiado compactos. Extraer a métodos privados estáticos:

```java
private static Map<String, String> buildCreatedTokens(UserModel user, String plainPassword) {
    return Map.of(
        TOKEN_NAME,     user.getName().value(),
        TOKEN_EMAIL,    user.getEmail().value(),
        TOKEN_PASSWORD, plainPassword,
        TOKEN_ROLE,     user.getRole().name());
}

private static Map<String, String> buildUpdatedTokens(UserModel user) {
    return Map.of(
        TOKEN_NAME,   user.getName().value(),
        TOKEN_EMAIL,  user.getEmail().value(),
        TOKEN_ROLE,   user.getRole().name(),
        TOKEN_STATUS, user.getStatus().name());
}
```

---

### Rama: `fix/regla-cc27/violacion1`
**Archivo:** `src/main/java/.../infrastructure/entrypoint/desktop/cli/io/UserResponsePrinter.java`
**Qué hacer:** El método `printSummary` usaba una cadena `Optional.ofNullable().filter().map().reduce().map().ifPresentOrElse()` ilegible. Reemplazar por:

```java
public void printSummary(final List<UserResponse> users) {
    if (users == null || users.isEmpty()) {
        console.println("  No users found.");
        return;
    }
    for (final UserResponse user : users) {
        console.println(String.format("  %s (%s)", user.name(), getStatusLabel(user.status())));
    }
}
```

---

### Rama: `fix/regla-cc1-2-3/violacion1`
**Archivo:** `src/main/java/.../application/service/CreateUserService.java`
**Qué hacer:** El método `execute()` hacía demasiadas cosas en un solo bloque (validar, loguear, verificar email, construir modelo, persistir, notificar). Extraer en métodos privados:

```java
@Override
public UserModel execute(final CreateUserCommand command) {
    validateCommand(command);
    ensureEmailIsNotTaken(command.email());
    final UserModel savedUser = saveUserPort.save(buildUser(command));
    emailNotificationService.notifyUserCreated(savedUser, command.password());
    return savedUser;
}

private void validateCommand(final CreateUserCommand command) { ... }
private void ensureEmailIsNotTaken(final String rawEmail) { ... }
private static UserModel buildUser(final CreateUserCommand command) {
    return UserModel.create(
        new UserId(command.id()),
        new UserName(command.name()),
        new UserEmail(command.email()),
        UserPassword.fromPlainText(command.password()),
        UserRole.fromString(command.role()));
}
```

---

### Rama: `fix/regla11/tests-display-name-aaa`
**Archivos de test:**
- `UserIdTest.java` — agregar `@DisplayName` en clase y métodos; `assertEquals` en vez de `assertTrue(x.equals(y))`; comentarios AAA.
- `UserNameTest.java` — igual que UserIdTest.
- `UserPasswordTest.java` — agregar `@DisplayName` en clase; `assertNotNull` en vez de `assertTrue(x != null)`; comentarios AAA.
- `EmailNotificationServiceTest.java` — agregar `@DisplayName` al test que le falta; agregar `// Act` y `// Assert`.
- `UserPersistenceMapperTest.java` — eliminar el campo `private UserPersistenceMapper mapper` (no inicializado); reemplazar `mapper.xxx(...)` por `UserPersistenceMapper.xxx(...)` ya que los métodos son estáticos.
- `UserAlreadyExistsExceptionTest.java` — eliminar el comentario de violación dentro del test.

---

### Limpieza final de comentarios stale
**Qué hacer:** Una vez aplicadas todas las correcciones anteriores, buscar y eliminar todos los comentarios que digan `// VIOLACIÓN`, `// Clean Code - Regla` o `// Regla X` que hayan quedado huérfanos (apuntando a código ya corregido).

Archivos donde pueden quedar:
- `Main.java` — cambiar además `org.slf4j.Logger` por `java.util.logging.Logger` y extraer `buildContainer()` y `buildCli()`.
- `UserController.java`, `UserDesktopMapper.java`, `UserResponsePrinter.java`.

---

## Verificación final

```bash
grep -rn "VIOLACI\|Clean Code - Regla" src/ --include="*.java"
# debe retornar: (sin resultados)

mvn test
# debe retornar: Tests run: 193, Failures: 0, Errors: 0
```

# Роли «супер-админ» и «админ»

## Контекст
Сейчас любой, кто записан в `OurMemory/Admins/{uid}`, — полноправный админ, а добавлять и удалять админов можно только вручную в Firebase Console. Нужна роль **супер-админа**, который управляет админами прямо из приложения, и **обычного админа** без этой возможности. База общая с iOS, поэтому модель данных и правила одинаковые для обеих платформ, и iOS делает то же самое.

Решения:
- админа добавляют **по e-mail**; человек должен хотя бы раз войти в приложение: через Google в «Ещё» или через вход администратора;
- права на загрузку медиа по-прежнему выдаются вручную в `storage.rules` (Storage не читает базу). Экран админов показывает UID с копированием и подсказку.

## 1. Модель данных и правила
**Почему.** Клиент не может найти UID по e-mail в Firebase Auth, поэтому нужен индекс `Accounts`. Существующий формат `Admins/{uid}: true` сохраняется, чтобы старые версии приложения продолжали работать.

| Узел `OurMemory/` | Значение | Кто пишет |
|---|---|---|
| `Admins/{uid}` | `true` | супер-админ (кроме себя) |
| `SuperAdmins/{uid}` | `true` | только консоль |
| `Accounts/{emailKey}` | `{uid, email}` | сам пользователь |

```json
"Admins": {
  ".read": "auth != null && root.child('OurMemory/SuperAdmins/' + auth.uid).exists()",
  "$uid": {
    ".read": "auth != null && auth.uid === $uid",
    ".write": "auth != null && $uid !== auth.uid && root.child('OurMemory/SuperAdmins/' + auth.uid).exists()",
    ".validate": "!newData.exists() || newData.val() === true"
  }
}
```

## 2. Индекс аккаунтов
**Почему.** Без индекса супер-админ не может выдать роль по e-mail.

```kotlin
object AccountKeys {
    private const val DOT = '.'
    private const val KEY_DOT = ','

    fun forEmail(email: String) = email.trim().lowercase().replace(DOT, KEY_DOT)
}
```
`AuthRepositoryImpl` регистрирует не анонимного пользователя в `Accounts` в `observeSession()` (так попадают и Google-аккаунты посетителей) и после успешного `signIn`. Ошибки записи игнорируются.

## 3. Сессия
**Почему.** Интерфейс должен знать, супер-админ ли пользователь.

```kotlin
data class AdminSession(
    val uid: String = "",
    val email: String = "",
    val isAdmin: Boolean = false,
    val isSuperAdmin: Boolean = false
)
```

## 4. Управление админами
```kotlin
interface AdminsRepository {
    fun observeAdmins(): Flow<List<AdminAccount>>
    suspend fun addAdmin(email: String): AddAdminResult
    suspend fun removeAdmin(uid: String)
}
```
`AddAdminResult`: `ADDED`, `ACCOUNT_NOT_FOUND`, `ALREADY_ADMIN`.

Экран `ui/admin/admins`:
- список админов: e-mail, отметка «Супер-админ», UID с копированием;
- удаление обычного админа с подтверждением (себя и супер-админов удалить нельзя);
- диалог добавления по e-mail;
- подсказка про `storage.rules`.

Плитка «Администраторы» на `AdminHomeScreen` видна только супер-админу.

## Проверка
```bash
./gradlew detektAll testDebugUnitTest assembleDebug
```
- Опубликовать правила узла `OurMemory` в консоли, записать `SuperAdmins/{uid}: true`.
- Супер-админ видит плитку, добавляет по e-mail вошедшего ранее человека; у того после перезапуска появляется вкладка «Админ» (Android и iOS).
- E-mail, который ни разу не входил, — сообщение «Аккаунт не найден».
- Удаление снимает роль; себя удалить нельзя; обычный админ плитку не видит.

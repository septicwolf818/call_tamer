# CallTamer

Temporarily block unwanted calls — without adding numbers to a permanent blacklist.

## Setting Up as Default Call Screening App

CallTamer uses Android's `CallScreeningService` API, which requires being set as the system's **default Caller ID & Spam app**:

1. Install and open CallTamer
2. Go to **Settings** → tap **Grant Call Screening Role**
3. You'll be taken to system settings — toggle "Allow role" for CallTamer
4. Once granted, CallTamer will receive all incoming calls before they ring

If the settings screen doesn't work properly, navigate manually:
- **Settings → Apps → Default apps → Caller ID & Spam → CallTamer**

## Known Platform Limitations

### Silence Mode
- `CallResponse.Builder.setSilenceCall(true)` suppresses ringer + incoming-call notification
- However, a **missed-call notification** will appear after the call ends (post-call)
- The call also appears in the device call log
- True "silence with no trace" is not possible via `CallScreeningService` API

### Call Screening Role
- The role survives app updates but is cleared on app data wipe
- Revoking the role silently disables screening — CallTamer shows a banner if it detects this
- Only one app can hold the `ROLE_CALL_SCREENING` at a time

### Reboot
- `AlarmManager` alarms are cleared on device reboot
- `BootReceiver` reschedules all active expiry alarms on boot
- `WorkManager` periodic reconciliation (every 15 min) acts as a safety net

## Testing

```bash
# Unit tests (JVM, fast)
./gradlew testDebugUnitTest

# Install debug build
./gradlew installDebug
```

### Testing Call Screening on Emulator
The emulator doesn't support the Call Screening role natively. You can:
1. Grant the role via ADB: `adb shell roles grant pl.septicwolf818.calltamer android.app.role.CALL_SCREENING`
2. Or sideload onto a physical device

## Architecture

- **UI**: Jetpack Compose + Material 3 (dynamic color)
- **Architecture**: MVVM unidirectional data flow
- **DI**: Hilt
- **Database**: Room
- **Background**: AlarmManager (precise expiry) + WorkManager (reconciliation safety net)
- **Offline**: Fully offline, no network permissions

# Validation

The unit suite covers app ordering, missing/duplicate apps, Android API control boundaries, security classification and connection feedback. Framework tests run on Android APIs 28, 30 and 34. They exercise nested navigation, real preferences, device fallback, legacy/modern Wi-Fi controls, app launching and Control Center. Native graphics tests render actual views at 1080p.

These checks do not establish physical-device performance or universal firmware compatibility. API 26 is the declared minimum; the current automated API matrix starts at 28.

Before a stable release, test Android TV/Google TV devices at 720p, 1080p and 4K with their real density and larger fonts. Record model, Android/API, firmware, build/signature, input device and permission state. Check:

- Cold/warm startup, every D-pad direction, tile long presses, ordering, returning from apps and Control Center focus.
- Empty and populated app lists, installation/removal while Home is visible, TV-only versus All Apps, and missing app banners.
- Background choices and local photos after restart/revoked file access. Screen saver start/exit and inactivity cancellation.
- Once-per-boot startup selection, including a missing or unavailable app.
- Ethernet-only devices, missing/off Wi-Fi, denied location, Location disabled, stale/empty scans, wrong passwords and unsupported security types.
- Bluetooth missing/off, denied nearby-device permission, discovery cancellation and Android pairing confirmation. Turning Bluetooth off must warn about remote disconnection.
- Fixed/variable audio, mute and receiver behavior. Manufacturer settings activity fallbacks and default-home restrictions.
- Memory, cold/warm startup and dropped frames on a low-end device. Record the method; avoid unsupported performance claims.

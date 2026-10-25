# Contributing

Run `./build.sh` with JDK 17 and SDK 34 before submitting a change. Keep production dependencies at zero unless a real need and measured benefit justify a change. Preserve offline behavior and explicit permission requests.

Exercise every action with the D-pad. Focus must be visible, and Back must return through the actual navigation path. Test denied/revoked permission, missing radios, unknown network state, fixed-volume audio, long labels and empty app lists. Add tests for behavior that could regress.

Mark framework renders and sample app data clearly; never present them as physical-device measurements. Do not add copyrighted streaming artwork, branding assets, signing keys, personal paths or credentials to the source.

Use focused commits describing the visible behavior, supported Android versions, checks and outstanding device evidence. Translation and device-test contributions are welcome.

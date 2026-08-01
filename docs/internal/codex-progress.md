# Codex progress

- Last commit: `d4512b9402734c856a2adda19b15fcc13239b4ec`.
- Tests: Seal and Tanki baseline `clean test` pass offline on JDK 21; E2 targeted `:core:test :desktop:test` passes offline.
- Risks: Gradle must run offline with the existing cache; JDK 25 is incompatible with Gradle 8.14.
- Next step: replace the temporary E2 `0.0f` update argument with monotonic capped `dtMillis` in E3.

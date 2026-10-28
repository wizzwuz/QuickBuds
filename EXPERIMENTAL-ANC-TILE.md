# Experimental ANC Quick Settings tile

Long-pressing the Quick Settings tile opens the compact themed control panel in every mode; it does not launch `MainActivity`.

- Noise Cancellation mode: Smart, High, Low, and Medium choices, followed by LHDC and Spatial Sound switches.
- Transparency mode: only LHDC and Spatial Sound switches.
- Normal mode: only LHDC and Spatial Sound switches.
- Experimental LHDC + Spatial Sound coexistence: both switches are independently selectable and the app does not automatically turn the other off. Earbud readback determines whether the combination is actually supported; firmware may still reject or disable one feature. The feature writes use the existing `BudsService`/`BudsConnectionManager` protocol path.

This experimental change has not been verified on a physical earbud/device. Build and test it with the repository's GitHub Actions workflow before relying on it.

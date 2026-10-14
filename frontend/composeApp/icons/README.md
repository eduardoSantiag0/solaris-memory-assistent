# Desktop app icons

`icon.ico` (Windows), `icon.icns` (macOS), `icon.png` (Linux) — wired into
`composeApp/build.gradle.kts`'s `nativeDistributions` block, used for the
Dmg/Msi/Deb installers and the running app's window/taskbar icon.

Same symbol as `design/solaris-handoff_v2/solaris-handoff_2/assets/icone-app-108.svg`
(Deep Ocean background, the planet/horizon/reflection glyph), rasterized by
hand with `gerar_icone.py` since no SVG renderer was available in the
environment that generated these. Regenerate after a logo change with:

```
pip install pillow
python gerar_icone.py
```

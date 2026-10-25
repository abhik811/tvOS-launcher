# Icon design

The launcher uses original Canvas/vector artwork. No Apple logo or SF Symbols font/assets are bundled. Visual reference: [Apple TV Home](https://support.apple.com/en-euro/guide/tv/welcome/tvos) and [Apple TV Control Center](https://support.apple.com/en-euro/guide/tv/atvb0a416871/tvos), reviewed in the tvOS 27 user guide.

- TV cards use 5:3 proportions and a shared rounded mask for artwork and focus rings. Hardware outlines and cached software clipping keep the same silhouette in device rendering and native-graphics previews.
- App banners fill the card without stretching; square or portrait app icons fit proportionally on a subdued colored background. Missing custom artwork produces a letter tile, avoiding the Android default icon.
- Labels sit below the artwork and become visible for the focused app. Launch, ordering and package selection are unchanged.
- Settings uses a proper eight-tooth cog, with a metallic badge at large sizes. Network, volume, apps, Home, screen saver, remote, accessibility, storage and information share consistent optical weight. The clock capsule uses the Control Center slider symbol.
- All geometry is cached; icons are drawn at display resolution and require no network or raster icon-pack downloads.

The preview's Cinema, Files, Music, Photos, Player, Radio, Studio and Weather apps are test-only fixtures with original colorful banners. These do not install apps or change the branding of installed Android apps. Real app icons depend on what is installed on the user's TV. The designs follow Apple TV visual conventions, but are original recreations, not a claim of pixel-identical Apple assets.

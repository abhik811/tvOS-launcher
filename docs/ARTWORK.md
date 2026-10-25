# Wallpaper artwork

Created for this project using the built-in OpenAI image generation tool. These are AI-generated landscapes, not photographs documenting real locations. No existing Apple wallpaper was supplied or copied. App icons and banners still come from installed apps.

Three original PNG masters are 1672 × 941 pixels. The app includes quality-86 WebP copies without upscaling. The compressed set totals approximately 415 KiB. Runtime decoding uses RGB565 and center-crops to the screen while preserving the image aspect ratio; a wallpaper is static between preference/layout changes. Solid black and user-selected local photos remain available.

## Prompt set

Each scene used this shared specification, with its scene description inserted after “Primary request”.

> Use case: photorealistic-natural. Asset type: original premium Android television launcher wallpaper, landscape 16:9, requested 3840 x 2160 resolution if supported. Primary request: [scene]. Composition: immersive edge-to-edge panoramic environment, visual interest in the upper two-thirds, quiet dark lower 30 percent reserved for an app dock, no abrupt highlights at bottom or upper-right clock area. Style: exceptionally polished photographic realism, natural light and material detail, smooth gradients, rich but controlled dynamic range, elegant luxury TV wallpaper. Constraints: artwork only, absolutely no text, labels, logos, icons, UI, framing, watermarks, collage or devices. Create an original scene, do not reproduce an existing wallpaper.

The requested size was advisory; the actual generated output is 1672 × 941, not 4K.

### Aurora Coast

a spectacular remote northern coastline seen from a high aerial perspective, sweeping dark sculptural cliffs on the left, still deep teal ocean opening toward the right, delicate luminous green and cyan aurora ribbons across a midnight navy sky, subtle atmospheric haze, sophisticated restrained colors, realistic intricate rock and water textures

### Desert Dawn

an immense pristine desert of sculptural sand dunes at first sunrise, sweeping elegant S curves, warm peach and rose gold light on dune crests, deep mauve and indigo shadow valleys, distant atmospheric layers under a soft lavender sky, sophisticated fine natural sand texture, cinematic fine art aerial landscape photography

### Alpine Night

a pristine alpine lake at blue hour beneath majestic jagged mountain peaks, mountains softly lit by distant moonlight, deep cobalt and slate blue palette, a subtle star field and faint mist over glassy still water, luminous restrained horizon, meticulous natural rock detail, cinematic fine art landscape photography

## Files

- `app/src/main/res/drawable-nodpi/wallpaper_aurora.webp`
- `app/src/main/res/drawable-nodpi/wallpaper_dawn.webp`
- `app/src/main/res/drawable-nodpi/wallpaper_night.webp`

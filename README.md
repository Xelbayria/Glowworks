# Coloful Lighting (Glowworks)

An add-on for [Colorful Lighting](https://www.curseforge.com/minecraft/mc-mods/colorful-lighting-sodium-compat). 
This is more of a "helper" mod in that it can identify every light block (LightType) and glass block (GlassType) 
in any mod. It will be added to a configuration file, which lets you set the color of one or more blocks. This will 
relieve you of the heavy works.

There are 3 configs:
- `glowworks-blacklist.toml` - where GlassType or LightBlock can be excluded from the config. The emitters.json and filters.json are also excluded, too.
- `glowworks-emitter.toml` - where you can configure the LightType's color
- `glowworks-filter.toml` - where you can configure the GlassType's color

There will be examples in these three files. I advise you to read the explanation of Colorful Lighting in order 
to comprehend the function of emitters.json & filters.json
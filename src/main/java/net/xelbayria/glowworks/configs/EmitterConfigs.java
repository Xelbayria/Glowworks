package net.xelbayria.glowworks.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.xelbayria.glowworks.Glowworks;
import net.xelbayria.glowworks.api.set.light.LightType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;
import static me.erykczy.colorfullighting.common.util.JsonHelper.getColor4FromDyeName;

public class EmitterConfigs {

    public static final JsonObject EMITTER_JSON = new JsonObject();
    public static final Map<String, Supplier<JsonElement>> EMITTER_CONFIGS = new HashMap<>();

    public static ConfigSpec SPEC;
    private static boolean wasInit = false;

    public static final List<String> VANILLA_COLORS = List.of(
            "white",
            "black",
            "light_blue",
            "light_gray",
            "blue",
            "brown",
            "cyan",
            "gray",
            "green",
            "lime",
            "magenta",
            "orange",
            "pink",
            "purple",
            "red",
            "yellow"
    );

    // Blocks' ID has no color from VANILLA_COLORS - This will determine what color the block will be emitting
    public static final Map<String, String> BLOCK_ID_TO_COLOR = Map.ofEntries(
            entry("beacon", "white"),
            entry("dragon_egg", "purple"),
            entry("lava", "orange"),
            entry("candle", "white"),
            entry("crying_obsidian", "purple")
    );

    public static final Map<String, String> KEYWORD_TO_COLOR = Map.ofEntries(
            entry("amethyst", "purple"),
            entry("sea", "light_blue"),
            entry("soul", "blue"),
            entry("torch", "yellow"),
            entry("glowstone", "yellow"),
            entry("dark_matter_block", "black")
    );

    @SuppressWarnings("DataFlowIssue")
    public static void init() {
        if (wasInit) return;
        wasInit = true;

        regexBuilder();

        ConfigBuilder builder = ConfigBuilder.create(Glowworks.res("emitter"), ConfigType.CLIENT);

        String commentBuilder = """
                Setting the block's color to be emitted in 3 ways: HEX, RGB, or COLOR-NAME
                EXAMPLE:
                    [minecraft]
                        [minecraft.blocks]
                            lava = "#FFFF00"\t\t\t\t\tCOMMENT: is a HEX: yellow
                            brown_mushroom = [136, 68, 17]\t\tCOMMENT: is a RGB: brown. NOTE: It's an ARRAY, not STRING.
                            redstone_lamp = "red"\t\t\t\tCOMMENT: no need for an explanatory
                            soul_torch = "purple;5"\t\t\tCOMMENT: override light level emission
                            oak_leaves = "light_blue;F"\t\tCOMMENT: value after ';' is a hex number from 0 to F
                """;

        builder.comment(commentBuilder);

        for (var modId : BlockSetAPI.getTypeRegistry(LightType.class).getValues()) {
            String namespace = modId.getNamespace();
            builder.push(namespace);

            builder.push("blocks");

            for (Map.Entry<String, Object> entry : modId.getChildren()) {

                if (namespace.equals("minecraft")) continue;

                String nameBlock = entry.getKey();
                String blockId = idGenerator(namespace, nameBlock);

                /// Creating RGB
                String dyeName = getColorOrDefault(nameBlock);
                int red = (nameBlock.matches("dark_red_\\w+"))
                        ? getColor4FromDyeName(dyeName).mul(8.5F).red4
                        : getColor4FromDyeName(dyeName).mul(17).red4;
                int green = (nameBlock.matches("dark_green_\\w+"))
                        ? getColor4FromDyeName(dyeName).mul(8.5F).green4
                        : getColor4FromDyeName(dyeName).mul(17).green4;
                int blue = (nameBlock.matches("dark_blue_\\w+"))
                        ? getColor4FromDyeName(dyeName).mul(8.5F).blue4
                        : getColor4FromDyeName(dyeName).mul(17).blue4;

                JsonArray rgb = new JsonArray(3);
                rgb.add(red);
                rgb.add(green);
                rgb.add(blue);

                /// Adding a config for each blockId in the config
                Supplier<JsonElement> configSupplier = builder.defineJson(nameBlock, rgb);

                EMITTER_CONFIGS.computeIfAbsent(blockId, s -> JsonArray::new);

                EMITTER_CONFIGS.put(blockId, configSupplier);

            }

            builder.pop();

            builder.pop();
        }

        SPEC = builder.buildAndRegister();

        SPEC.loadFromFile();

        /// Adding the blockId & its RGB to EMITTER_JSON & it will be imported into emitters.json
        for (Map.Entry<String, Supplier<JsonElement>> entry : EMITTER_CONFIGS.entrySet()) {
            String blockId = entry.getKey();
            Supplier<JsonElement> value = entry.getValue();

            if (blockId.contains("minecraft")) continue;

            EMITTER_JSON.add(blockId, value.get());
        }
    }

// ────────────────────────────────────────────────────── Methods ──────────────────────────────────────────────────────

    /// Check if there is a color keywords as prefix in the id
    static Pattern prefixRegEx;
    /// Check if there is a color keywords as suffix in the id
    static Pattern suffixRegEx;

    /// Get the keyword of color or defaulted to "white"
    public static String getColorOrDefault(String nameBlock) {
        Matcher colorPrefix = prefixRegEx.matcher(nameBlock);
        Matcher colorSuffix = suffixRegEx.matcher(nameBlock);

        if (colorPrefix.matches()) return colorPrefix.group("color");
        else if (colorSuffix.matches()) return colorSuffix.group("color");

        else if (BLOCK_ID_TO_COLOR.containsKey(nameBlock))
            return BLOCK_ID_TO_COLOR.get(nameBlock);

        return KEYWORD_TO_COLOR.entrySet().stream()
            .filter(entry -> nameBlock.contains(entry.getKey()))
            .map(Map.Entry::getValue).findFirst().orElse("white");
    }

    public static void regexBuilder() {
        String RegEx = "";

        for (int idx = 0; idx < VANILLA_COLORS.size(); idx++) {
            String color = VANILLA_COLORS.get(idx);
            RegEx = RegEx.concat(color);
            if (idx < VANILLA_COLORS.size() - 1) RegEx = RegEx.concat("|");
        }

        prefixRegEx = Pattern.compile("(?:[a-z]+_)?(?<color>%s)(?:stone)?_(?:\\w+)?".formatted(RegEx));
        suffixRegEx = Pattern.compile("(?:stone)?(?:\\w+_)(?<color>%s)".formatted(RegEx));
    }

    public static String idGenerator(String namespace, String nameBlock) {
        return namespace + ":" + nameBlock;
    }


}

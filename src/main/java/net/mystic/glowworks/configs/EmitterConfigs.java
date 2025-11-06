package net.mystic.glowworks.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mystic.glowworks.Glowworks;
import net.mystic.glowworks.api.set.LightType;

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

    private static final List<String> VANILLA_COLORS = List.of(
            "white",
            "black",
            "blue",
            "brown",
            "cyan",
            "gray",
            "green",
            "light_blue",
            "light_gray",
            "lime",
            "magenta",
            "orange",
            "pink",
            "purple",
            "red",
            "yellow"
    );

    // Blocks' ID has no color from VANILLA_COLORS - This will determine what color the block will be emitting
    private static final Map<String, String> BLOCK_ID_TO_COLOR = Map.ofEntries(
            entry("beacon", "white"),
            entry("dragon_egg", "purple"),
            entry("lava", "orange"),
            entry("candle", "white"),
            entry("crying_obsidian", "purple")
    );

    private static final Map<String, String> KEYWORD_TO_COLOR = Map.ofEntries(
            entry("amethyst", "purple"),
            entry("sea", "light_blue"),
            entry("soul", "blue"),
            entry("torch", "yellow")
    );

//    @SuppressWarnings("DataFlowIssue")
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

            modId.getChildren().forEach(entry -> {
                String nameBlock = entry.getKey();
                String dyeName = getColorOrDefault(nameBlock);

                try {

                    int red = getColor4FromDyeName(dyeName).red4 * 17;
                    int green = getColor4FromDyeName(dyeName).green4 * 17;
                    int blue = getColor4FromDyeName(dyeName).blue4 * 17;

                    JsonArray rgb = new JsonArray(3);
                    rgb.add(red);
                    rgb.add(green);
                    rgb.add(blue);

                    String blockId = idGenerator(namespace, nameBlock);

                    Supplier<JsonElement> configSupplier = builder.defineJson(nameBlock, rgb);

                    EMITTER_CONFIGS.computeIfAbsent(blockId, s -> JsonArray::new);

                    EMITTER_CONFIGS.put(blockId, configSupplier);

                } catch (Exception e) {
                    Glowworks.LOGGER.error("Failed to get color for {}: {}", nameBlock, e);
                }
            });

            builder.pop();

            builder.pop();
        }

        SPEC = builder.buildAndRegister();

        SPEC.loadFromFile();

        EMITTER_CONFIGS.forEach((blockId, value) -> EMITTER_JSON.add(blockId, value.get()));
    }

// ────────────────────────────────────────────────────── Methods ──────────────────────────────────────────────────────

    static Pattern compiledRegEx;

    /// Get the keyword of color or defaulted to "yellow"
    private static String getColorOrDefault(String nameBlock) {
        if (VANILLA_COLORS.stream().anyMatch(nameBlock::contains)) {
            Matcher m = compiledRegEx.matcher(nameBlock);

            if (m.find()) return m.group("color");
            else {
                Glowworks.LOGGER.warn("Failed to get color & defaulted to \"white\" for {}", nameBlock);
                return "white";
            }
        }
        else if (BLOCK_ID_TO_COLOR.containsKey(nameBlock)) return BLOCK_ID_TO_COLOR.get(nameBlock);

        return KEYWORD_TO_COLOR.entrySet().stream()
                .filter(entry -> nameBlock.contains(entry.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse("white");
    }

    private static void regexBuilder() {
        String RegEx = "";

        for (int idx = 0; idx < VANILLA_COLORS.size(); idx++) {
            String color = VANILLA_COLORS.get(idx);
            RegEx = RegEx.concat(color);
            if (idx < VANILLA_COLORS.size() - 1) RegEx = RegEx.concat("|");
        }

        compiledRegEx = Pattern.compile("(?:\\w+_)?(?<color>%s)(?:stone)?(?:_\\w+)?".formatted(RegEx));
    }

    public static String idGenerator(String namespace, String nameBlock) {
        return namespace + ":" + nameBlock;
    }


}

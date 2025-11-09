package net.xelbayria.glowworks.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.xelbayria.glowworks.Glowworks;
import net.xelbayria.glowworks.api.set.glass.GlassType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static me.erykczy.colorfullighting.common.util.JsonHelper.getColor4FromDyeName;
import static net.xelbayria.glowworks.configs.EmitterConfigs.*;

public class FilterConfigs {

    public static final JsonObject FILTER_JSON = new JsonObject();
    public static final Map<String, Supplier<JsonElement>> FILTER_CONFIGS = new HashMap<>();

    public static ConfigSpec SPEC;
    private static boolean wasInit = false;

    @SuppressWarnings("DataFlowIssue")
    public static void init() {
        if (wasInit) return;
        wasInit = true;

        regexBuilder();

        ConfigBuilder builder = ConfigBuilder.create(Glowworks.res("filter"), ConfigType.CLIENT);

        String commentBuilder = """
                The role of filter is a regular light that passes through a filter of any color.
                Setting the glass's color to be filtered in 3 ways: HEX, RGB, or COLOR-NAME
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

        for (var modId : BlockSetAPI.getTypeRegistry(GlassType.class).getValues()) {
            String namespace = modId.getNamespace();
            builder.push(namespace);

            builder.push("blocks");

            modId.getChildren().forEach(entry -> {
                String nameBlock = entry.getKey();
                String blockId = idGenerator(namespace, nameBlock);

                /// Creating RGB
                String dyeName = getColorOrDefault(nameBlock);
                int red = getColor4FromDyeName(dyeName).mul(17).red4;
                int green = getColor4FromDyeName(dyeName).mul(17).green4;
                int blue = getColor4FromDyeName(dyeName).mul(17).blue4;

                JsonArray rgb = new JsonArray(3);
                rgb.add(red);
                rgb.add(green);
                rgb.add(blue);

                /// Adding a config for each blockId in the config
                Supplier<JsonElement> configSupplier = builder.defineJson(nameBlock, rgb);

                FILTER_CONFIGS.computeIfAbsent(blockId, s -> JsonArray::new);

                FILTER_CONFIGS.put(blockId, configSupplier);

            });

            builder.pop();

            builder.pop();
        }

        SPEC = builder.buildAndRegister();

        SPEC.loadFromFile();

        /// Adding the blockId & its RGB to FILTER_JSON & it will be imported into filters.json
        FILTER_CONFIGS.forEach((blockId, value) -> FILTER_JSON.add(blockId, value.get()));
    }

}

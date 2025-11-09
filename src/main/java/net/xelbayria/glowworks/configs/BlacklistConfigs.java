package net.xelbayria.glowworks.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.xelbayria.glowworks.Glowworks;

import java.util.List;
import java.util.function.Supplier;

public class BlacklistConfigs {

    public static Supplier<List<String>> LIGHT_BLACKLIST;
    public static Supplier<List<String>> GLASS_BLACKLIST;

    public static ConfigSpec SPEC;
    private static boolean wasInit = false;

    public static void init() {
        if (wasInit) return;
        wasInit = true;

        ConfigBuilder builder = ConfigBuilder.create(Glowworks.res("blacklist"), ConfigType.CLIENT);

        String commentBuilder = """
                Blacklist blocks from being applied with emitters
                    EXAMPLE: light_blacklist = [
                        "minecraft:torch",\t\t\tCOMMENT: a simple block_id for a torch. It's blacklisted
                        "minecraft:.*torch",\t\tCOMMENT: this is a regex, soul_torch, soul_wall_torch, torch, wall_torch from Minecraft's are blacklisted.
                        "minecraft:.*",\t\t\t\tCOMMENT: This is a regex, all of blocks from Minecraft's are blacklisted.
                        ".*:.*torch"\t\t\t\tCOMMENT: this is a regex: soul_torch, soul_wall_torch, dim_redstone_torch, and other torches from multiple mods are blacklisted.
                    ]
                """;

        builder.comment(commentBuilder);

        builder.push("light_blacklist");

        LIGHT_BLACKLIST = builder.define("light_blacklist", List.of());

        builder.pop();

        builder.push("glass_blacklist");

        GLASS_BLACKLIST = builder.define("glass_blacklist", List.of());

        builder.pop();

        SPEC = builder.buildAndRegister();

        SPEC.loadFromFile();
    }
}

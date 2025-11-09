package net.xelbayria.glowworks.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.xelbayria.glowworks.Glowworks;

import java.util.function.Supplier;

//loaded after registry
public class GlowworksConfigs {

    public static ConfigSpec CLIENT_SPED;

    public static final Supplier<Boolean> DEBUG_RESOURCES;
    public static final Supplier<Boolean> GENERATE_DYNAMIC_CLIENT;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Glowworks.res("client"), ConfigType.CLIENT);
        builder.push("general");

        GENERATE_DYNAMIC_CLIENT = builder.comment("Enables the generation of dynamic assets. This is required for the mod to work properly. Turn off if you chose to add all the generated assets via datapack manually. This can speedup boot times for modpacks. Note that the generated assets will depend on loaded datapacks")
                .define("generate_dynamic_assets", true);

        DEBUG_RESOURCES = builder.comment("Creates a debug folder inside your instance directory where all the dynamically generated resources will be saved")
                .define("save_debug_resources", false);

        builder.pop();

        CLIENT_SPED = builder.buildAndRegister();
        CLIENT_SPED.loadFromFile(); //manually load early
    }

    public static void init() {}
}

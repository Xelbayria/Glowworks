package net.mystic.glowworks ;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.mystic.glowworks.api.set.LightType;
import net.mystic.glowworks.api.set.LightTypeRegistry;
import net.mystic.glowworks.configs.BlacklistConfigs;
import net.mystic.glowworks.configs.EmitterConfigs;
import net.mystic.glowworks.configs.GlowworksConfigs;
import net.mystic.glowworks.dynamicpack.ClientDynamicResourcesHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Glowworks.MOD_ID)
public final class Glowworks {
    public static final String MOD_ID = "glowworks";
    public static final Logger LOGGER = LogManager.getLogger("Glowworks");

    public Glowworks() {

        GlowworksConfigs.init();
        BlacklistConfigs.init();

        BlockSetAPI.registerBlockSetDefinition(LightTypeRegistry.INSTANCE);
        BlockSetAPI.addDynamicBlockRegistration((r, t) ->
                        EmitterConfigs.init(), LightType.class
                );

        if (PlatHelper.getPhysicalSide().isClient()) ClientDynamicResourcesHandler.getInstance().register();

    }

    /// @return MOD_ID:path
    public static ResourceLocation res(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}

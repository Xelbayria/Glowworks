package net.mystic.glowworks.dynamicpack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.minecraft.resources.ResourceLocation;
import net.mystic.glowworks.Glowworks;
import net.mystic.glowworks.configs.EmitterConfigs;
import net.mystic.glowworks.configs.GlowworksConfigs;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;


public class ClientDynamicResourcesHandler extends DynClientResourcesGenerator {

    private static ClientDynamicResourcesHandler INSTANCE;

    public static ClientDynamicResourcesHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientDynamicResourcesHandler();
        }
        return INSTANCE;
    }

    private boolean firstInit = false;

    public ClientDynamicResourcesHandler() {
        super(new DynamicTexturePack(Glowworks.res("generated_pack")));
        //since we place chests textures in its namespace to use its renderer
        if (PlatHelper.isModLoaded("quark")) getPack().addNamespaces("quark");

        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev() || GlowworksConfigs.DEBUG_RESOURCES.get());
    }

    @Override
    public Logger getLogger() {
        return Glowworks.LOGGER;
    }

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        if (!GlowworksConfigs.GENERATE_DYNAMIC_CLIENT.get()) return;

        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev() || GlowworksConfigs.DEBUG_RESOURCES.get());

        executor.accept((manager, sink) -> {

            //            if (manager.getResource(EmitterConfigs.EMITTER_LOCATION)) {
                Glowworks.LOGGER.info("Generating emitters.json...");
                ResourceLocation resLoc = Glowworks.res("light/emitters");

                sink.addJson(resLoc, EmitterConfigs.EMITTER_JSON, ResType.JSON);
                Glowworks.LOGGER.info("Generation Completed");
//            }
        });


    }
}


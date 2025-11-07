package net.mystic.glowworks.api.set.light;

import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.mystic.glowworks.Glowworks;

import java.util.*;

import static net.mystic.glowworks.configs.BlacklistConfigs.BLACKLIST;

public class LightTypeRegistry extends BlockTypeRegistry<LightType> {

    public static final LightTypeRegistry INSTANCE = new LightTypeRegistry();

//    public static Map<String, >

    protected LightTypeRegistry() {
        super(LightType.class, "light_type");
    }

    @Override
    protected LightType register(LightType vanillaType) {
        return super.register(vanillaType);
    }

    @Override
    public LightType getDefaultType() {
        return null; // Add VanillaEmissionTypes.???
    }

    @Override
    protected Optional<LightType> detectTypeFromBlock(Block block, ResourceLocation baseRes) {
        ResourceLocation standard_id = baseRes.withPath("block_type");
        String blockPath = baseRes.getPath();

        boolean isBlockNotBlacklisted = BLACKLIST.get().stream().noneMatch(baseRes.toString()::matches);

        /// Detect blocks that has emission greater than 0
        if (block.getStateDefinition().getPossibleStates().get(0).getLightEmission() != 0) {
            if (isBlockNotBlacklisted) {
                if (!valuesReg.containsKey(standard_id)) {
                    LightType lightType = new LightType(standard_id);
                    lightType.addChild(blockPath, block);
                    return Optional.of(lightType);
                }
                else if (valuesReg.containsKey(standard_id))
                    Objects.requireNonNull(valuesReg.getValue(standard_id)).addChild(blockPath, block);
                else
                    Glowworks.LOGGER.error("Failed to add a block to LightType: {}", Utils.getID(block).toString());
            }
        }
        return Optional.empty();
    }

    /// Shorthand for add finder. Gives a builder-like object that's meant to be configured inline
    public LightType.Finder addSimpleFinder(ResourceLocation lightTypeId) {
        LightType.Finder finder = new LightType.Finder(lightTypeId);
        this.addFinder(finder);
        return finder;
    }

    public LightType.Finder addSimpleFinder(String lightTypeId) {
        return addSimpleFinder(new ResourceLocation(lightTypeId));
    }

    public LightType.Finder addSimpleFinder(String namespace, String nameLightType) {
        return addSimpleFinder(new ResourceLocation(namespace, nameLightType));
    }


    @Override
    public int priority() {
        return 110;
    }
}

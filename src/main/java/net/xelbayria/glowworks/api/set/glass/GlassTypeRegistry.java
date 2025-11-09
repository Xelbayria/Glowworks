package net.xelbayria.glowworks.api.set.glass;

import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.xelbayria.glowworks.Glowworks;

import java.util.Objects;
import java.util.Optional;

import static net.xelbayria.glowworks.configs.BlacklistConfigs.GLASS_BLACKLIST;

public class GlassTypeRegistry extends BlockTypeRegistry<GlassType> {

    public static final GlassTypeRegistry INSTANCE = new GlassTypeRegistry();

    protected GlassTypeRegistry() {
        super(GlassType.class, "glass_type");
    }

    @Override
    protected GlassType register(GlassType vanillaType) {
        return super.register(vanillaType);
    }

    @Override
    public GlassType getDefaultType() {
        return null; // Add VanillaEmissionTypes.???
    }

    @Override
    protected Optional<GlassType> detectTypeFromBlock(Block block, ResourceLocation baseRes) {
        ResourceLocation standard_id = baseRes.withPath("block_type");
        String blockPath = baseRes.getPath();

        boolean isBlockNotBlacklisted = GLASS_BLACKLIST.get().stream().noneMatch(baseRes.toString()::matches);

        /// Detect blocks that has emission greater than 0
        if (blockPath.matches("(?:\\w+)?_?(?:glass|window)_?(?:\\w+)?")) {
            if (isBlockNotBlacklisted) {
                if (!valuesReg.containsKey(standard_id)) {
                    GlassType glassType = new GlassType(standard_id);
                    glassType.addChild(blockPath, block);
                    return Optional.of(glassType);
                }
                else if (valuesReg.containsKey(standard_id))
                    Objects.requireNonNull(valuesReg.getValue(standard_id)).addChild(blockPath, block);
                else
                    Glowworks.LOGGER.error("Failed to add a block to GlassType: {}", Utils.getID(block).toString());
            }
        }
        return Optional.empty();
    }

    /// Shorthand for add finder. Gives a builder-like object that's meant to be configured inline
    public GlassType.Finder addSimpleFinder(ResourceLocation glassTypeId) {
        GlassType.Finder finder = new GlassType.Finder(glassTypeId);
        this.addFinder(finder);
        return finder;
    }

    public GlassType.Finder addSimpleFinder(String glassTypeId) {
        return addSimpleFinder(new ResourceLocation(glassTypeId));
    }

    public GlassType.Finder addSimpleFinder(String namespace, String nameGlassType) {
        return addSimpleFinder(new ResourceLocation(namespace, nameGlassType));
    }


    @Override
    public int priority() {
        return 110;
    }
}

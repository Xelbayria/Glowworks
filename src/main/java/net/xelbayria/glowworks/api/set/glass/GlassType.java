package net.xelbayria.glowworks.api.set.glass;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.xelbayria.glowworks.Glowworks;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class GlassType extends BlockType {

    public GlassType(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public String getTranslationKey() {
        return "glass_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    protected void initializeChildrenBlocks() {}

    @Override
    protected void initializeChildrenItems() {}

    @Override
    public ItemLike mainChild() {
        return Blocks.TORCH;
    }

    protected static ResourceLocation[] makeKnownIDConventions(ResourceLocation id, String... affixKeyword) {
        List<ResourceLocation> resources = new ArrayList<>();
        for (String keyword : affixKeyword) {
            String path = id.getPath();
            String namespace = id.getNamespace();

            String _suffix = (keyword.isEmpty()) ? "" : "_" + keyword;
            String prefix_ = (keyword.isEmpty()) ? "" : keyword + "_";

            resources.add(new ResourceLocation(namespace, path + _suffix));
            resources.add(new ResourceLocation(namespace, prefix_ + path));
        }
        return resources.toArray(new ResourceLocation[0]);
    }

    public static Block findGlassBlock(ResourceLocation id) {
        ResourceLocation[] tests = makeKnownIDConventions(id,  "");
        return Utils.findFirstInRegistry(BuiltInRegistries.BLOCK, tests);
    }

    public static class Finder extends SetFinderBuilder<GlassType> {

        private Supplier<Block> glassFinder;

        public Finder(ResourceLocation id) {
            super(id, GlassTypeRegistry.INSTANCE);
            this.glassBlock(() -> findGlassBlock(id));
        }

        public Finder glassBlock(Supplier<Block> glassFinder) {
            this.glassFinder = glassFinder;
            return this;
        }

        /// @param id Full Id of MudType as ResourceLocation
        public Finder glassBlock(ResourceLocation id) {
            return this.glassBlock(() -> BuiltInRegistries.BLOCK.getOptional(id)
                    .orElseThrow(() -> new IllegalStateException("Failed to find glass block: " + id))
            );
        }

        /// /// @param nameEmission name of Stone Block without modId or namespace
        public Finder glassBlock(String nameGlass) {
            return this.glassBlock(Utils.idWithOptionalNamespace(nameGlass, id.getNamespace()));
        }

        /**
         * @param prefix include the underscore, "_" if the blockId has one
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder glassAffix(String prefix, String suffix) {
            return glassBlock(prefix + id.getPath() + suffix);
        }

        /**
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder glassSuffix(String suffix) {
            return glassBlock(id.getPath() + suffix);
        }

        @Override
        @ApiStatus.Internal
        public Optional<GlassType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block glass = Preconditions.checkNotNull(glassFinder.get(), "Manual Finder - failed to find a glass block for {}", id);
                    var glassType = new GlassType(id);
                    childNames.forEach((key, value) -> {
                        try {
                            ItemLike obj = Preconditions.checkNotNull(value.get());
                            glassType.addChild(key, obj);
                        } catch (Exception e) {
                            Glowworks.LOGGER.warn("Failed to get children for GlassType: {} - {}. Ignored! ERROR: {}", id, key, e.getMessage());
                        }
                    });
                    return Optional.of(glassType);
                } catch (Exception e) {
                    Glowworks.LOGGER.warn("Failed to find custom GlassType: {} - ", id, e);
                }
            }
            return Optional.empty();
        }
    }
}

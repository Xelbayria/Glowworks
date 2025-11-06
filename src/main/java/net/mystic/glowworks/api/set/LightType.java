package net.mystic.glowworks.api.set;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.mystic.glowworks.Glowworks;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class LightType extends BlockType {

    public LightType(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public String getTranslationKey() {
        return "light_type." + this.getNamespace() + "." + this.getTypeName();
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

    public static Block findLightBlock(ResourceLocation id) {
        ResourceLocation[] tests = makeKnownIDConventions(id,  "");
        return Utils.findFirstInRegistry(BuiltInRegistries.BLOCK, tests);
    }

    public static class Finder extends SetFinderBuilder<LightType> {

        private Supplier<Block> lightFinder;

        public Finder(ResourceLocation id) {
            super(id, LightTypeRegistry.INSTANCE);
            this.lightBlock(() -> findLightBlock(id));
        }

        public Finder lightBlock(Supplier<Block> lightFinder) {
            this.lightFinder = lightFinder;
            return this;
        }

        /// @param id Full Id of MudType as ResourceLocation
        public Finder lightBlock(ResourceLocation id) {
            return this.lightBlock(() -> BuiltInRegistries.BLOCK.getOptional(id)
                    .orElseThrow(() -> new IllegalStateException("Failed to find light block: " + id))
            );
        }

        /// /// @param nameEmission name of Stone Block without modId or namespace
        public Finder lightBlock(String nameLight) {
            return this.lightBlock(Utils.idWithOptionalNamespace(nameLight, id.getNamespace()));
        }

        /**
         * @param prefix include the underscore, "_" if the blockId has one
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder lightAffix(String prefix, String suffix) {
            return lightBlock(prefix + id.getPath() + suffix);
        }

        /**
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder lightSuffix(String suffix) {
            return lightBlock(id.getPath() + suffix);
        }

        @Override
        @ApiStatus.Internal
        public Optional<LightType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block light = Preconditions.checkNotNull(lightFinder.get(), "Manual Finder - failed to find a light block for {}", id);
                    var lightType = new LightType(id);
                    childNames.forEach((key, value) -> {
                        try {
                            ItemLike obj = Preconditions.checkNotNull(value.get());
                            lightType.addChild(key, obj);
                        } catch (Exception e) {
                            Glowworks.LOGGER.warn("Failed to get children for LightType: {} - {}. Ignored! ERROR: {}", id, key, e.getMessage());
                        }
                    });
                    return Optional.of(lightType);
                } catch (Exception e) {
                    Glowworks.LOGGER.warn("Failed to find custom LightType: {} - ", id, e);
                }
            }
            return Optional.empty();
        }
    }
}

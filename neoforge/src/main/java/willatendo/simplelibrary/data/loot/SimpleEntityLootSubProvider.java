package willatendo.simplelibrary.data.loot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SimpleEntityLootSubProvider extends EntityLootSubProvider {
    private final String modId;

    public SimpleEntityLootSubProvider(String modId) {
        super(FeatureFlags.REGISTRY.allFlags());
        this.modId = modId;
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return BuiltInRegistries.ENTITY_TYPE.stream().filter(entityType -> this.modId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getNamespace())).collect(Collectors.toSet()).stream();
    }
}

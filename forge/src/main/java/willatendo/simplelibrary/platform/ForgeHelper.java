package willatendo.simplelibrary.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;
import willatendo.simplelibrary.server.menu.ExtendedMenuSupplier;
import willatendo.simplelibrary.server.util.RegistryHolder;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

public class ForgeHelper implements ModloaderHelper {
    @Override
    public boolean isDevEnviroment() {
        return !FMLEnvironment.production;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public CreativeModeTab.Builder createCreativeModeTab() {
        return CreativeModeTab.builder();
    }

    @Override
    public <T extends Entity> EntityType<T> entityTypeBuilder(String name, EntityType.EntityFactory<T> entityFactory, MobCategory mobCategory, boolean noSave, boolean fireImmune, Optional<Block> immuneTo, float width, float height) {
        EntityType.Builder<T> builder = EntityType.Builder.of(entityFactory, mobCategory).sized(width, height);
        if (noSave) {
            builder.noSave();
        }
        if (fireImmune) {
            builder.fireImmune();
        }
        if (!immuneTo.isEmpty()) {
            builder.immuneTo(immuneTo.get());
        }
        return builder.build(name);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(ExtendedMenuSupplier<T> extendedMenuSupplier) {
        return IForgeMenuType.create((windowId, inventory, friendlyByteBuf) -> extendedMenuSupplier.create(windowId, inventory, friendlyByteBuf));
    }

    @Override
    public <T> RegistryHolder<T> createRegistry(ResourceKey<Registry<T>> resourceKey) {
        RegistryBuilder<T> registryBuilder = new RegistryBuilder<T>().setName(resourceKey.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving().hasTags();
        DeferredRegister<T> deferredRegister = DeferredRegister.create(resourceKey, resourceKey.location().getNamespace());
        Supplier<IForgeRegistry<T>> iForgeRegistry = deferredRegister.makeRegistry(() -> registryBuilder);
        return new RegistryHolder<T>() {
            @Override
            public T getOrThrow(ResourceKey<T> resourceKey) {
                return iForgeRegistry.get().getDelegateOrThrow(resourceKey).value();
            }

            @Override
            public Holder<T> getHolderOrThrow(ResourceKey<T> resourceKey) {
                return iForgeRegistry.get().getDelegateOrThrow(resourceKey);
            }

            @Override
            public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> resourceKey) {
                return iForgeRegistry.get().getDelegate(resourceKey);
            }

            @Override
            public Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagKey) {
                return iForgeRegistry.get().tags().getTag(tagKey).stream().map(value -> {
                    return Holder.direct(value);
                }).toList();
            }

            @Override
            public ResourceLocation getKey(T value) {
                return iForgeRegistry.get().getKey(value);
            }

            @Override
            public T get(ResourceLocation resourceLocation) {
                return iForgeRegistry.get().getValue(resourceLocation);
            }

            @Override
            public IdMap<Holder<T>> asHolderIdMap() {
                return new IdMap<Holder<T>>() {
                    @Override
                    public int getId(Holder<T> holder) {
                        return iForgeRegistry.get().getValues().stream().toList().indexOf(holder.value());
                    }

                    @Nullable
                    @Override
                    public Holder<T> byId(int id) {
                        return Holder.direct(iForgeRegistry.get().getValues().stream().toList().get(id));
                    }

                    @Override
                    public int size() {
                        return iForgeRegistry.get().getValues().stream().toList().size();
                    }

                    @NotNull
                    @Override
                    public Iterator<Holder<T>> iterator() {
                        return iForgeRegistry.get().getValues().stream().map(value -> {
                            return Holder.direct(value);
                        }).iterator();
                    }
                };
            }

            @Override
            public Registry<T> registry() {
                return null;
            }

            @Override
            public int getId(T value) {
                return iForgeRegistry.get().getValues().stream().toList().indexOf(value);
            }

            @Nullable
            @Override
            public T byId(int id) {
                return iForgeRegistry.get().getValues().stream().toList().get(id);
            }

            @Override
            public int size() {
                return iForgeRegistry.get().getValues().stream().toList().size();
            }

            @NotNull
            @Override
            public Iterator<T> iterator() {
                return iForgeRegistry.get().iterator();
            }
        };
    }

    @Override
    public SpawnEggItem createSpawnEgg(Supplier<EntityType<? extends Mob>> entityType, int primaryColor, int secondaryColor, Item.Properties properties) {
        return new ForgeSpawnEggItem(entityType, primaryColor, secondaryColor, properties);
    }

    @Override
    public SimpleParticleType createParticleType(boolean overrideLimiter) {
        return new SimpleParticleType(overrideLimiter);
    }

    @Override
    public void openContainer(BlockEntity blockEntity, BlockPos blockPos, ServerPlayer serverPlayer) {
        serverPlayer.openMenu((MenuProvider) blockEntity, blockPos);
    }
}

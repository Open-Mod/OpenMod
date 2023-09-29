package dev.openmod.project.custom;

import dev.openmod.project.Project;
import dev.openmod.project.init.BlockInit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CustomBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Project.MODID);
    public static final Map<String, Supplier<BlockEntityType<?>>> BLOCK_ENTITIES_MAP = new HashMap();
    public static void init() {
        for(RegistryObject<Block> blockEntry : BlockInit.BLOCKS.getEntries()) {
            BLOCK_ENTITIES_MAP.put(blockEntry.getKey().location().getPath(),  BLOCK_ENTITIES.register(blockEntry.getKey().location().getPath(), () -> BlockEntityType.Builder.of((pos, state) -> new CustomBlockEntity(blockEntry.getKey().location().getPath(), pos, state),
                    blockEntry.get()).build(null)));
        }
    }
}

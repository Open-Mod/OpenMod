package dev.openmod.project.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class CustomTree extends SaplingBlock implements EntityBlock {
    private String name;
    public CustomTree(String name, Properties properties) {
        super(new AbstractTreeGrower() {
            @Override
            public boolean growTree(ServerLevel p_222905_, ChunkGenerator p_222906_, BlockPos p_222907_, BlockState p_222908_, RandomSource p_222909_) {
                return super.growTree(p_222905_, p_222906_, p_222907_, p_222908_, p_222909_);
            }

            @Nullable
            @Override
            protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_222910_, boolean p_222911_) {
                return null;
            }
        }, properties);
        this.name = name;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new CustomBlockEntity(this.name, blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
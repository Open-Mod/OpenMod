package dev.openmod.project.util;

import dev.openmod.project.Project;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.function.Consumer;

public class CustomBlockItem extends BlockItem implements GeoItem {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private String name;
    private boolean custom;
    public CustomBlockItem(String name, String modelType, Block block, Properties properties) {
        super(block, properties);
        this.name = name;
        this.custom = !modelType.equals("default");
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(!custom) return IClientItemExtensions.super.getCustomRenderer();
                if(this.renderer == null)
                    this.renderer = new GeoItemRenderer(new GeoModel() {
                        @Override
                        public ResourceLocation getModelResource(GeoAnimatable animatable) {
                            return new ResourceLocation(Project.MODID, "geo/" + name + ".geo.json");
                        }

                        @Override
                        public ResourceLocation getTextureResource(GeoAnimatable animatable) {
                            return new ResourceLocation(Project.MODID, "textures/block/" + name + ".png");
                        }

                        @Override
                        public ResourceLocation getAnimationResource(GeoAnimatable animatable) {
                            return new ResourceLocation(Project.MODID, "animations/" + name + ".animation.json");
                        }
                    });
                return this.renderer;
            }
        });
    }

    public PlayState predicate(AnimationState animationState) {
        animationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        if(!custom) return;
        controllerRegistrar.add(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        if(!custom) return null;
        return cache;
    }

    @Override
    public double getTick(Object itemStack) {
        return RenderUtils.getCurrentTick();
    }
}

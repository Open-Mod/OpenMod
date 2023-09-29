package dev.openmod.project.custom;

import dev.openmod.project.Project;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.function.Consumer;

public class CustomAxe extends AxeItem implements GeoItem {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private String name;
    private IEventBus eventBus = MinecraftForge.EVENT_BUS;
    private boolean custom;
    public CustomAxe(String name, String modelType, Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
        this.name = name;
        this.custom = !modelType.equals("default");
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
                            return new ResourceLocation(Project.MODID, "textures/item/" + name + ".png");
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
        eventBus.post(new CustomEvent.AnimationInit(animationState, name));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        if(!custom) return;
        controllerRegistrar.add(new AnimationController(this, "controller", 0, this::predicate));
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        if(!custom) return null;
        return cache;
    }

    @Override
    public double getTick(Object itemStack) {
        return RenderUtils.getCurrentTick();
    }
}

package dev.openmod.project.custom;

import dev.openmod.project.Project;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Consumer;

public class CustomArmor extends ArmorItem implements GeoItem {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private IEventBus eventBus = MinecraftForge.EVENT_BUS;
    private String materialName;
    private boolean custom;
    public CustomArmor(String materialName, String modelType, ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
        this.materialName = materialName;
        this.custom = !modelType.equals("default");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if(!custom) return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
                if(this.renderer == null)
                    this.renderer = new GeoArmorRenderer(new GeoModel() {
                        @Override
                        public ResourceLocation getModelResource(GeoAnimatable animatable) {
                            return new ResourceLocation(Project.MODID, "geo/" + materialName + ".geo.json");
                        }

                        @Override
                        public ResourceLocation getTextureResource(GeoAnimatable animatable) {
                            return new ResourceLocation(Project.MODID, "textures/item/" + materialName + ".png");
                        }

                        @Override
                        public ResourceLocation getAnimationResource(GeoAnimatable animatable) {
                            return new ResourceLocation(Project.MODID, "animations/" + materialName + ".animation.json");
                        }
                    });
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    public PlayState predicate(AnimationState animationState) {
        eventBus.post(new CustomEvent.AnimationInit(animationState, this));
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
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        if(!p_41433_.isCrouching()) return super.use(p_41432_, p_41433_, p_41434_);
        ItemStack itemstack = p_41433_.getItemInHand(p_41434_);
        if (itemstack.isEdible()) {
            if (p_41433_.canEat(itemstack.getFoodProperties(p_41433_).canAlwaysEat())) {
                p_41433_.startUsingItem(p_41434_);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            return InteractionResultHolder.pass(p_41433_.getItemInHand(p_41434_));
        }
    }
    @Override
    public ItemStack finishUsingItem(ItemStack p_41409_, Level p_41410_, LivingEntity p_41411_) {
        return this.isEdible() ? p_41411_.eat(p_41410_, p_41409_) : p_41409_;
    }
}

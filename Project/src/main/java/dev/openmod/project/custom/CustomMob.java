package dev.openmod.project.custom;

import dev.openmod.project.init.MobInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class CustomMob extends Animal implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private String name;
    private static IEventBus eventBus = MinecraftForge.EVENT_BUS;

    public CustomMob(String name, EntityType entityType, Level level) {
        super(entityType, level);
        this.name = name;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        eventBus.post(new CustomEvent.MobGoalsInit(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        eventBus.post(new CustomEvent.MobServerAiStep(this));
    }
    public void setWalking() {
        this.moveControl = new MoveControl(this);
        this.navigation = this.createNavigation(this.level());
    }
    public void setFlying() {
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.navigation = new FlyingPathNavigation(this, this.level());
    }
    @Override
    public void tick() {
        super.tick();
        eventBus.post(new CustomEvent.MobTick(this));
    }

    public PlayState predicate(AnimationState animationState) {
        eventBus.post(new CustomEvent.AnimationInit_mob(animationState, this));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

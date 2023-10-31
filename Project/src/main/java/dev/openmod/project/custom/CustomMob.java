package dev.openmod.project.custom;

import dev.openmod.project.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.function.Supplier;

public class CustomMob extends Animal implements GeoEntity, ItemSteerable, Saddleable {
    private static EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(CustomMob.class, EntityDataSerializers.BOOLEAN);
    private static EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(CustomMob.class, EntityDataSerializers.INT);
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
    private String name;
    public boolean requiresSaddle;
    private boolean rideable;
    private Supplier<Item> ridingItem;
    private static IEventBus eventBus = MinecraftForge.EVENT_BUS;

    public CustomMob(String name, EntityType entityType, Level level, boolean requiresSaddle, boolean rideable, String ridingItem, String controller) {
        super(entityType, level);
        this.name = name;
        this.requiresSaddle = requiresSaddle;
        this.rideable = rideable;
        if(!ridingItem.equals("none")) {
            Supplier<Item> item = null;
            for (RegistryObject<Item> itemEntry : ItemInit.ITEMS.getEntries()) {
                if (ridingItem.equals(itemEntry.getKey().location().getPath())) {
                    item = () -> itemEntry.get();
                    break;
                }
            }
            if (item == null)
                item = () -> RegistryObject.create(new ResourceLocation(ridingItem), ForgeRegistries.ITEMS).get();
            this.ridingItem = item;
        } else
            this.ridingItem = null;
        if(controller.equals("walking"))
            this.setWalking();
        else if(controller.equals("flying"))
            this.setFlying();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }
    @javax.annotation.Nullable
    public LivingEntity getControllingPassenger() {
        if (this.isSaddled()) {
            Entity entity = this.getFirstPassenger();
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (ridingItem != null && (player.getMainHandItem().is(ridingItem.get()) || player.getOffhandItem().is(ridingItem.get()))) {
                    return player;
                } else if(ridingItem == null) {
                    return player;
                }
            }
        }

        return null;
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_29480_) {
        if (DATA_BOOST_TIME.equals(p_29480_) && this.level().isClientSide) {
            this.steering.onSynced();
        }

        super.onSyncedDataUpdated(p_29480_);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SADDLE_ID, false);
        this.entityData.define(DATA_BOOST_TIME, 0);
    }

    public void addAdditionalSaveData(CompoundTag p_29495_) {
        super.addAdditionalSaveData(p_29495_);
        this.steering.addAdditionalSaveData(p_29495_);
    }

    public void readAdditionalSaveData(CompoundTag p_29478_) {
        super.readAdditionalSaveData(p_29478_);
        this.steering.readAdditionalSaveData(p_29478_);
    }
    @Override
    protected Vec3 getRiddenInput(Player p_278309_, Vec3 p_275479_) {
        LivingEntity driver = (LivingEntity) this.getPassengers().get(0);
        return new Vec3(0.0D, ridingItem == null ? driver.zza * this.getXRot() / 45 * -1 : this.getXRot() / 45 * -1, ridingItem == null ? driver.zza : 1.0D);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        eventBus.post(new CustomEvent.MobGoalsInit(this));
    }

    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {
        if(flies()) return;
        super.checkFallDamage(p_20990_, p_20991_, p_20992_, p_20993_);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        eventBus.post(new CustomEvent.MobServerAiStep(this));
    }
    public void setWalking() {
        this.setNoGravity(false);
        this.moveControl = new MoveControl(this);
        this.navigation = this.createNavigation(this.level());
    }
    public void setFlying() {
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.navigation = new FlyingPathNavigation(this, this.level());
    }
    public boolean flies() {
        return this.moveControl instanceof FlyingMoveControl;
    }
    @Override
    public void tick() {
        super.tick();
        eventBus.post(new CustomEvent.MobTick(this));
    }

    public InteractionResult mobInteract(Player p_29489_, InteractionHand p_29490_) {
        eventBus.post(new CustomEvent.MobInteract(p_29489_, p_29490_, this));
        boolean flag = this.isFood(p_29489_.getItemInHand(p_29490_));
        if (!flag && this.isSaddled() && !this.isVehicle() && !p_29489_.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                p_29489_.startRiding(this);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            InteractionResult interactionresult = super.mobInteract(p_29489_, p_29490_);
            if (!interactionresult.consumesAction()) {
                ItemStack itemstack = p_29489_.getItemInHand(p_29490_);
                return itemstack.is(Items.SADDLE) ? itemstack.interactLivingEntity(p_29489_, this, p_29490_) : InteractionResult.PASS;
            } else {
                return interactionresult;
            }
        }
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

    @Override
    public boolean boost() {
        return false;
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void equipSaddle(@javax.annotation.Nullable SoundSource p_29476_) {
        this.steering.setSaddle(true);
        if (p_29476_ != null) {
            this.level().playSound((Player)null, this, SoundEvents.PIG_SADDLE, p_29476_, 0.5F, 1.0F);
        }

    }

    @Override
    public boolean isSaddled() {
        if(this.rideable) return this.requiresSaddle ? this.steering.hasSaddle() : true;
        return false;
    }
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.isSaddled() && this.requiresSaddle) {
            this.spawnAtLocation(Items.SADDLE);
        }

    }
    protected void tickRidden(Player p_278330_, Vec3 p_278267_) {
        super.tickRidden(p_278330_, p_278267_);
        this.setRot(p_278330_.getYRot(), p_278330_.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.steering.tickBoost();
    }


    protected float getRiddenSpeed(Player p_278258_) {
        if(this.level().getBlockState(new BlockPos((int) this.getX(), (int) this.getY() - 1, (int) this.getZ())).isAir() && flies())
            return (float)(this.getAttributeValue(Attributes.FLYING_SPEED) * 0.225D);
        else
            return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225D);
    }
    public Vec3 getDismountLocationForPassenger(LivingEntity p_29487_) {
        Direction direction = this.getMotionDirection();
        if (direction.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(p_29487_);
        } else {
            int[][] aint = DismountHelper.offsetsForDirection(direction);
            BlockPos blockpos = this.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for(Pose pose : p_29487_.getDismountPoses()) {
                AABB aabb = p_29487_.getLocalBoundsForPose(pose);

                for(int[] aint1 : aint) {
                    blockpos$mutableblockpos.set(blockpos.getX() + aint1[0], blockpos.getY(), blockpos.getZ() + aint1[1]);
                    double d0 = this.level().getBlockFloorHeight(blockpos$mutableblockpos);
                    if (DismountHelper.isBlockFloorValid(d0)) {
                        Vec3 vec3 = Vec3.upFromBottomCenterOf(blockpos$mutableblockpos, d0);
                        if (DismountHelper.canDismountTo(this.level(), p_29487_, aabb.move(vec3))) {
                            p_29487_.setPose(pose);
                            return vec3;
                        }
                    }
                }
            }

            return super.getDismountLocationForPassenger(p_29487_);
        }
    }
}

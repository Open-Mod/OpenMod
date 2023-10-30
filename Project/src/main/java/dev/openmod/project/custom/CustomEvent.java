package dev.openmod.project.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import software.bernie.geckolib.core.animation.AnimationState;

public class CustomEvent extends Event {
    public static class AnimationInit extends CustomEvent
    {
        private AnimationState animationState;
        private Object item;
        public AnimationInit(AnimationState animationState, Object item)
        {
            super();
            this.animationState = animationState;
            this.item = item;
        }

        public AnimationState getAnimationState() {
            return animationState;
        }

        public Object get() {
            return item;
        }
    }

    public static class AnimationInit_block extends CustomEvent
    {
        private AnimationState animationState;
        private Object item;
        public AnimationInit_block(AnimationState animationState, Object item)
        {
            super();
            this.animationState = animationState;
            this.item = item;
        }

        public AnimationState getAnimationState() {
            return animationState;
        }

        public Object get() {
            return item;
        }
    }

    public static class AnimationInit_mob extends CustomEvent
    {
        private AnimationState animationState;
        private Object item;
        public AnimationInit_mob(AnimationState animationState, Object item)
        {
            super();
            this.animationState = animationState;
            this.item = item;
        }

        public AnimationState getAnimationState() {
            return animationState;
        }

        public Object get() {
            return item;
        }
    }

    public static class MobGoalsInit extends CustomEvent
    {
        private Mob mob;
        public MobGoalsInit(Mob mob)
        {
            super();
            this.mob = mob;
        }

        public Mob get() {
            return mob;
        }
    }

    public static class MobServerAiStep extends CustomEvent
    {
        private Mob mob;
        public MobServerAiStep(Mob mob)
        {
            super();
            this.mob = mob;
        }

        public Mob get() {
            return mob;
        }
    }

    public static class MobTick extends CustomEvent
    {
        private Mob mob;
        public MobTick(Mob mob)
        {
            super();
            this.mob = mob;
        }

        public Mob get() {
            return mob;
        }

    }
    public static class MobInteract extends CustomEvent
    {
        private Mob mob;
        private Player player;
        private Vec3 pos;
        private InteractionHand hand;
        public MobInteract(Player player, Vec3 pos, InteractionHand hand, Mob mob)
        {
            super();
            this.mob = mob;
            this.player = player;
            this.pos = pos;
            this.hand = hand;
        }

        public Mob getMob() {
            return mob;
        }
        public Player getPlayer() {
            return player;
        }
        public Vec3 getPos() {
            return pos;
        }
        public InteractionHand getHand() {
            return hand;
        }

    }
}

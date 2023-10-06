package dev.openmod.project.custom;

import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraftforge.eventbus.api.Event;
import software.bernie.geckolib.core.animation.AnimationState;

public class CustomEvent extends Event {
    public static class AnimationInit extends CustomEvent
    {
        private AnimationState animationState;
        private String name;
        public AnimationInit(AnimationState animationState, String name)
        {
            super();
            this.animationState = animationState;
            this.name = name;
        }

        public AnimationState getAnimationState() {
            return animationState;
        }

        public String getName() {
            return name;
        }
    }

    public static class MobGoalsInit extends CustomEvent
    {
        private GoalSelector goalSelector;
        private GoalSelector targetSelector;
        private String name;
        public MobGoalsInit(GoalSelector goalSelector, GoalSelector targetSelector, String name)
        {
            super();
            this.goalSelector = goalSelector;
            this.targetSelector = targetSelector;
            this.name = name;
        }

        public GoalSelector getGoalSelector() {
            return goalSelector;
        }

        public GoalSelector getTargetSelector() {
            return targetSelector;
        }

        public String getName() {
            return name;
        }
    }
}

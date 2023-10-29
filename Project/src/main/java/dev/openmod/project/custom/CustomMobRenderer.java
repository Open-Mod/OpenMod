package dev.openmod.project.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Map;

public class CustomMobRenderer extends GeoEntityRenderer<CustomMob> {
    private String name;
    public CustomMobRenderer(String name, EntityRendererProvider.Context renderManager) {
        super(renderManager, new GeoModel<CustomMob>() {
            @Override
            public ResourceLocation getModelResource(CustomMob animatable) {
                return new ResourceLocation(Project.MODID, "geo/" + name + ".geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(CustomMob animatable) {
                return new ResourceLocation(Project.MODID, "textures/entity/" + name + ".png");
            }

            @Override
            public ResourceLocation getAnimationResource(CustomMob animatable) {
                return new ResourceLocation(Project.MODID, "animations/" + name + ".animation.json");
            }
            @Override
            public void setCustomAnimations(CustomMob animatable, long instanceId, AnimationState<CustomMob> animationState) {
                CoreGeoBone head = getAnimationProcessor().getBone("head");

                if (head != null) {
                    EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

                    head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
                    head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
                }
            }
        });
        this.name = name;
    }

    @Override
    public ResourceLocation getTextureLocation(CustomMob animatable) {
        return new ResourceLocation(Project.MODID, "textures/entity/" + name + ".png");
    }

    @Override
    public void render(CustomMob entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if(entity.isBaby()) {
            Map<String, Map> mobs = Util.readFileAsJSON("../src/data/mobs.json");
            Map data = null;
            for (Map.Entry<String, Map> mobEntry : mobs.entrySet()) {
                String name = mobEntry.getKey();
                if(name.equals(this.name)) {
                    data = mobEntry.getValue();
                    break;
                }
            }
            float sizeRatio = ((Number) data.get("sizeRatio")).intValue() / 100f;
            poseStack.scale(sizeRatio, sizeRatio, sizeRatio);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}

package dev.openmod.project.util;

import dev.openmod.project.Project;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CustomBlockRenderer extends GeoBlockRenderer<CustomBlock> {
    public CustomBlockRenderer(String name) {
        super(new GeoModel() {
            @Override
            public ResourceLocation getModelResource(GeoAnimatable animatable) {
                return new ResourceLocation(Project.MODID, "geo/" + name.substring(0, name.length() - 7) + ".geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(GeoAnimatable animatable) {
                return new ResourceLocation(Project.MODID, "textures/block/" + name.substring(0, name.length() - 7) + ".png");
            }

            @Override
            public ResourceLocation getAnimationResource(GeoAnimatable animatable) {
                return new ResourceLocation(Project.MODID, "animations/" + name.substring(0, name.length() - 7) + ".animation.json");
            }
        });
    }
}

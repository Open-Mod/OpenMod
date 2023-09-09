package dev.openmod.project.init;

import com.mojang.datafixers.util.Pair;
import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import terrablender.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BiomeInit {
    public static Map<String, ResourceKey> BIOMES = new HashMap<String, ResourceKey>();
    public static void init(IEventBus bus) {
        Map<String, Map> biomes = Util.readFileAsJSON("../src/data/biomes.json");
        for (Map.Entry<String, Map> biomeEntry : biomes.entrySet()) {
            String name = biomeEntry.getKey();
            BIOMES.put(name, ResourceKey.create(Registries.BIOME, new ResourceLocation(Project.MODID, name)));
        }
        bus.addListener((FMLCommonSetupEvent event) ->
        {
            event.enqueueWork(() ->
            {
                for (Map.Entry<String, Map> biomeEntry : biomes.entrySet()) {
                    String name = biomeEntry.getKey();
                    final Map data = biomeEntry.getValue();
                    int weight = ((Number) data.get("weight")).intValue();
                    String type = (String) data.get("type");
                    String blockAbove = (String) data.get("blockAbove");
                    String blockUnder = (String) data.get("blockUnder");
                    Block above = null;
                    Block under = null;
                    for(RegistryObject<Block> itemEntry : BlockInit.BLOCKS.getEntries()) {
                        if(blockAbove.equals(itemEntry.getKey().location().getPath())) {
                            above = itemEntry.get();
                            break;
                        }
                    }
                    if(above == null) above = RegistryObject.create(new ResourceLocation(blockAbove), ForgeRegistries.BLOCKS).get();
                    for(RegistryObject<Block> itemEntry : BlockInit.BLOCKS.getEntries()) {
                        if(blockUnder.equals(itemEntry.getKey().location().getPath())) {
                            under = itemEntry.get();
                            break;
                        }
                    }
                    if(under == null) under = RegistryObject.create(new ResourceLocation(blockUnder), ForgeRegistries.BLOCKS).get();
                    Regions.register(new Region(new ResourceLocation(Project.MODID, name), type.equals("overworld") ? RegionType.OVERWORLD : RegionType.NETHER, weight) {
                        @Override
                        public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
                            ParameterUtils.Temperature minT = null;
                            ParameterUtils.Temperature maxT = null;
                            ParameterUtils.Humidity minH = null;
                            ParameterUtils.Humidity maxH = null;
                            ArrayList<ParameterUtils.Continentalness> C = new ArrayList();
                            ArrayList<ParameterUtils.Erosion> E = new ArrayList();
                            ArrayList<ParameterUtils.Depth> D = new ArrayList();
                            ArrayList<ParameterUtils.Weirdness> W = new  ArrayList();
                            String minTemp = (String) data.get("minTemp");
                            String maxTemp = (String) data.get("maxTemp");
                            String minHumidity = (String) data.get("minHumidity");
                            String maxHumidity = (String) data.get("maxHumidity");
                            ArrayList continentalness = (ArrayList) data.get("continentalness");
                            ArrayList erosion = (ArrayList) data.get("erosion");
                            ArrayList depth = (ArrayList) data.get("depth");
                            ArrayList weirdness = (ArrayList) data.get("weirdness");
                            if(minTemp.equals("icy")) minT = ParameterUtils.Temperature.ICY;
                            else if(minTemp.equals("cool")) minT = ParameterUtils.Temperature.COOL;
                            else if(minTemp.equals("neutral")) minT = ParameterUtils.Temperature.NEUTRAL;
                            else if(minTemp.equals("warm")) minT = ParameterUtils.Temperature.WARM;
                            else if(minTemp.equals("hot")) minT = ParameterUtils.Temperature.HOT;
                            else if(minTemp.equals("frozen")) minT = ParameterUtils.Temperature.FROZEN;
                            else if(minTemp.equals("unfrozen")) minT = ParameterUtils.Temperature.UNFROZEN;
                            else if(minTemp.equals("full_range")) minT = ParameterUtils.Temperature.FULL_RANGE;
                            if(maxTemp.equals("icy")) maxT = ParameterUtils.Temperature.ICY;
                            else if(maxTemp.equals("cool")) maxT = ParameterUtils.Temperature.COOL;
                            else if(maxTemp.equals("neutral")) maxT = ParameterUtils.Temperature.NEUTRAL;
                            else if(maxTemp.equals("warm")) maxT = ParameterUtils.Temperature.WARM;
                            else if(maxTemp.equals("hot")) maxT = ParameterUtils.Temperature.HOT;
                            else if(maxTemp.equals("frozen")) maxT = ParameterUtils.Temperature.FROZEN;
                            else if(maxTemp.equals("unfrozen")) maxT = ParameterUtils.Temperature.UNFROZEN;
                            else if(maxTemp.equals("full_range")) maxT = ParameterUtils.Temperature.FULL_RANGE;
                            if(minHumidity.equals("arid")) minH = ParameterUtils.Humidity.ARID;
                            else if(minHumidity.equals("dry")) minH = ParameterUtils.Humidity.DRY;
                            else if(minHumidity.equals("neutral")) minH = ParameterUtils.Humidity.NEUTRAL;
                            else if(minHumidity.equals("wet")) minH = ParameterUtils.Humidity.WET;
                            else if(minHumidity.equals("humid")) minH = ParameterUtils.Humidity.HUMID;
                            else if(minHumidity.equals("full_range")) minH = ParameterUtils.Humidity.FULL_RANGE;
                            if(maxHumidity.equals("arid")) maxH = ParameterUtils.Humidity.ARID;
                            else if(maxHumidity.equals("dry")) maxH = ParameterUtils.Humidity.DRY;
                            else if(maxHumidity.equals("neutral")) maxH = ParameterUtils.Humidity.NEUTRAL;
                            else if(maxHumidity.equals("wet")) maxH = ParameterUtils.Humidity.WET;
                            else if(maxHumidity.equals("humid")) maxH = ParameterUtils.Humidity.HUMID;
                            else if(maxHumidity.equals("full_range")) maxH = ParameterUtils.Humidity.FULL_RANGE;
                            if(continentalness.contains("mushroom_fields")) C.add(ParameterUtils.Continentalness.MUSHROOM_FIELDS);
                            if(continentalness.contains("deep_ocean")) C.add(ParameterUtils.Continentalness.DEEP_OCEAN);
                            if(continentalness.contains("ocean")) C.add(ParameterUtils.Continentalness.OCEAN);
                            if(continentalness.contains("coast")) C.add(ParameterUtils.Continentalness.COAST);
                            if(continentalness.contains("near_inland")) C.add(ParameterUtils.Continentalness.NEAR_INLAND);
                            if(continentalness.contains("mid_inland")) C.add(ParameterUtils.Continentalness.MID_INLAND);
                            if(continentalness.contains("far_inland")) C.add(ParameterUtils.Continentalness.FAR_INLAND);
                            if(continentalness.contains("inland")) C.add(ParameterUtils.Continentalness.INLAND);
                            if(continentalness.contains("full_range")) C.add(ParameterUtils.Continentalness.FULL_RANGE);
                            if(erosion.contains("erosion_0")) E.add(ParameterUtils.Erosion.EROSION_0);
                            if(erosion.contains("erosion_1")) E.add(ParameterUtils.Erosion.EROSION_1);
                            if(erosion.contains("erosion_2")) E.add(ParameterUtils.Erosion.EROSION_2);
                            if(erosion.contains("erosion_3")) E.add(ParameterUtils.Erosion.EROSION_3);
                            if(erosion.contains("erosion_4")) E.add(ParameterUtils.Erosion.EROSION_4);
                            if(erosion.contains("erosion_5")) E.add(ParameterUtils.Erosion.EROSION_5);
                            if(erosion.contains("erosion_6")) E.add(ParameterUtils.Erosion.EROSION_6);
                            if(erosion.contains("full_range")) E.add(ParameterUtils.Erosion.FULL_RANGE);
                            if(depth.contains("surface")) D.add(ParameterUtils.Depth.SURFACE);
                            if(depth.contains("underground")) D.add(ParameterUtils.Depth.UNDERGROUND);
                            if(depth.contains("floor")) D.add(ParameterUtils.Depth.FLOOR);
                            if(depth.contains("full_range")) D.add(ParameterUtils.Depth.FULL_RANGE);
                            if(weirdness.contains("low_slice_normal_descending")) W.add(ParameterUtils.Weirdness.LOW_SLICE_NORMAL_DESCENDING);
                            if(weirdness.contains("low_slice_variant_ascending")) W.add(ParameterUtils.Weirdness.LOW_SLICE_VARIANT_ASCENDING);
                            if(weirdness.contains("mid_slice_normal_ascending")) W.add(ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING);
                            if(weirdness.contains("mid_slice_normal_descending")) W.add(ParameterUtils.Weirdness.MID_SLICE_NORMAL_DESCENDING);
                            if(weirdness.contains("mid_slice_variant_ascending")) W.add(ParameterUtils.Weirdness.MID_SLICE_VARIANT_ASCENDING);
                            if(weirdness.contains("mid_slice_variant_descending")) W.add(ParameterUtils.Weirdness.MID_SLICE_VARIANT_DESCENDING);
                            if(weirdness.contains("high_slice_normal_ascending")) W.add(ParameterUtils.Weirdness.HIGH_SLICE_NORMAL_ASCENDING);
                            if(weirdness.contains("high_slice_normal_descending")) W.add(ParameterUtils.Weirdness.HIGH_SLICE_NORMAL_DESCENDING);
                            if(weirdness.contains("high_slice_variant_ascending")) W.add(ParameterUtils.Weirdness.HIGH_SLICE_VARIANT_ASCENDING);
                            if(weirdness.contains("high_slice_variant_descending")) W.add(ParameterUtils.Weirdness.HIGH_SLICE_VARIANT_DESCENDING);
                            if(weirdness.contains("peak_normal")) W.add(ParameterUtils.Weirdness.PEAK_NORMAL);
                            if(weirdness.contains("peak_variant")) W.add(ParameterUtils.Weirdness.PEAK_VARIANT);
                            if(weirdness.contains("valley")) W.add(ParameterUtils.Weirdness.VALLEY);
                            if(weirdness.contains("full_range")) W.add(ParameterUtils.Weirdness.FULL_RANGE);
                            VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();
                            new ParameterUtils.ParameterPointListBuilder()
                                    .temperature(ParameterUtils.Temperature.span(minT, maxT))
                                    .humidity(ParameterUtils.Humidity.span(minH, maxH))
                                    .continentalness(C.toArray(new ParameterUtils.Continentalness[0]))
                                    .erosion(E.toArray(new ParameterUtils.Erosion[0]))
                                    .depth(D.toArray(new ParameterUtils.Depth[0]))
                                    .weirdness(W.toArray(new ParameterUtils.Weirdness[0]))
                                    .build().forEach(point -> builder.add(point, BIOMES.get(name)));

                            builder.build().forEach(mapper::accept);
                        }
                    });
                    SurfaceRuleManager.addSurfaceRules(type.equals("overworld") ? SurfaceRuleManager.RuleCategory.OVERWORLD : SurfaceRuleManager.RuleCategory.NETHER, Project.MODID,  SurfaceRules.sequence(
                            SurfaceRules.ifTrue(SurfaceRules.isBiome(BIOMES.get(name)),
                                    SurfaceRules.sequence(
                                            SurfaceRules.ifTrue(
                                                    SurfaceRules.waterBlockCheck(-1, 0),
                                                    SurfaceRules.sequence(
                                                            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                                                    SurfaceRules.state(above.defaultBlockState())
                                                            ),
                                                            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR,
                                                                    SurfaceRules.state(under.defaultBlockState())
                                                            )
                                                    )
                                            ),
                                            SurfaceRules.state(under.defaultBlockState())
                                    )
                            )
                    ));
                }
            });
        });
    }
}

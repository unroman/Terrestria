package com.terraformersmc.terrestria;

import com.terraformersmc.terrestria.config.TerrestriaConfigManager;
import com.terraformersmc.terrestria.data.TerrestriaDatagen;
import com.terraformersmc.terrestria.init.*;
import com.terraformersmc.terrestria.init.helpers.TerrestriaPlacementModifierType;
import com.terraformersmc.terrestria.init.helpers.TerrestriaRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Mod("terrestria_common")
public class Terrestria {
	public static final String MOD_ID = "terrestria";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static final TerrestriaConfigManager CONFIG_MANAGER = new TerrestriaConfigManager();

	private static Boolean initialized = false;
	private static final ArrayList<Runnable> runnables = new ArrayList<>(1);

	public static ItemGroup itemGroup;

	public Terrestria(){
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::commonLoad);
		eventBus.register(this);
		itemGroup = new ItemGroup(MOD_ID + ".items") {

			@Override
			public void appendStacks(DefaultedList<ItemStack> stacks) {
				super.appendStacks(stacks);
				Registry.ITEM.forEach(item -> {
					if (Registry.ITEM.getId(item).getNamespace().equals(MOD_ID)) {
						item.appendStacks(item.getGroup(), stacks);
					}
				});
			}

			@Override
			public ItemStack createIcon() {
				return new ItemStack(TerrestriaItems.RUBBER_SAPLING);
			}
		};
	}

	private static void register() {
		ModContainer previous = ModLoadingContext.get().getActiveContainer();
		ModContainer newContainer = ModList.get().getModContainerById(MOD_ID).orElse(null);
		if (newContainer == null) newContainer = previous;
		ModLoadingContext.get().setActiveContainer(newContainer);
		// Load the general config if it hasn't been loaded already
		CONFIG_MANAGER.getGeneralConfig();

		TerrestriaBlocks.init();
		TerrestriaItems.init();
		TerrestriaPlacementModifierType.init();
		TerrestriaFoliagePlacerTypes.init();
		TerrestriaTrunkPlacerTypes.init();
		TerrestriaTreeDecorators.init();
		TerrestriaFeatures.init();
		TerrestriaConfiguredFeatures.init();
		TerrestriaPlacedFeatures.init();
		TerrestriaStructures.init();
		TerrestriaBiomes.init();
		TerrestriaVillagerTypes.init();

		// This must be after TerrestriaBiomes.init()
		CONFIG_MANAGER.getBiomeConfig();
		ModLoadingContext.get().setActiveContainer(previous);
	}

	public void onInitialize() {
		register();

		if (!ModList.get().isLoaded("terrestria_worldgen")) {
			Terrestria.LOGGER.info("No Terrestria worldgen module present; Terrestria biomes will not generate.");
		}

		// At this point Terrestria is completely initialized.
		initialized = true;
	}

	@SubscribeEvent
	public void onRegister(final RegisterEvent event){
		if (event.getRegistryKey() == ForgeRegistries.Keys.BLOCKS) {
			onInitialize();
		}
	}

	private void commonLoad(FMLCommonSetupEvent event){
		runnables.forEach(event::enqueueWork);
	}

	public static void callbackWhenInitialized(Runnable callback) {
		if (initialized) {
			callback.run();
		} else {
			runnables.add(callback);
		}
	}

	public static TerrestriaConfigManager getConfigManager() {
		return CONFIG_MANAGER;
	}
}

package brightspark.asynclocator;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(ALConstants.MOD_ID)
public class AsyncLocatorModForge {
	public AsyncLocatorModForge() {
		ModLoadingContext ctx = ModLoadingContext.get();

		// Tells Forge that this mod is only required server side
		ctx.registerExtensionPoint(
			IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				(serverVersion, networkBool) -> true
			)
		);

		ctx.registerConfig(ModConfig.Type.SERVER, AsyncLocatorConfigForge.SPEC);

		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		forgeEventBus.addListener((ServerAboutToStartEvent event) -> AsyncLocator.setupExecutorService());
		forgeEventBus.addListener((ServerStoppingEvent event) -> AsyncLocator.shutdownExecutorService());

		FMLJavaModLoadingContext.get().getModEventBus()
			.addListener((ModConfigEvent.Loading event) -> AsyncLocatorModCommon.printConfigs());
	}
}

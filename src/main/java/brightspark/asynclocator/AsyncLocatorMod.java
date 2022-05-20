package brightspark.asynclocator;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

@Mod(AsyncLocatorMod.MOD_ID)
public class AsyncLocatorMod {
	public static final String MOD_ID = "asynclocator";
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String LOG_PREFIX = "Async Locator -> ";

	public AsyncLocatorMod() {
		ModLoadingContext ctx = ModLoadingContext.get();

		// Tells Forge that this mod is only required server side
		ctx.registerExtensionPoint(
			IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				(serverVersion, networkBool) -> true
			)
		);

		ctx.registerConfig(ModConfig.Type.SERVER, AsyncLocatorConfig.SPEC);

		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		forgeEventBus.addListener(AsyncLocator::handleServerStoppingEvent);
		forgeEventBus.addListener(AsyncLocator::handleServerAboutToStartEvent);
	}

	public static void logWarn(String text, Object... args) {
		LOGGER.warn(LOG_PREFIX + text, args);
	}

	public static void logInfo(String text, Object... args) {
		LOGGER.info(LOG_PREFIX + text, args);
	}

	public static void logDebug(String text, Object... args) {
		LOGGER.debug(LOG_PREFIX + text, args);
	}
}

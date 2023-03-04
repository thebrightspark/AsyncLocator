package brightspark.asynclocator;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;

public class AsyncLocatorMod implements ModInitializer {
	public static final String MOD_ID = "asynclocator";
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String LOG_PREFIX = "Async Locator -> ";

	@Override
	public void onInitialize() {


		ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer) -> AsyncLocator.setupExecutorService());
		ServerLifecycleEvents.SERVER_STOPPING.register((minecraftServer) -> AsyncLocator.shutdownExecutorService());
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

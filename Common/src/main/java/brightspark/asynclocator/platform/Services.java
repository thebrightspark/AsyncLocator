package brightspark.asynclocator.platform;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.platform.services.ConfigHelper;
import brightspark.asynclocator.platform.services.ExplorationMapFunctionLogicHelper;
import brightspark.asynclocator.platform.services.PlatformHelper;

import java.util.ServiceLoader;

public class Services {
	public static final PlatformHelper PLATFORM = load(PlatformHelper.class);
	public static final ConfigHelper CONFIG = load(ConfigHelper.class);
	public static final ExplorationMapFunctionLogicHelper EXPLORATION_MAP_FUNCTION_LOGIC =
		load(ExplorationMapFunctionLogicHelper.class);

	private static <T> T load(Class<T> clazz) {
		final T service = ServiceLoader.load(clazz)
			.findFirst()
			.orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
		ALConstants.logDebug("Loaded {} for service {}", service, clazz);
		return service;
	}
}

package brightspark.asynclocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class AsyncLocatorMod implements ModInitializer {
	public static final String MOD_ID = "asynclocator";
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String LOG_PREFIX = "Async Locator -> ";
	public static AsyncLocatorConfig CONFIGURATION;

	@Override
	public void onInitialize() {

		// Configuration
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toString(), "asynclocator.properties");

		try {
			if (configFile.createNewFile()) {

				LOGGER.info(LOG_PREFIX + "creating default config file");
				CONFIGURATION = ConfigFactory.create(AsyncLocatorConfig.class);
				CONFIGURATION.store(new FileOutputStream(configFile), "automatically generated default config file");

			} else {

				LOGGER.info(LOG_PREFIX + "loading config from file");
				Properties props = new Properties();
				props.load(new FileInputStream(configFile));
				CONFIGURATION = ConfigFactory.create(AsyncLocatorConfig.class, props);

			}

		} catch (IOException e) {
			LOGGER.error(LOG_PREFIX + "failed to load config file!");
			e.printStackTrace();
		}
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

package brightspark.asynclocator;

import brightspark.asynclocator.platform.Services;
import brightspark.asynclocator.platform.services.ConfigHelper;

public class AsyncLocatorModCommon {
	public static void printConfigs() {
		ConfigHelper config = Services.CONFIG;
		ALConstants.logInfo("Configs:" +
			"\nLocator Threads: " + config.locatorThreads() +
			"\nRemove Offer: " + config.removeOffer() +
			"\nDolphin Treasure Enabled: " + config.dolphinTreasureEnabled() +
			"\nEye Of Ender Enabled: " + config.eyeOfEnderEnabled() +
			"\nExploration Map Enabled: " + config.explorationMapEnabled() +
			"\nLocate Command Enabled: " + config.locateCommandEnabled() +
			"\nVillager Trade Enabled: " + config.villagerTradeEnabled()
		);
	}
}

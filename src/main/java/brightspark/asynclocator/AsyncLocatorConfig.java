package brightspark.asynclocator;

import org.aeonbits.owner.Accessible;

public interface AsyncLocatorConfig extends Accessible {

	@DefaultValue("1")
	int locatorThreads();

	@DefaultValue("false")
	boolean removeOffer();

	// Feature toggles
	@DefaultValue("true")
	boolean enableDolphinTreasure();

	@DefaultValue("true")
	boolean enableEyeofEnder();

	@DefaultValue("true")
	boolean enableExplorationMap();

	@DefaultValue("true")
	boolean enableLocateCommand();

	@DefaultValue("true")
	boolean enableVillagerTrade();
}

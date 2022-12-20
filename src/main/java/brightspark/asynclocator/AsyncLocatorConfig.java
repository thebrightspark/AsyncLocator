package brightspark.asynclocator;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class AsyncLocatorConfig {
	public static ForgeConfigSpec SPEC;

	public static ConfigValue<Integer> LOCATOR_THREADS;
	public static ConfigValue<Boolean> REMOVE_OFFER;
	// TODO: Add configs to enable/disable each async feature separately

	static {
		SPEC = new Builder()
			.configure(builder -> {
				LOCATOR_THREADS = builder
					.worldRestart()
					.comment("The maximum number of threads in the async locator thread pool.",
						"There's no upper bound to this, however this should only be increased if you're experiencing",
						"simultaneous location lookups causing issues AND you have the hardware capable of handling",
						"the extra possible threads.",
						"The default of 1 should be suitable for most users.")
					.defineInRange("asyncLocatorThreads", 1, 1, Integer.MAX_VALUE);
				REMOVE_OFFER = builder
					.comment("When a merchant's treasure map offer ends up not finding a feature location,",
						"whether the offer should be removed or marked as out of stock.")
					.define("removeMerchantInvalidMapOffer", false);
				return null;
			})
			.getValue();
	}

	private AsyncLocatorConfig() {}
}

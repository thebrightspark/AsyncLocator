package brightspark.asynclocator;

import eu.midnightdust.lib.config.MidnightConfig;

public class AsyncLocatorConfig extends MidnightConfig {

	@Comment
	public static int LOCATOR_THREADS_COMMENT;

	@Entry
	public static int LOCATOR_THREADS = 1;

	@Entry
	public static boolean REMOVE_OFFER = true;

	@Entry
	// Feature toggles
	public static boolean DOLPHIN_TREASURE_ENABLED = true;

	@Entry
	public static boolean EYE_OF_ENDER_ENABLED = true;

	@Entry
	public static boolean EXPLORATION_MAP_ENABLED = true;

	@Entry
	public static boolean LOCATE_COMMAND_ENABLED = true;

	@Entry
	public static boolean VILLAGER_TRADE_ENABLED = true;

	static {
//		SPEC = new Builder()
//			.configure(builder -> {
//				REMOVE_OFFER = builder
//					.comment(
//						"When a merchant's treasure map offer ends up not finding a feature location,",
//						"whether the offer should be removed or marked as out of stock."
//					)
//					.define("removeMerchantInvalidMapOffer", false);
//
//				builder.push("Feature Toggles");
//				DOLPHIN_TREASURE_ENABLED = builder
//					.comment("If true, enables asynchronous locating of structures for dolphin treasures.")
//					.define("dolphinTreasureEnabled", true);
//				EYE_OF_ENDER_ENABLED = builder
//					.comment("If true, enables asynchronous locating of structures when Eyes Of Ender are thrown.")
//					.define("eyeOfEnderEnabled", true);
//				EXPLORATION_MAP_ENABLED = builder
//					.comment(
//						"If true, enables asynchronous locating of structures for exploration maps found in chests.")
//					.define("explorationMspEnabled", true);
//				LOCATE_COMMAND_ENABLED = builder
//					.comment("If true, enables asynchronous locating of structures for the locate command.")
//					.define("locateCommandEnabled", true);
//				VILLAGER_TRADE_ENABLED = builder
//					.comment("If true, enables asynchronous locating of structures for villager trades.")
//					.define("villagerTradeEnabled", true);
//				builder.pop();
//				return null;
//			})
//			.getValue();
	}

	private AsyncLocatorConfig() {}
}

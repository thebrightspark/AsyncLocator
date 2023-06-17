package brightspark.asynclocator.platform.services;

public interface ConfigHelper {
	/**
	 * The maximum number of threads in the async locator thread pool.
	 */
	int locatorThreads();

	/**
	 * When a merchant's treasure map offer ends up not finding a feature location, whether the offer should be removed
	 * or marked as out of stock.
	 */
	boolean removeOffer();

	/**
	 * If true, enables asynchronous locating of structures for dolphin treasures.
	 */
	boolean dolphinTreasureEnabled();

	/**
	 * If true, enables asynchronous locating of structures when Eyes Of Ender are thrown.
	 */
	boolean eyeOfEnderEnabled();

	/**
	 * If true, enables asynchronous locating of structures for exploration maps found in chests.
	 */
	boolean explorationMapEnabled();

	/**
	 * If true, enables asynchronous locating of structures for the locate command.
	 */
	boolean locateCommandEnabled();

	/**
	 * If true, enables asynchronous locating of structures for villager trades.
	 */
	boolean villagerTradeEnabled();
}

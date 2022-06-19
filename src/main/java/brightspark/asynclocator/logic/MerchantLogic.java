package brightspark.asynclocator.logic;

import brightspark.asynclocator.AsyncLocator;
import brightspark.asynclocator.AsyncLocatorConfig;
import brightspark.asynclocator.AsyncLocatorMod;
import brightspark.asynclocator.mixins.MerchantOfferAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

public class MerchantLogic {
	private MerchantLogic() {}

	/**
	 * @deprecated Use {@link CommonLogic#createEmptyMap()} instead
	 */
	@Deprecated(since = "1.1.0", forRemoval = true)
	public static ItemStack createEmptyMap() {
		return CommonLogic.createEmptyMap();
	}

	public static void invalidateMap(AbstractVillager merchant, ItemStack mapStack) {
		mapStack.setHoverName(new TranslatableComponent("asynclocator.map.none"));
		merchant.getOffers()
			.stream()
			.filter(offer -> offer.getResult() == mapStack)
			.findFirst()
			.ifPresentOrElse(
				offer -> removeOffer(merchant, offer),
				() -> AsyncLocatorMod.logWarn("Failed to find merchant offer for map")
			);
	}

	public static void removeOffer(AbstractVillager merchant, MerchantOffer offer) {
		if (AsyncLocatorConfig.REMOVE_OFFER.get()) {
			if (merchant.getOffers().remove(offer)) AsyncLocatorMod.logInfo("Removed merchant map offer");
			else AsyncLocatorMod.logWarn("Failed to remove merchant map offer");
		} else {
			((MerchantOfferAccess) offer).setMaxUses(0);
			offer.setToOutOfStock();
			AsyncLocatorMod.logInfo("Marked merchant map offer as out of stock");
		}
	}

	/**
	 * @deprecated Use {@link CommonLogic#updateMap(ItemStack, ServerLevel, BlockPos, int, MapDecoration.Type, String)} instead
	 */
	@Deprecated(since = "1.1.0", forRemoval = true)
	public static void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		String displayName,
		MapDecoration.Type destinationType
	) {
		CommonLogic.updateMap(mapStack, level, pos, 2, destinationType, displayName);
	}

	public static void handleLocationFound(
		ServerLevel level,
		AbstractVillager merchant,
		ItemStack mapStack,
		String displayName,
		MapDecoration.Type destinationType,
		BlockPos pos
	) {
		if (pos == null) {
			AsyncLocatorMod.logInfo("No location found - invalidating merchant offer");

			invalidateMap(merchant, mapStack);
		} else {
			AsyncLocatorMod.logInfo("Location found - updating treasure map in merchant offer");

			CommonLogic.updateMap(mapStack, level, pos, 2, destinationType, displayName);
		}

		if (merchant.getTradingPlayer() instanceof ServerPlayer tradingPlayer) {
			AsyncLocatorMod.logInfo("Player {} currently trading - updating merchant offers", tradingPlayer);

			tradingPlayer.sendMerchantOffers(
				tradingPlayer.containerMenu.containerId,
				merchant.getOffers(),
				merchant instanceof Villager villager ? villager.getVillagerData().getLevel() : 1,
				merchant.getVillagerXp(),
				merchant.showProgressBar(),
				merchant.canRestock()
			);
		}
	}

	public static MerchantOffer updateMapAsync(
		Entity pTrader,
		int emeraldCost,
		String displayName,
		MapDecoration.Type destinationType,
		int maxUses,
		int villagerXp,
		TagKey<ConfiguredStructureFeature<?, ?>> destination
	) {
		return updateMapAsyncInternal(
			pTrader,
			emeraldCost,
			maxUses,
			villagerXp,
			(level, merchant, mapStack) -> AsyncLocator.locate(level, destination, merchant.blockPosition(), 100, true)
				.thenOnServerThread(pos -> handleLocationFound(
					level,
					merchant,
					mapStack,
					displayName,
					destinationType,
					pos
				))
		);
	}

	public static MerchantOffer updateMapAsync(
		Entity pTrader,
		int emeraldCost,
		String displayName,
		MapDecoration.Type destinationType,
		int maxUses,
		int villagerXp,
		HolderSet<ConfiguredStructureFeature<?, ?>> structureSet
	) {
		return updateMapAsyncInternal(
			pTrader,
			emeraldCost,
			maxUses,
			villagerXp,
			(level, merchant, mapStack) -> AsyncLocator.locate(level, structureSet, merchant.blockPosition(), 100, true)
				.thenOnServerThread(pair -> handleLocationFound(
					level,
					merchant,
					mapStack,
					displayName,
					destinationType,
					pair.getFirst()
				))
		);
	}

	private static MerchantOffer updateMapAsyncInternal(
		Entity trader, int emeraldCost, int maxUses, int villagerXp, MapUpdateTask task
	) {
		if (trader instanceof AbstractVillager merchant) {
			ItemStack mapStack = CommonLogic.createEmptyMap();
			task.apply((ServerLevel) trader.level, merchant, mapStack);

			return new MerchantOffer(
				new ItemStack(Items.EMERALD, emeraldCost),
				new ItemStack(Items.COMPASS),
				mapStack,
				maxUses,
				villagerXp,
				0.2F
			);
		} else {
			AsyncLocatorMod.logInfo(
				"Merchant is not of type {} - not running async logic",
				AbstractVillager.class.getSimpleName()
			);
			return null;
		}
	}

	public interface MapUpdateTask {
		void apply(ServerLevel level, AbstractVillager merchant, ItemStack mapStack);
	}
}

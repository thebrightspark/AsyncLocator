package brightspark.asynclocator.mixins;

import brightspark.asynclocator.AsyncLocatorMod;
import brightspark.asynclocator.logic.MerchantLogic;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(targets = "net.minecraft.world.entity.npc.VillagerTrades$TreasureMapForEmeralds")
public class TreasureMapForEmeraldsMixin {
	@Shadow
	@Final
	private int emeraldCost;

	@Shadow
	@Final
	private String displayName;

	@Shadow
	@Final
	private MapDecoration.Type destinationType;

	@Shadow
	@Final
	private int maxUses;

	@Shadow
	@Final
	private int villagerXp;

	@Shadow
	@Final
	private TagKey<ConfiguredStructureFeature<?, ?>> destination;

	/*
		Intercept TreasureMapForEmeralds#getOffer call right before it calls ServerLevel#findNearestMapFeature to pass
		the logic over to an async task. Instead of returning the complete map or null, we'll have to always return an
	 	incomplete filled map and later update it with the details when we have them.
	 */
	@Inject(
		method = "getOffer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;findNearestMapFeature(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"
		),
		cancellable = true
	)
	public void updateMapAsync(Entity pTrader, Random pRand, CallbackInfoReturnable<MerchantOffer> callbackInfo) {
		AsyncLocatorMod.logDebug("Intercepted TreasureMapForEmeralds#getOffer call");
		MerchantOffer offer = MerchantLogic.updateMapAsync(
			pTrader, emeraldCost, displayName, destinationType, maxUses, villagerXp, destination
		);
		if (offer != null) {
			callbackInfo.setReturnValue(offer);
		}
	}
}

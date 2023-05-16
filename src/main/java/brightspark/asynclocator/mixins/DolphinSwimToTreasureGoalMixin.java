package brightspark.asynclocator.mixins;

import brightspark.asynclocator.AsyncLocator;
import brightspark.asynclocator.AsyncLocator.LocateTask;
import brightspark.asynclocator.AsyncLocatorMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.animal.Dolphin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.entity.animal.Dolphin$DolphinSwimToTreasureGoal")
public class DolphinSwimToTreasureGoalMixin {
	@Final
	@Shadow
	private Dolphin dolphin;

	@Shadow
	private boolean stuck;

	private LocateTask<BlockPos> locateTask = null;

	/*
		Intercept DolphinSwimToTreasureGoal#start call right before it calls ServerLevel#findNearestMapFeature to pass
		the logic over to an async task.
	 */
	@Inject(
		method = "start",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"
		),
		cancellable = true,
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	public void findTreasureAsync(CallbackInfo ci, ServerLevel level, BlockPos blockpos) {
		if (!AsyncLocatorMod.CONFIGURATION.enableDolphinTreasure()) return;

		AsyncLocatorMod.logDebug("Intercepted DolphinSwimToTreasureGoal#start call");
		handleFindTreasureAsync(level, blockpos);
		ci.cancel();
	}

	/*
		Intercept DolphinSwimToTreasureGoal#canContinueToUse to return true if an async locating task is ongoing so that
		the goal isn't ended early due to no treasure pos being set yet.
	 */
	@Inject(
		method = "canContinueToUse",
		at = @At(value = "HEAD"),
		cancellable = true
	)
	public void continueToUseIfLocatingTreasure(CallbackInfoReturnable<Boolean> cir) {
		if (locateTask != null) {
			AsyncLocatorMod.logDebug("Locating task ongoing - returning true for continueToUse()");
			cir.setReturnValue(true);
		}
	}

	@Inject(
		method = "stop",
		at = @At(value = "HEAD")
	)
	public void stopLocatingTreasure(CallbackInfo ci) {
		if (locateTask != null) {
			AsyncLocatorMod.logDebug("Locating task ongoing - cancelling during stop()");
			locateTask.cancel();
			locateTask = null;
		}
	}

	/*
		Intercept DolphinSwimToTreasureGoal#tick to return early if an async locating task is ongoing so that the
		dolphin doesn't try to go towards an old treasure position.
	 */
	@Inject(
		method = "tick",
		at = @At(value = "HEAD"),
		cancellable = true
	)
	public void skipTickingIfLocatingTreasure(CallbackInfo ci) {
		if (locateTask != null) {
			AsyncLocatorMod.logDebug("Locating task ongoing - skipping tick()");
			ci.cancel();
		}
	}

	private void handleFindTreasureAsync(ServerLevel level, BlockPos blockPos) {
		locateTask = AsyncLocator.locate(level, StructureTags.DOLPHIN_LOCATED, blockPos, 50, false)
			.thenOnServerThread(pos -> handleLocationFound(level, pos));
	}

	private void handleLocationFound(ServerLevel level, BlockPos pos) {
		locateTask = null;
		if (pos != null) {
			AsyncLocatorMod.logInfo("Location found - updating dolphin treasure pos");
			dolphin.setTreasurePos(pos);
			level.broadcastEntityEvent(dolphin, (byte) 38);
		} else {
			AsyncLocatorMod.logInfo("No location found - marking dolphin as stuck");
			stuck = true;
		}
	}
}

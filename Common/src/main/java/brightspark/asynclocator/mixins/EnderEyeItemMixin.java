package brightspark.asynclocator.mixins;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.logic.EnderEyeItemLogic;
import brightspark.asynclocator.platform.Services;
import net.minecraft.advancements.critereon.UsedEnderEyeTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {
	/*
		Intercept EnderEyeItem#use call and return BlockPos.ZERO instead. It won't be used in the EyeOfEnder entity
		created later either, as we need to set the actual location ourselves.
	 */
	@Redirect(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"
		)
	)
	public BlockPos levelFindNearestMapFeature(
		ServerLevel serverlevel,
		TagKey<Structure> pStructureTag,
		BlockPos pPos,
		int pRadius,
		boolean pSkipExistingChunks
	) {
		if (Services.CONFIG.eyeOfEnderEnabled()) {
			ALConstants.logDebug("Intercepted EnderEyeItem#use call");
			return BlockPos.ZERO;
		} else {
			// Normal behaviour
			return serverlevel.findNearestMapStructure(pStructureTag, pPos, pRadius, pSkipExistingChunks);
		}
	}

	// Start the async locate task here so we have the eye of ender entity for context
	@Inject(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/EyeOfEnder;setItem(Lnet/minecraft/world/item/ItemStack;)V"
		),
		locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	public void startAsyncLocateTask(
		Level pLevel,
		Player pPlayer,
		InteractionHand pHand,
		CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
		ItemStack itemstack,
		BlockHitResult blockhitresult,
		ServerLevel serverlevel,
		BlockPos blockpos,
		EyeOfEnder eyeofender
	) {
		if (!Services.CONFIG.eyeOfEnderEnabled()) return;
		//noinspection DataFlowIssue
		EnderEyeItemLogic.locateAsync(serverlevel, pPlayer, eyeofender, (EnderEyeItem) (Object) this);
	}

	@Redirect(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/EyeOfEnder;signalTo(Lnet/minecraft/core/BlockPos;)V"
		)
	)
	public void eyeOfEnderSignalTo(EyeOfEnder eyeOfEnder, BlockPos blockpos) {
		if (!Services.CONFIG.eyeOfEnderEnabled())
			eyeOfEnder.signalTo(blockpos);
		// Else do nothing - we'll do this later if a location is found
	}

	@Redirect(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/critereon/UsedEnderEyeTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;)V"
		)
	)
	public void triggerUsedEnderEyeCriteria(UsedEnderEyeTrigger trigger, ServerPlayer player, BlockPos pos) {
		if (!Services.CONFIG.eyeOfEnderEnabled())
			trigger.trigger(player, pos);
		// Else do nothing - we'll do this later if a location is found
	}

	@Redirect(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"
		)
	)
	public void playerAwardStat(Player player, Stat<?> pStat) {
		if (!Services.CONFIG.eyeOfEnderEnabled())
			player.awardStat(pStat);
		// Else do nothing - we'll do this later if a location is found
	}
}

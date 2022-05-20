package brightspark.asynclocator.mixins;

import brightspark.asynclocator.AsyncLocatorMod;
import brightspark.asynclocator.logic.LocateCommandLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {
	@Inject(
		method = "locate",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;findNearestMapFeature(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/BlockPos;IZ)Lcom/mojang/datafixers/util/Pair;"
		),
		cancellable = true,
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private static void findLocationAsync(CommandSourceStack sourceStack, ResourceOrTagLocationArgument.Result<ConfiguredStructureFeature<?, ?>> argResult, CallbackInfoReturnable<Integer> cir, Registry<ConfiguredStructureFeature<?, ?>> registry, HolderSet<ConfiguredStructureFeature<?, ?>> holderset) {
		AsyncLocatorMod.logDebug("Intercepted LocateCommand#locate call");
		LocateCommandLogic.locateAsync(sourceStack, argResult, holderset);
		cir.setReturnValue(0);
	}
}

package brightspark.asynclocator.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import brightspark.asynclocator.AsyncLocatorMod;
import brightspark.asynclocator.logic.LocateCommandLogic;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {
	@Inject(
		method = "locateStructure",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;findNearestMapStructure(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/BlockPos;IZ)Lcom/mojang/datafixers/util/Pair;"
		),
		cancellable = true,
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private static void findLocationAsync(
		CommandSourceStack sourceStack,
		ResourceOrTagKeyArgument.Result<Structure> structureResult,
		CallbackInfoReturnable<Integer> cir,
		Registry<Structure> registry,
		HolderSet<Structure> holderset
	) {
		if (!AsyncLocatorMod.CONFIGURATION.enableLocateCommand()) return;

		CommandSource source = ((CommandSourceStackAccess) sourceStack).getSource();
		if (source instanceof ServerPlayer || source instanceof MinecraftServer) {
			AsyncLocatorMod.logDebug("Intercepted LocateCommand#locate call");
			LocateCommandLogic.locateAsync(sourceStack, structureResult, holderset);
			cir.setReturnValue(0);
		}
	}
}

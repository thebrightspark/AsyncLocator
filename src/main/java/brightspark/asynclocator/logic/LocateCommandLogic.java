package brightspark.asynclocator.logic;

import com.google.common.base.Stopwatch;

import brightspark.asynclocator.AsyncLocator;
import brightspark.asynclocator.AsyncLocatorMod;
import brightspark.asynclocator.mixins.LocateCommandAccess;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.levelgen.structure.Structure;

public class LocateCommandLogic {
	private LocateCommandLogic() {}

	public static void locateAsync(
		CommandSourceStack sourceStack,
		ResourceOrTagKeyArgument.Result<Structure> structureResult,
		HolderSet<Structure> holderset
	) {
		BlockPos originPos = BlockPos.containing(sourceStack.getPosition());
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
		AsyncLocator.locate(sourceStack.getLevel(), holderset, originPos, 100, false)
			.thenOnServerThread(pair -> {
				if (pair != null) {
					AsyncLocatorMod.logInfo("Location found - sending success back to command source");
					LocateCommand.showLocateResult(
						sourceStack,
						structureResult,
						originPos,
						pair,
						"commands.locate.structure.success",
						false, stopwatch.elapsed()
					);
				} else {
					AsyncLocatorMod.logInfo("No location found - sending failure back to command source");
					sourceStack.sendFailure(Component.literal(
						LocateCommandAccess.getErrorFailed().create(structureResult.asPrintable()).getMessage()
					));
				}
			});
	}
}

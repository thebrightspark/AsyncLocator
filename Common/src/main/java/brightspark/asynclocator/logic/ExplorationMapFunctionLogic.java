package brightspark.asynclocator.logic;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.AsyncLocator;
import brightspark.asynclocator.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

public class ExplorationMapFunctionLogic {
	private ExplorationMapFunctionLogic() {}

	public static void handleLocationFound(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		MapDecoration.Type destinationType,
		BlockPos invPos
	) {
		if (pos == null) {
			ALConstants.logInfo("No location found - invalidating map stack");
			Services.EXPLORATION_MAP_FUNCTION_LOGIC.invalidateMap(mapStack, level, invPos);
		} else {
			ALConstants.logInfo("Location found - updating treasure map in chest");
			Services.EXPLORATION_MAP_FUNCTION_LOGIC.updateMap(mapStack, level, pos, scale, destinationType, invPos);
		}
	}

	public static ItemStack updateMapAsync(
		ServerLevel level,
		BlockPos blockPos,
		int scale,
		int searchRadius,
		boolean skipKnownStructures,
		MapDecoration.Type destinationType,
		TagKey<Structure> destination
	) {
		ItemStack mapStack = CommonLogic.createEmptyMap();
		AsyncLocator.locate(level, destination, blockPos, searchRadius, skipKnownStructures)
			.thenOnServerThread(pos -> handleLocationFound(mapStack, level, pos, scale, destinationType, blockPos));
		return mapStack;
	}
}

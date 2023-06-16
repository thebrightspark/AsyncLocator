package brightspark.asynclocator.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

public interface ExplorationMapFunctionLogicHelper {
	void invalidateMap(ItemStack mapStack, ServerLevel level, BlockPos pos);

	void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		MapDecoration.Type destinationType,
		BlockPos invPos
	);
}

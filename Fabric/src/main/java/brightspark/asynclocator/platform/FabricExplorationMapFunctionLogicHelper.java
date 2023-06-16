package brightspark.asynclocator.platform;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.logic.CommonLogic;
import brightspark.asynclocator.platform.services.ExplorationMapFunctionLogicHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.function.BiConsumer;

public class FabricExplorationMapFunctionLogicHelper implements ExplorationMapFunctionLogicHelper {
	@Override
	public void invalidateMap(ItemStack mapStack, ServerLevel level, BlockPos pos) {
		handleUpdateMapInChest(mapStack, level, pos, (chest, slot) -> chest.setItem(slot, new ItemStack(Items.MAP)));
	}

	@Override
	public void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		MapDecoration.Type destinationType,
		BlockPos invPos
	) {
		CommonLogic.updateMap(mapStack, level, pos, scale, destinationType);
		// Shouldn't need to set the stack in its slot again, as we're modifying the same instance
		handleUpdateMapInChest(mapStack, level, invPos, (chest, slot) -> {});
	}

	private static void handleUpdateMapInChest(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos invPos,
		BiConsumer<ChestBlockEntity, Integer> handleSlotFound
	) {
		BlockEntity be = level.getBlockEntity(invPos);
		if (be instanceof ChestBlockEntity chest) {
			for (int i = 0; i < chest.getContainerSize(); i++) {
				ItemStack slotStack = chest.getItem(i);
				if (slotStack == mapStack) {
					handleSlotFound.accept(chest, i);
					CommonLogic.broadcastChestChanges(level, be);
					return;
				}
			}
		} else {
			ALConstants.logWarn(
				"Couldn't find chest block entity on block {} at {}",
				level.getBlockState(invPos), invPos
			);
		}
	}
}

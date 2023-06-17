package brightspark.asynclocator.platform;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.logic.CommonLogic;
import brightspark.asynclocator.platform.services.ExplorationMapFunctionLogicHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.BiConsumer;

public class ForgeExplorationMapFunctionLogicHelper implements ExplorationMapFunctionLogicHelper {
	@Override
	public void invalidateMap(ItemStack mapStack, ServerLevel level, BlockPos pos) {
		handleUpdateMapInChest(mapStack, level, pos, (handler, slot) -> {
			if (handler instanceof IItemHandlerModifiable modifiableHandler) {
				modifiableHandler.setStackInSlot(slot, new ItemStack(Items.MAP));
			} else {
				handler.extractItem(slot, Item.MAX_STACK_SIZE, false);
				handler.insertItem(slot, new ItemStack(Items.MAP), false);
			}
		});
	}

	@Override
	public void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		MapDecoration.Type destinationType,
		BlockPos invPos,
		Component displayName
	) {
		CommonLogic.updateMap(mapStack, level, pos, scale, destinationType, displayName);
		// Shouldn't need to set the stack in its slot again, as we're modifying the same instance
		handleUpdateMapInChest(mapStack, level, invPos, (handler, slot) -> {});
	}

	private static void handleUpdateMapInChest(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos invPos,
		BiConsumer<IItemHandler, Integer> handleSlotFound
	) {
		BlockEntity be = level.getBlockEntity(invPos);
		if (be != null) {
			be.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().ifPresentOrElse(
				itemHandler -> {
					for (int i = 0; i < itemHandler.getSlots(); i++) {
						ItemStack slotStack = itemHandler.getStackInSlot(i);
						if (slotStack == mapStack) {
							handleSlotFound.accept(itemHandler, i);
							CommonLogic.broadcastChestChanges(level, be);
							return;
						}
					}
				},
				() -> ALConstants.logWarn(
					"Couldn't find item handler capability on chest {} at {}",
					be.getClass().getSimpleName(), invPos
				)
			);
		} else {
			ALConstants.logWarn(
				"Couldn't find block entity on chest {} at {}",
				level.getBlockState(invPos), invPos
			);
		}
	}
}

package brightspark.asynclocator.logic;

import brightspark.asynclocator.AsyncLocator;
import brightspark.asynclocator.AsyncLocatorMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

// TODO: Need to test this
public class ExplorationMapFunctionLogic {
	private ExplorationMapFunctionLogic() {}
//
//	public static void invalidateMap(ItemStack mapStack, ServerLevel level, BlockPos pos) {
//		handleUpdateMapInChest(mapStack, level, pos, (handler, slot) -> {
//			if (handler instanceof IItemHandlerModifiable modifiableHandler) {
//				modifiableHandler.setStackInSlot(slot, new ItemStack(Items.MAP));
//			} else {
//				handler.extractItem(slot, Item.MAX_STACK_SIZE, false);
//				handler.insertItem(slot, new ItemStack(Items.MAP), false);
//			}
//		});
//	}
//
//	public static void updateMap(
//		ItemStack mapStack,
//		ServerLevel level,
//		BlockPos pos,
//		int scale,
//		MapDecoration.Type destinationType,
//		BlockPos invPos
//	) {
//		CommonLogic.updateMap(mapStack, level, pos, scale, destinationType);
//		// Shouldn't need to set the stack in its slot again, as we're modifying the same instance
//		handleUpdateMapInChest(mapStack, level, invPos, (handler, slot) -> {});
//	}
//
//	public static void handleUpdateMapInChest(
//		ItemStack mapStack,
//		ServerLevel level,
//		BlockPos invPos,
//		BiConsumer<IItemHandler, Integer> handleSlotFound
//	) {
//		BlockEntity be = level.getBlockEntity(invPos);
//		if (be != null) {
//			be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().ifPresentOrElse(
//				itemHandler -> {
//					for (int i = 0; i < itemHandler.getSlots(); i++) {
//						ItemStack slotStack = itemHandler.getStackInSlot(i);
//						if (slotStack == mapStack) {
//							handleSlotFound.accept(itemHandler, i);
//							CommonLogic.broadcastChestChanges(level, be);
//							return;
//						}
//					}
//				},
//				() -> AsyncLocatorMod.logWarn(
//					"Couldn't find item handler capability on chest {} at {}",
//					be.getClass().getSimpleName(), invPos
//				)
//			);
//		} else {
//			AsyncLocatorMod.logWarn(
//				"Couldn't find block entity on chest {} at {}",
//				level.getBlockState(invPos), invPos
//			);
//		}
//	}
//
//	public static void handleLocationFound(
//		ItemStack mapStack,
//		ServerLevel level,
//		BlockPos pos,
//		int scale,
//		MapDecoration.Type destinationType,
//		BlockPos invPos
//	) {
//		if (pos == null) {
//			AsyncLocatorMod.logInfo("No location found - invalidating map stack");
//			invalidateMap(mapStack, level, invPos);
//		} else {
//			AsyncLocatorMod.logInfo("Location found - updating treasure map in chest");
//			updateMap(mapStack, level, pos, scale, destinationType, invPos);
//		}
//	}
//
//	public static ItemStack updateMapAsync(
//		ServerLevel level,
//		BlockPos blockPos,
//		int scale,
//		int searchRadius,
//		boolean skipKnownStructures,
//		MapDecoration.Type destinationType,
//		TagKey<Structure> destination
//	) {
//		ItemStack mapStack = CommonLogic.createEmptyMap();
//		AsyncLocator.locate(level, destination, blockPos, searchRadius, skipKnownStructures)
//			.thenOnServerThread(pos -> handleLocationFound(mapStack, level, pos, scale, destinationType, blockPos));
//		return mapStack;
//	}
}

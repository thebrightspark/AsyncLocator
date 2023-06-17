package brightspark.asynclocator.logic;

import brightspark.asynclocator.mixins.MapItemAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CommonLogic {
	private static final String MAP_HOVER_NAME_KEY = "menu.working";
	private static final String KEY_LOCATING = "asynclocator.locating";

	private CommonLogic() {}

	/**
	 * Creates an empty "Filled Map", with a hover tooltip name stating that it's locating a feature.
	 *
	 * @return The ItemStack
	 */
	public static ItemStack createEmptyMap() {
		ItemStack stack = new ItemStack(Items.FILLED_MAP);
		stack.setHoverName(Component.translatable(MAP_HOVER_NAME_KEY));
		stack.addTagElement(KEY_LOCATING, ByteTag.ONE);
		return stack;
	}

	/**
	 * Returns true if the stack is an empty FILLED_MAP item with the hover tooltip name stating that it's locating a
	 * feature.
	 *
	 * @param stack The stack to check.
	 * @return True if the stack is an empty FILLED_MAP awaiting to be populated with location data.
	 */
	@SuppressWarnings("DataFlowIssue")
	public static boolean isEmptyPendingMap(ItemStack stack) {
		return stack.is(Items.FILLED_MAP) && stack.hasTag() && stack.getTag().contains(KEY_LOCATING);
	}

	/**
	 * Updates the map stack with all the given data.
	 *
	 * @param mapStack        The map ItemStack to update
	 * @param level           The ServerLevel
	 * @param pos             The feature position
	 * @param scale           The map scale
	 * @param destinationType The map feature type
	 */
	public static void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		MapDecoration.Type destinationType
	) {
		updateMap(mapStack, level, pos, scale, destinationType, (Component) null);
	}

	/**
	 * Updates the map stack with all the given data.
	 *
	 * @param mapStack        The map ItemStack to update
	 * @param level           The ServerLevel
	 * @param pos             The feature position
	 * @param scale           The map scale
	 * @param destinationType The map feature type
	 * @param displayName     The hover tooltip display name of the ItemStack
	 */
	public static void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		MapDecoration.Type destinationType,
		String displayName
	) {
		updateMap(mapStack, level, pos, scale, destinationType, Component.translatable(displayName));
	}

	/**
	 * Updates the map stack with all the given data.
	 *
	 * @param mapStack        The map ItemStack to update
	 * @param level           The ServerLevel
	 * @param pos             The feature position
	 * @param scale           The map scale
	 * @param destinationType The map feature type
	 * @param displayName     The hover tooltip display name of the ItemStack
	 */
	public static void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		MapDecoration.Type destinationType,
		Component displayName
	) {
		MapItemAccess.callCreateAndStoreSavedData(
			mapStack, level, pos.getX(), pos.getZ(), scale, true, true, level.dimension()
		);
		MapItem.renderBiomePreviewMap(level, mapStack);
		MapItemSavedData.addTargetDecoration(mapStack, pos, "+", destinationType);
		if (displayName != null)
			mapStack.setHoverName(displayName);
		mapStack.removeTagKey(KEY_LOCATING);
	}

	/**
	 * Broadcasts slot changes to all players that have the chest container open.
	 * Won't do anything if the BlockEntity isn't an instance of {@link ChestBlockEntity}.
	 */
	public static void broadcastChestChanges(ServerLevel level, BlockEntity be) {
		if (!(be instanceof ChestBlockEntity))
			return;

		level.players().forEach(player -> {
			AbstractContainerMenu container = player.containerMenu;
			if (container instanceof ChestMenu chestMenu && chestMenu.getContainer() == be) {
				chestMenu.broadcastChanges();
			}
		});
	}
}

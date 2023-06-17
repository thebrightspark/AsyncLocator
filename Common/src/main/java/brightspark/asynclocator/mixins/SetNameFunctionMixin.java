package brightspark.asynclocator.mixins;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.logic.CommonLogic;
import brightspark.asynclocator.logic.ExplorationMapFunctionLogic;
import brightspark.asynclocator.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SetNameFunction.class)
public class SetNameFunctionMixin {
	@Redirect(
		method = "run",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;setHoverName(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;"
		)
	)
	public ItemStack deferSetName(ItemStack stack, Component name) {
		if (Services.CONFIG.explorationMapEnabled()) {
			ALConstants.logDebug("Intercepted SetNameFunction#run call");
			if (CommonLogic.isEmptyPendingMap(stack))
				ExplorationMapFunctionLogic.cacheName(stack, name);
		} else
			stack.setHoverName(name);
		return stack;
	}
}

package brightspark.asynclocator.mixins;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.commands.LocateCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocateCommand.class)
public interface LocateCommandAccess {
	@Accessor("ERROR_STRUCTURE_NOT_FOUND")
	static DynamicCommandExceptionType getErrorFailed() {
		throw new UnsupportedOperationException();
	}
}

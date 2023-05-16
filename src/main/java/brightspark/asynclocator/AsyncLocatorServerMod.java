package brightspark.asynclocator;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class AsyncLocatorServerMod implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(AsyncLocator::setupExecutorService);
		ServerLifecycleEvents.SERVER_STOPPING.register(AsyncLocator::setupExecutorService);
	}

}

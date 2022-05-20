package brightspark.asynclocator;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AsyncLocator {
	private static ExecutorService LOCATING_EXECUTOR_SERVICE = null;

	private AsyncLocator() {}

	private static void setupExecutorService() {
		shutdownExecutorService();

		int threads = AsyncLocatorConfig.LOCATOR_THREADS.get();
		AsyncLocatorMod.logInfo("Starting locating executor service with thread pool size of {}", threads);
		LOCATING_EXECUTOR_SERVICE = Executors.newFixedThreadPool(
			threads,
			new ThreadFactory() {
				private static final AtomicInteger poolNum = new AtomicInteger(1);
				private final AtomicInteger threadNum = new AtomicInteger(1);
				private final String namePrefix = "asynclocator-" + poolNum.getAndIncrement() + "-thread-";

				@Override
				public Thread newThread(@NotNull Runnable r) {
					return new Thread(SidedThreadGroups.SERVER, r, namePrefix + threadNum.getAndIncrement());
				}
			}
		);
	}

	private static void shutdownExecutorService() {
		if (LOCATING_EXECUTOR_SERVICE != null) {
			AsyncLocatorMod.logInfo("Shutting down locating executor service");
			LOCATING_EXECUTOR_SERVICE.shutdown();
		}
	}

	public static void handleServerAboutToStartEvent(ServerAboutToStartEvent ignoredEvent) {
		setupExecutorService();
	}

	public static void handleServerStoppingEvent(ServerStoppingEvent ignoredEvent) {
		shutdownExecutorService();
	}

	/**
	 * Queues a task to locate a feature using {@link ServerLevel#findNearestMapFeature(TagKey, BlockPos, int, boolean)}
	 * and returns a {@link LocateTask} with the futures for it.
	 */
	public static LocateTask<BlockPos> locate(
		ServerLevel level,
		TagKey<ConfiguredStructureFeature<?, ?>> structureTag,
		BlockPos pos,
		int searchRadius,
		boolean skipKnownStructures
	) {
		AsyncLocatorMod.logDebug(
			"Creating locate task for {} in {} around {} within {} chunks",
			structureTag, level, pos, searchRadius
		);
		CompletableFuture<BlockPos> completableFuture = new CompletableFuture<>();
		Future<?> future = LOCATING_EXECUTOR_SERVICE.submit(
			() -> doLocateLevel(completableFuture, level, structureTag, pos, searchRadius, skipKnownStructures)
		);
		return new LocateTask<>(level.getServer(), completableFuture, future);
	}

	/**
	 * Queues a task to locate a feature using
	 * {@link ChunkGenerator#findNearestMapFeature(ServerLevel, HolderSet, BlockPos, int, boolean)} and returns a
	 * {@link LocateTask} with the futures for it.
	 */
	public static LocateTask<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> locate(
		ServerLevel level,
		HolderSet<ConfiguredStructureFeature<?, ?>> structureSet,
		BlockPos pos,
		int searchRadius,
		boolean skipKnownStructures
	) {
		AsyncLocatorMod.logDebug(
			"Creating locate task for {} in {} around {} within {} chunks",
			structureSet, level, pos, searchRadius
		);
		CompletableFuture<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> completableFuture = new CompletableFuture<>();
		Future<?> future = LOCATING_EXECUTOR_SERVICE.submit(
			() -> doLocateChunkGenerator(completableFuture, level, structureSet, pos, searchRadius, skipKnownStructures)
		);
		return new LocateTask<>(level.getServer(), completableFuture, future);
	}

	private static void doLocateLevel(
		CompletableFuture<BlockPos> completableFuture,
		ServerLevel level,
		TagKey<ConfiguredStructureFeature<?, ?>> structureTag,
		BlockPos pos,
		int searchRadius,
		boolean skipExistingChunks
	) {
		AsyncLocatorMod.logInfo(
			"Trying to locate {} in {} around {} within {} chunks",
			structureTag, level, pos, searchRadius
		);
		BlockPos foundPos = level.findNearestMapFeature(structureTag, pos, searchRadius, skipExistingChunks);
		if (foundPos == null)
			AsyncLocatorMod.logInfo("No {} found", structureTag);
		else
			AsyncLocatorMod.logInfo("Found {} at {}", structureTag, foundPos);
		completableFuture.complete(foundPos);
	}

	private static void doLocateChunkGenerator(
		CompletableFuture<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> completableFuture,
		ServerLevel level,
		HolderSet<ConfiguredStructureFeature<?, ?>> structureSet,
		BlockPos pos,
		int searchRadius,
		boolean skipExistingChunks
	) {
		AsyncLocatorMod.logInfo(
			"Trying to locate {} in {} around {} within {} chunks",
			structureSet, level, pos, searchRadius
		);
		Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> foundPair = level.getChunkSource().getGenerator()
			.findNearestMapFeature(level, structureSet, pos, searchRadius, skipExistingChunks);
		if (foundPair == null)
			AsyncLocatorMod.logInfo("No {} found", structureSet);
		else
			AsyncLocatorMod.logInfo("Found {} at {}", foundPair.getSecond().value(), foundPair.getFirst());
		completableFuture.complete(foundPair);
	}

	/**
	 * Holder of the futures for an async locate task as well as providing some helper functions.
	 * The completableFuture will be completed once the call to
	 * {@link ServerLevel#findNearestMapFeature(TagKey, BlockPos, int, boolean)} has completed, and will hold the
	 * result of it.
	 * The taskFuture is the future for the {@link Runnable} itself in the executor service.
	 */
	public record LocateTask<T>(MinecraftServer server, CompletableFuture<T> completableFuture, Future<?> taskFuture) {
		/**
		 * Helper function that calls {@link CompletableFuture#thenAccept(Consumer)} with the given action.
		 * Bear in mind that the action will be executed from the task's thread. If you intend to change any game data,
		 * it's strongly advised you use {@link #thenOnServerThread(Consumer)} instead so that it's queued and executed
		 * on the main server thread instead.
		 */
		public LocateTask<T> then(Consumer<T> action) {
			completableFuture.thenAccept(action);
			return this;
		}

		/**
		 * Helper function that calls {@link CompletableFuture#thenAccept(Consumer)} with the given action on the server
		 * thread.
		 */
		public LocateTask<T> thenOnServerThread(Consumer<T> action) {
			completableFuture.thenAccept(pos -> server.submit(() -> action.accept(pos)));
			return this;
		}

		/**
		 * Helper function that cancels both completableFuture and taskFuture.
		 */
		public void cancel() {
			taskFuture.cancel(true);
			completableFuture.cancel(false);
		}
	}
}

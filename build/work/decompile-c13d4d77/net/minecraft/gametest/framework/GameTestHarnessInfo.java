package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.phys.AxisAlignedBB;

public class GameTestHarnessInfo {

    private final GameTestHarnessTestFunction testFunction;
    @Nullable
    private BlockPosition structureBlockPos;
    private final WorldServer level;
    private final Collection<GameTestHarnessListener> listeners = Lists.newArrayList();
    private final int timeoutTicks;
    private final Collection<GameTestHarnessSequence> sequences = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap();
    private long startTick;
    private int ticksToWaitForChunkLoading = 20;
    private boolean placedStructure;
    private boolean chunksLoaded;
    private long tickCount;
    private boolean started;
    private boolean rerunUntilFailed;
    private final Stopwatch timer = Stopwatch.createUnstarted();
    private boolean done;
    private final EnumBlockRotation rotation;
    @Nullable
    private Throwable error;
    @Nullable
    private TileEntityStructure structureBlockEntity;

    public GameTestHarnessInfo(GameTestHarnessTestFunction gametestharnesstestfunction, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        this.testFunction = gametestharnesstestfunction;
        this.level = worldserver;
        this.timeoutTicks = gametestharnesstestfunction.getMaxTicks();
        this.rotation = gametestharnesstestfunction.getRotation().getRotated(enumblockrotation);
    }

    void setStructureBlockPos(BlockPosition blockposition) {
        this.structureBlockPos = blockposition;
    }

    void startExecution() {
        this.startTick = this.level.getGameTime() + this.testFunction.getSetupTicks();
        this.timer.start();
    }

    public void tick() {
        if (!this.isDone()) {
            if (this.structureBlockEntity == null) {
                this.fail(new IllegalStateException("Running test without structure block entity"));
            }

            if (this.chunksLoaded || GameTestHarnessStructures.getStructureBoundingBox(this.structureBlockEntity).intersectingChunks().allMatch((chunkcoordintpair) -> {
                return this.level.isPositionEntityTicking(chunkcoordintpair.getWorldPosition());
            })) {
                this.chunksLoaded = true;
                if (this.ticksToWaitForChunkLoading > 0) {
                    --this.ticksToWaitForChunkLoading;
                } else {
                    if (!this.placedStructure) {
                        this.placedStructure = true;
                        this.structureBlockEntity.placeStructure(this.level);
                        StructureBoundingBox structureboundingbox = GameTestHarnessStructures.getStructureBoundingBox(this.structureBlockEntity);

                        this.level.getBlockTicks().clearArea(structureboundingbox);
                        this.level.clearBlockEvents(structureboundingbox);
                        this.startExecution();
                    }

                    this.tickInternal();
                    if (this.isDone()) {
                        if (this.error != null) {
                            this.listeners.forEach((gametestharnesslistener) -> {
                                gametestharnesslistener.testFailed(this);
                            });
                        } else {
                            this.listeners.forEach((gametestharnesslistener) -> {
                                gametestharnesslistener.testPassed(this);
                            });
                        }
                    }

                }
            }
        }
    }

    private void tickInternal() {
        this.tickCount = this.level.getGameTime() - this.startTick;
        if (this.tickCount >= 0L) {
            if (this.tickCount == 0L) {
                this.startTest();
            }

            ObjectIterator objectiterator = this.runAtTickTimeMap.object2LongEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<Runnable> entry = (Entry) objectiterator.next();

                if (entry.getLongValue() <= this.tickCount) {
                    try {
                        ((Runnable) entry.getKey()).run();
                    } catch (Exception exception) {
                        this.fail(exception);
                    }

                    objectiterator.remove();
                }
            }

            if (this.tickCount > (long) this.timeoutTicks) {
                if (this.sequences.isEmpty()) {
                    this.fail(new GameTestHarnessTimeout("Didn't succeed or fail within " + this.testFunction.getMaxTicks() + " ticks"));
                } else {
                    this.sequences.forEach((gametestharnesssequence) -> {
                        gametestharnesssequence.tickAndFailIfNotComplete(this.tickCount);
                    });
                    if (this.error == null) {
                        this.fail(new GameTestHarnessTimeout("No sequences finished"));
                    }
                }
            } else {
                this.sequences.forEach((gametestharnesssequence) -> {
                    gametestharnesssequence.tickAndContinue(this.tickCount);
                });
            }

        }
    }

    private void startTest() {
        if (this.started) {
            throw new IllegalStateException("Test already started");
        } else {
            this.started = true;

            try {
                this.testFunction.run(new GameTestHarnessHelper(this));
            } catch (Exception exception) {
                this.fail(exception);
            }

        }
    }

    public void setRunAtTickTime(long i, Runnable runnable) {
        this.runAtTickTimeMap.put(runnable, i);
    }

    public String getTestName() {
        return this.testFunction.getTestName();
    }

    public BlockPosition getStructureBlockPos() {
        return this.structureBlockPos;
    }

    @Nullable
    public BlockPosition getStructureOrigin() {
        TileEntityStructure tileentitystructure = this.getStructureBlockEntity();

        return tileentitystructure == null ? null : GameTestHarnessStructures.getStructureOrigin(tileentitystructure);
    }

    @Nullable
    public BaseBlockPosition getStructureSize() {
        TileEntityStructure tileentitystructure = this.getStructureBlockEntity();

        return tileentitystructure == null ? null : tileentitystructure.getStructureSize();
    }

    @Nullable
    public AxisAlignedBB getStructureBounds() {
        TileEntityStructure tileentitystructure = this.getStructureBlockEntity();

        return tileentitystructure == null ? null : GameTestHarnessStructures.getStructureBounds(tileentitystructure);
    }

    @Nullable
    private TileEntityStructure getStructureBlockEntity() {
        return (TileEntityStructure) this.level.getBlockEntity(this.structureBlockPos);
    }

    public WorldServer getLevel() {
        return this.level;
    }

    public boolean hasSucceeded() {
        return this.done && this.error == null;
    }

    public boolean hasFailed() {
        return this.error != null;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public boolean isDone() {
        return this.done;
    }

    public long getRunTime() {
        return this.timer.elapsed(TimeUnit.MILLISECONDS);
    }

    private void finish() {
        if (!this.done) {
            this.done = true;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }

    }

    public void succeed() {
        if (this.error == null) {
            this.finish();
            AxisAlignedBB axisalignedbb = this.getStructureBounds();
            List<Entity> list = this.getLevel().getEntitiesOfClass(Entity.class, axisalignedbb.inflate(1.0D), (entity) -> {
                return !(entity instanceof EntityHuman);
            });

            list.forEach((entity) -> {
                entity.remove(Entity.RemovalReason.DISCARDED);
            });
        }

    }

    public void fail(Throwable throwable) {
        this.error = throwable;
        this.finish();
    }

    @Nullable
    public Throwable getError() {
        return this.error;
    }

    public String toString() {
        return this.getTestName();
    }

    public void addListener(GameTestHarnessListener gametestharnesslistener) {
        this.listeners.add(gametestharnesslistener);
    }

    public void prepareTestStructure(BlockPosition blockposition) {
        this.structureBlockEntity = GameTestHarnessStructures.prepareTestStructure(this, blockposition, this.getRotation(), this.level);
        this.structureBlockPos = this.structureBlockEntity.getBlockPos();
        GameTestHarnessStructures.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPosition(1, 0, -1), this.getRotation(), this.level);
        this.listeners.forEach((gametestharnesslistener) -> {
            gametestharnesslistener.testStructureLoaded(this);
        });
    }

    public void clearStructure() {
        if (this.structureBlockEntity == null) {
            throw new IllegalStateException("Expected structure to be initialized, but it was null");
        } else {
            StructureBoundingBox structureboundingbox = GameTestHarnessStructures.getStructureBoundingBox(this.structureBlockEntity);

            GameTestHarnessStructures.clearSpaceForStructure(structureboundingbox, this.level);
        }
    }

    long getTick() {
        return this.tickCount;
    }

    GameTestHarnessSequence createSequence() {
        GameTestHarnessSequence gametestharnesssequence = new GameTestHarnessSequence(this);

        this.sequences.add(gametestharnesssequence);
        return gametestharnesssequence;
    }

    public boolean isRequired() {
        return this.testFunction.isRequired();
    }

    public boolean isOptional() {
        return !this.testFunction.isRequired();
    }

    public String getStructureName() {
        return this.testFunction.getStructureName();
    }

    public EnumBlockRotation getRotation() {
        return this.rotation;
    }

    public GameTestHarnessTestFunction getTestFunction() {
        return this.testFunction;
    }

    public int getTimeoutTicks() {
        return this.timeoutTicks;
    }

    public boolean isFlaky() {
        return this.testFunction.isFlaky();
    }

    public int maxAttempts() {
        return this.testFunction.getMaxAttempts();
    }

    public int requiredSuccesses() {
        return this.testFunction.getRequiredSuccesses();
    }

    public void setRerunUntilFailed(boolean flag) {
        this.rerunUntilFailed = flag;
    }

    public boolean rerunUntilFailed() {
        return this.rerunUntilFailed;
    }
}

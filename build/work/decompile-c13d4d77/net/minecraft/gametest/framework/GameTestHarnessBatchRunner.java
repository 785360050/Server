package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.phys.AxisAlignedBB;
import org.slf4j.Logger;

public class GameTestHarnessBatchRunner {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockPosition firstTestNorthWestCorner;
    final WorldServer level;
    private final GameTestHarnessTicker testTicker;
    private final int testsPerRow;
    private final List<GameTestHarnessInfo> allTestInfos;
    private final List<Pair<GameTestHarnessBatch, Collection<GameTestHarnessInfo>>> batches;
    private int count;
    private AxisAlignedBB rowBounds;
    private final BlockPosition.MutableBlockPosition nextTestNorthWestCorner;

    public GameTestHarnessBatchRunner(Collection<GameTestHarnessBatch> collection, BlockPosition blockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver, GameTestHarnessTicker gametestharnessticker, int i) {
        this.nextTestNorthWestCorner = blockposition.mutable();
        this.rowBounds = new AxisAlignedBB(this.nextTestNorthWestCorner);
        this.firstTestNorthWestCorner = blockposition;
        this.level = worldserver;
        this.testTicker = gametestharnessticker;
        this.testsPerRow = i;
        this.batches = (List) collection.stream().map((gametestharnessbatch) -> {
            Collection<GameTestHarnessInfo> collection1 = (Collection) gametestharnessbatch.getTestFunctions().stream().map((gametestharnesstestfunction) -> {
                return new GameTestHarnessInfo(gametestharnesstestfunction, enumblockrotation, worldserver);
            }).collect(ImmutableList.toImmutableList());

            return Pair.of(gametestharnessbatch, collection1);
        }).collect(ImmutableList.toImmutableList());
        this.allTestInfos = (List) this.batches.stream().flatMap((pair) -> {
            return ((Collection) pair.getSecond()).stream();
        }).collect(ImmutableList.toImmutableList());
    }

    public List<GameTestHarnessInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.runBatch(0);
    }

    void runBatch(final int i) {
        if (i < this.batches.size()) {
            Pair<GameTestHarnessBatch, Collection<GameTestHarnessInfo>> pair = (Pair) this.batches.get(i);
            final GameTestHarnessBatch gametestharnessbatch = (GameTestHarnessBatch) pair.getFirst();
            Collection<GameTestHarnessInfo> collection = (Collection) pair.getSecond();
            Map<GameTestHarnessInfo, BlockPosition> map = this.createStructuresForBatch(collection);
            String s = gametestharnessbatch.getName();

            GameTestHarnessBatchRunner.LOGGER.info("Running test batch '{}' ({} tests)...", s, collection.size());
            gametestharnessbatch.runBeforeBatchFunction(this.level);
            final GameTestHarnessCollector gametestharnesscollector = new GameTestHarnessCollector();

            Objects.requireNonNull(gametestharnesscollector);
            collection.forEach(gametestharnesscollector::addTestToTrack);
            gametestharnesscollector.addListener(new GameTestHarnessListener() {
                private void testCompleted() {
                    if (gametestharnesscollector.isDone()) {
                        gametestharnessbatch.runAfterBatchFunction(GameTestHarnessBatchRunner.this.level);
                        LongArraySet longarrayset = new LongArraySet(GameTestHarnessBatchRunner.this.level.getForcedChunks());

                        longarrayset.forEach((j) -> {
                            GameTestHarnessBatchRunner.this.level.setChunkForced(ChunkCoordIntPair.getX(j), ChunkCoordIntPair.getZ(j), false);
                        });
                        GameTestHarnessBatchRunner.this.runBatch(i + 1);
                    }

                }

                @Override
                public void testStructureLoaded(GameTestHarnessInfo gametestharnessinfo) {}

                @Override
                public void testPassed(GameTestHarnessInfo gametestharnessinfo) {
                    this.testCompleted();
                }

                @Override
                public void testFailed(GameTestHarnessInfo gametestharnessinfo) {
                    this.testCompleted();
                }
            });
            collection.forEach((gametestharnessinfo) -> {
                BlockPosition blockposition = (BlockPosition) map.get(gametestharnessinfo);

                GameTestHarnessRunner.runTest(gametestharnessinfo, blockposition, this.testTicker);
            });
        }
    }

    private Map<GameTestHarnessInfo, BlockPosition> createStructuresForBatch(Collection<GameTestHarnessInfo> collection) {
        Map<GameTestHarnessInfo, BlockPosition> map = Maps.newHashMap();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            GameTestHarnessInfo gametestharnessinfo = (GameTestHarnessInfo) iterator.next();
            BlockPosition blockposition = new BlockPosition(this.nextTestNorthWestCorner);
            TileEntityStructure tileentitystructure = GameTestHarnessStructures.prepareTestStructure(gametestharnessinfo, blockposition, gametestharnessinfo.getRotation(), this.level);
            AxisAlignedBB axisalignedbb = GameTestHarnessStructures.getStructureBounds(tileentitystructure);

            gametestharnessinfo.setStructureBlockPos(tileentitystructure.getBlockPos());
            map.put(gametestharnessinfo, new BlockPosition(this.nextTestNorthWestCorner));
            this.rowBounds = this.rowBounds.minmax(axisalignedbb);
            this.nextTestNorthWestCorner.move((int) axisalignedbb.getXsize() + 5, 0, 0);
            if (this.count++ % this.testsPerRow == this.testsPerRow - 1) {
                this.nextTestNorthWestCorner.move(0, 0, (int) this.rowBounds.getZsize() + 6);
                this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
                this.rowBounds = new AxisAlignedBB(this.nextTestNorthWestCorner);
            }
        }

        return map;
    }
}

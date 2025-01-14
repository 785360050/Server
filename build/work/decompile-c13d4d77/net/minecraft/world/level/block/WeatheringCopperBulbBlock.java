package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class WeatheringCopperBulbBlock extends CopperBulbBlock implements WeatheringCopper {

    public static final MapCodec<WeatheringCopperBulbBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperBulbBlock::getAge), propertiesCodec()).apply(instance, WeatheringCopperBulbBlock::new);
    });
    private final WeatheringCopper.a weatherState;

    @Override
    protected MapCodec<WeatheringCopperBulbBlock> codec() {
        return WeatheringCopperBulbBlock.CODEC;
    }

    public WeatheringCopperBulbBlock(WeatheringCopper.a weatheringcopper_a, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.weatherState = weatheringcopper_a;
    }

    @Override
    public void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        this.changeOverTime(iblockdata, worldserver, blockposition, randomsource);
    }

    @Override
    public boolean isRandomlyTicking(IBlockData iblockdata) {
        return WeatheringCopper.getNext(iblockdata.getBlock()).isPresent();
    }

    @Override
    public WeatheringCopper.a getAge() {
        return this.weatherState;
    }
}

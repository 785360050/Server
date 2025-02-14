package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class DecoratedPotBlock extends BlockTileEntity implements IBlockWaterlogged {

    public static final MapCodec<DecoratedPotBlock> CODEC = simpleCodec(DecoratedPotBlock::new);
    public static final MinecraftKey SHERDS_DYNAMIC_DROP_ID = new MinecraftKey("sherds");
    private static final VoxelShape BOUNDING_BOX = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final BlockStateDirection HORIZONTAL_FACING = BlockProperties.HORIZONTAL_FACING;
    public static final BlockStateBoolean CRACKED = BlockProperties.CRACKED;
    private static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;

    @Override
    public MapCodec<DecoratedPotBlock> codec() {
        return DecoratedPotBlock.CODEC;
    }

    protected DecoratedPotBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(DecoratedPotBlock.HORIZONTAL_FACING, EnumDirection.NORTH)).setValue(DecoratedPotBlock.WATERLOGGED, false)).setValue(DecoratedPotBlock.CRACKED, false));
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(DecoratedPotBlock.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        return (IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(DecoratedPotBlock.HORIZONTAL_FACING, blockactioncontext.getHorizontalDirection())).setValue(DecoratedPotBlock.WATERLOGGED, fluid.getType() == FluidTypes.WATER)).setValue(DecoratedPotBlock.CRACKED, false);
    }

    @Override
    public EnumInteractionResult use(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity decoratedpotblockentity = (DecoratedPotBlockEntity) tileentity;

            if (world.isClientSide) {
                return EnumInteractionResult.CONSUME;
            } else {
                ItemStack itemstack = entityhuman.getItemInHand(enumhand);
                ItemStack itemstack1 = decoratedpotblockentity.getTheItem();

                if (!itemstack.isEmpty() && (itemstack1.isEmpty() || ItemStack.isSameItemSameTags(itemstack1, itemstack) && itemstack1.getCount() < itemstack1.getMaxStackSize())) {
                    decoratedpotblockentity.wobble(DecoratedPotBlockEntity.b.POSITIVE);
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
                    ItemStack itemstack2 = entityhuman.isCreative() ? itemstack.copyWithCount(1) : itemstack.split(1);
                    float f;

                    if (decoratedpotblockentity.isEmpty()) {
                        decoratedpotblockentity.setTheItem(itemstack2);
                        f = (float) itemstack2.getCount() / (float) itemstack2.getMaxStackSize();
                    } else {
                        itemstack1.grow(1);
                        f = (float) itemstack1.getCount() / (float) itemstack1.getMaxStackSize();
                    }

                    world.playSound((EntityHuman) null, blockposition, SoundEffects.DECORATED_POT_INSERT, SoundCategory.BLOCKS, 1.0F, 0.7F + 0.5F * f);
                    if (world instanceof WorldServer) {
                        WorldServer worldserver = (WorldServer) world;

                        worldserver.sendParticles(Particles.DUST_PLUME, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 1.2D, (double) blockposition.getZ() + 0.5D, 7, 0.0D, 0.0D, 0.0D, 0.0D);
                    }

                    decoratedpotblockentity.setChanged();
                } else {
                    world.playSound((EntityHuman) null, blockposition, SoundEffects.DECORATED_POT_INSERT_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    decoratedpotblockentity.wobble(DecoratedPotBlockEntity.b.NEGATIVE);
                }

                world.gameEvent((Entity) entityhuman, GameEvent.BLOCK_CHANGE, blockposition);
                return EnumInteractionResult.SUCCESS;
            }
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        if (world.isClientSide) {
            world.getBlockEntity(blockposition, TileEntityTypes.DECORATED_POT).ifPresent((decoratedpotblockentity) -> {
                decoratedpotblockentity.setFromItem(itemstack);
            });
        }

    }

    @Override
    public boolean isPathfindable(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, PathMode pathmode) {
        return false;
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return DecoratedPotBlock.BOUNDING_BOX;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(DecoratedPotBlock.HORIZONTAL_FACING, DecoratedPotBlock.WATERLOGGED, DecoratedPotBlock.CRACKED);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new DecoratedPotBlockEntity(blockposition, iblockdata);
    }

    @Override
    public void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        InventoryUtils.dropContentsOnDestroy(iblockdata, iblockdata1, world, blockposition);
        super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
    }

    @Override
    public List<ItemStack> getDrops(IBlockData iblockdata, LootParams.a lootparams_a) {
        TileEntity tileentity = (TileEntity) lootparams_a.getOptionalParameter(LootContextParameters.BLOCK_ENTITY);

        if (tileentity instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity decoratedpotblockentity = (DecoratedPotBlockEntity) tileentity;

            lootparams_a.withDynamicDrop(DecoratedPotBlock.SHERDS_DYNAMIC_DROP_ID, (consumer) -> {
                decoratedpotblockentity.getDecorations().sorted().map(Item::getDefaultInstance).forEach(consumer);
            });
        }

        return super.getDrops(iblockdata, lootparams_a);
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        ItemStack itemstack = entityhuman.getMainHandItem();
        IBlockData iblockdata1 = iblockdata;

        if (itemstack.is(TagsItem.BREAKS_DECORATED_POTS) && !EnchantmentManager.hasSilkTouch(itemstack)) {
            iblockdata1 = (IBlockData) iblockdata.setValue(DecoratedPotBlock.CRACKED, true);
            world.setBlock(blockposition, iblockdata1, 4);
        }

        return super.playerWillDestroy(world, blockposition, iblockdata1, entityhuman);
    }

    @Override
    public Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(DecoratedPotBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    public SoundEffectType getSoundType(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(DecoratedPotBlock.CRACKED) ? SoundEffectType.DECORATED_POT_CRACKED : SoundEffectType.DECORATED_POT;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable IBlockAccess iblockaccess, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, iblockaccess, list, tooltipflag);
        DecoratedPotBlockEntity.Decoration decoratedpotblockentity_decoration = DecoratedPotBlockEntity.Decoration.load(ItemBlock.getBlockEntityData(itemstack));

        if (!decoratedpotblockentity_decoration.equals(DecoratedPotBlockEntity.Decoration.EMPTY)) {
            list.add(CommonComponents.EMPTY);
            Stream.of(decoratedpotblockentity_decoration.front(), decoratedpotblockentity_decoration.left(), decoratedpotblockentity_decoration.right(), decoratedpotblockentity_decoration.back()).forEach((item) -> {
                list.add((new ItemStack(item, 1)).getHoverName().plainCopy().withStyle(EnumChatFormat.GRAY));
            });
        }
    }

    @Override
    public void onProjectileHit(World world, IBlockData iblockdata, MovingObjectPositionBlock movingobjectpositionblock, IProjectile iprojectile) {
        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

        if (!world.isClientSide && iprojectile.mayInteract(world, blockposition) && iprojectile.mayBreak(world)) {
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(DecoratedPotBlock.CRACKED, true), 4);
            world.destroyBlock(blockposition, true, iprojectile);
        }

    }

    @Override
    public ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        TileEntity tileentity = iworldreader.getBlockEntity(blockposition);

        if (tileentity instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity decoratedpotblockentity = (DecoratedPotBlockEntity) tileentity;

            return decoratedpotblockentity.getPotAsItem();
        } else {
            return super.getCloneItemStack(iworldreader, blockposition, iblockdata);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(blockposition));
    }
}

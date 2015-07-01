package gigaherz.elementsofpower.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemSnow;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidFinite;

import java.util.List;

public class EntityAirball extends EntityBallBase {

    public EntityAirball(World worldIn)
    {
        super(worldIn);
    }
    public EntityAirball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
    }
    public EntityAirball(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }
    public EntityAirball(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, force, p_i1774_2_);
    }

    @Override
    protected void processEntitiesAround(Vec3 hitVec) {

        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord-damageForce,
                hitVec.yCoord-damageForce,
                hitVec.zCoord-damageForce,
                hitVec.xCoord+damageForce,
                hitVec.yCoord+damageForce,
                hitVec.zCoord+damageForce);

        List<EntityLivingBase> living = (List<EntityLivingBase>)worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        pushEntities(hitVec, living);

        List<EntityItem> items = (List<EntityItem>)worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
        pushEntities(hitVec, items);
    }

    private void pushEntities(Vec3 hitVec, List<? extends Entity> living) {
        for(Entity e : living)
        {
            if(!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx*dx+dy*dy+dz*dz);

            if(ll < 0.0001f)
                continue;

            double lv = Math.max(0, damageForce-ll);

            double vx = dx * lv / ll;
            double vy = dy * lv / ll;
            double vz = dz * lv / ll;
            e.addVelocity(vx, vy, vz);
        }
    }

    @Override
    protected void spawnBallParticles()
    {
        if(damageForce >= 5)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
        else if(damageForce >= 2)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
        else
        {
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX, this.posY, this.posZ,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }

    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.fire) {
            worldObj.setBlockToAir(blockPos);
        }
        else if (block == Blocks.flowing_water || block == Blocks.water) {
            if((Integer)currentState.getValue(BlockDynamicLiquid.LEVEL) > 0) {
                worldObj.setBlockToAir(blockPos);
            }
        }
    }
}

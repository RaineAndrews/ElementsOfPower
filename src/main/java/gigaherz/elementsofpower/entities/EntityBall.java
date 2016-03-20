package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityBall extends EntityThrowable
{
    Spellcast spellcast;

    private static final DataParameter<String> SEQ = EntityDataManager.createKey(EntityEssence.class, DataSerializers.STRING);

    public EntityBall(World worldIn)
    {
        super(worldIn);
    }

    public EntityBall(World worldIn, Spellcast spellcast, EntityLivingBase thrower)
    {
        super(worldIn, thrower);
        this.spellcast = spellcast;
        spellcast.setProjectile(this);
        
        getDataManager().register(SEQ, "");
        getDataManager().set(SEQ, spellcast.getSequence());
        this.getDataManager().setDirty(SEQ);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();

        getDataManager().register(SEQ, "");
    }

    // FIXME
    //@Override
    protected float getVelocity()
    {
        return 2.0F;
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.001F;
    }

    @Override
    protected void onImpact(RayTraceResult pos)
    {
        if (!this.worldObj.isRemote)
        {
            if (getSpellcast() != null)
                spellcast.onImpact(pos, rand);

            this.setDead();
        }
    }

    public float getScale()
    {
        return 0.6f * (1 + getSpellcast().getDamageForce());
    }

    public Spellcast getSpellcast()
    {
        if (spellcast == null)
        {
            String sequence = getDataManager().get(SEQ);
            if (sequence != null)
            {
                spellcast = SpellManager.makeSpell(sequence);
                spellcast.init(worldObj, (EntityPlayer) getThrower());
            }
        }
        return spellcast;
    }
}

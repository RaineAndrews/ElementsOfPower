package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class BeamShape extends SpellShape
{
    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
    {
        return cast;
    }

    @Override
    public void spellTick(Spellcast cast)
    {
        MovingObjectPosition mop = cast.getHitPosition();

        if (mop != null)
        {
            if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            {
                cast.effect.processDirectHit(cast, mop.entityHit);
            }
            else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos pos = mop.getBlockPos();
                IBlockState state = cast.world.getBlockState(pos);
                cast.effect.processBlockWithinRadius(cast, pos, state, 0, mop);
            }
        }
    }
}
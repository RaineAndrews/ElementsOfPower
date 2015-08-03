package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.Utils;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraft.util.StatCollector;

import java.util.*;

public class MagicDatabase
{
    static final List<ItemEssenceConversion> stockEntries = new ArrayList<>();

    public static Map<ItemStack, ItemStack> containerConversion = new HashMap<>();
    public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<>();
    public static Map<ItemStack, MagicAmounts> itemEssences = new HashMap<>();

    public final static String[] magicNames = {
            "element.fire",
            "element.water",
            "element.air",
            "element.earth",
            "element.light",
            "element.darkness",
            "element.life",
            "element.death",
    };

    public static String getMagicName(int i)
    {
        return StatCollector.translateToLocal(magicNames[i]);
    }

    public static void initialize()
    {
        registerContainerConversions();
        registerContainerCapacity();
        registerEssenceSources();
    }

    public static void postInitialize()
    {
        RecipeTools.gatherRecipes();
        registerEssencesForRecipes();
    }

    private static void registerEssencesForRecipes()
    {
        for (Map.Entry<ItemStack, List<ItemStack>> it : RecipeTools.itemSources.entrySet())
        {
            MagicAmounts ma = getEssences(it.getKey());
            if (ma != null && !ma.isEmpty())
                continue;

            boolean allFound = true;
            MagicAmounts am = new MagicAmounts();
            for (ItemStack b : it.getValue())
            {
                MagicAmounts m = getEssences(b);
                if (m == null || m.isEmpty())
                {
                    allFound = false;
                    break;
                }

                am.add(m);
            }

            if (!allFound)
                continue;

            ItemStack key = it.getKey().copy();

            for(int i=0;i<am.amounts.length;i++)
            {
                am.amounts[i] /= key.stackSize;
            }

            key.stackSize = 1;

            itemEssences.put(it.getKey(), am);
        }
    }

    static void registerEssenceSources()
    {
        //essences(Blocks.coal_ore).fire(8).earth(2);
        //essences(Blocks.quartz_ore,0);

        essences(Blocks.cactus).life(3);
        essences(Blocks.chest).earth(2).light(1);

        //essences(Items.dye);

        essences(Items.clay_ball).earth(1).water(1);
        essences(Blocks.clay).earth(4).water(4);
        essences(Items.brick).earth(1).fire(1);
        essences(Blocks.brick_block).earth(4).fire(4);

        essences(Blocks.dirt).earth(3).life(1);
        essences(Blocks.gravel).earth(3).air(1);
        essences(Blocks.sand).earth(2).air(2);
        essences(Blocks.obsidian).earth(10).darkness(10);
        essences(Blocks.netherrack).earth(1).fire(1);

        essences(Blocks.cobblestone).earth(5);
        essences(Blocks.stone).earth(10);
        essences(Blocks.hardened_clay).earth(5).fire(1);
        essences(Blocks.stained_hardened_clay).earth(5).fire(1);

        essences(Blocks.grass).earth(2).life(2);

        collection(
                essences(Blocks.yellow_flower),
                essences(Blocks.red_flower, 0),
                essences(Blocks.red_flower, 1),
                essences(Blocks.red_flower, 2),
                essences(Blocks.red_flower, 3),
                essences(Blocks.red_flower, 5),
                essences(Blocks.red_flower, 7)).life(1);

        essences(Blocks.log).life(16);
        essences(Blocks.log2).life(16);
        essences(Blocks.planks).life(4);
        essences(Items.stick).life(1);

        essences(Items.coal).fire(8);
        essences(Blocks.coal_block).fire(72).earth(8);

        //essences(Blocks.iron_block);
        //essences(Blocks.lapis_block);
        //essences(Blocks.nether_brick);
        //essences(Blocks.quartz_block);

        //essences(Blocks.stone_slab, 0);
        //essences(Blocks.stone_slab, 1);
        //essences(Blocks.stone_slab, 5);
        //essences(Blocks.stone_slab, 7);
        //essences(Blocks.stone_slab2, 0);
        //essences(Blocks.wooden_slab,0);

        essences(Items.wheat).life(1);
        essences(Blocks.hay_block).earth(1).life(9).air(1);

        essences(Blocks.red_mushroom, 0).earth(2).life(2);
        essences(Blocks.brown_mushroom, 0).earth(2).life(2);
        essences(Blocks.pumpkin, 0).earth(1).life(3);
        essences(Blocks.sponge, 1).water(4).life(2);
        essences(Blocks.vine, 0).life(2);

        //essences(Blocks.piston,0);
        //essences(Blocks.snow,0);
        //essences(Blocks.stonebrick,0);
        //essences(Blocks.tnt,0);

        //essences(Items.blaze_powder,0);
        //essences(Items.blaze_rod,0);

        //essences(Items.apple,0);
        //essences(Items.beef,0);
        //essences(Items.carrot,0);
        //essences(Items.chicken,0);
        //essences(Items.egg,0);

        //essences(Items.fish,0);
        //essences(Items.fish,1);

        //essences(Items.bone,0);
        //essences(Items.bowl,0);
        //essences(Items.clay_ball,0);
        //essences(Items.diamond,0);
        //essences(Items.emerald,0);
        //essences(Items.ender_pearl,0);
        //essences(Items.feather,0);
        //essences(Items.flint,0);
        //essences(Items.gold_nugget,0);
        //essences(Items.iron_ingot,0);
        //essences(Items.map,0);
        //essences(Items.melon,0);
        //essences(Items.milk_bucket,0);
        //essences(Items.mutton,0);
        //essences(Items.nether_star,0);
        //essences(Items.porkchop,0);
        //essences(Items.potato,0);
        //essences(Items.prismarine_crystals,0);
        //essences(Items.prismarine_shard,0);
        //essences(Items.rabbit_hide,0);
        //essences(Items.rabbit,0);
        //essences(Items.redstone,0);
        //essences(Items.reeds,0);
        //essences(Items.slime_ball,0);
        //essences(Items.snowball,0);
        //essences(Items.spider_eye,0);
        //essences(Items.string,0);
        //essences(Items.gunpowder,0);
        //essences(Items.glowstone_dust,0);

        for (ItemEssenceConversion source : stockEntries)
            source.putAll(itemEssences);
    }

    private static ItemEssenceCollection collection(ItemEssenceConversion... entries)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        Collections.addAll(collection, entries);

        return collection;
    }

    private static ItemEssenceCollection essences(Item item)
    {
        List<ItemStack> subItems = new ArrayList<>();

        item.getSubItems(item, CreativeTabs.tabAllSearch, subItems);

        ItemEssenceCollection collection = new ItemEssenceCollection();
        for (ItemStack is : subItems)
        {
            ItemEssenceEntry ee = new ItemEssenceEntry(is, new MagicAmounts());
            collection.add(ee);
            stockEntries.add(ee);
        }

        return collection;
    }

    private static ItemEssenceCollection essences(Block block)
    {
        return essences(Item.getItemFromBlock(block));
    }

    private static ItemEssenceEntry essences(Item item, int meta)
    {
        ItemEssenceEntry ee = new ItemEssenceEntry(new ItemStack(item, 1, meta), new MagicAmounts());
        stockEntries.add(ee);
        return ee;
    }

    private static ItemEssenceEntry essences(Block block, int meta)
    {
        ItemEssenceEntry ee = new ItemEssenceEntry(new ItemStack(block, 1, meta), new MagicAmounts());
        stockEntries.add(ee);
        return ee;
    }

    private static void registerContainerCapacity()
    {
        containerCapacity.put(new ItemStack(Items.dye, 1, 4), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(Items.emerald, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(Items.diamond, 1), new MagicAmounts().all(100));

        containerCapacity.put(new ItemStack(ElementsOfPower.magicContainer, 1, 0), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(ElementsOfPower.magicContainer, 1, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(ElementsOfPower.magicContainer, 1, 2), new MagicAmounts().all(100));

        containerCapacity.put(ElementsOfPower.wandLapis, new MagicAmounts().all(10));
        containerCapacity.put(ElementsOfPower.wandEmerald, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.wandDiamond, new MagicAmounts().all(100));
        containerCapacity.put(ElementsOfPower.staffLapis, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.staffEmerald, new MagicAmounts().all(250));
        containerCapacity.put(ElementsOfPower.staffDiamond, new MagicAmounts().all(500));
    }

    private static void registerContainerConversions()
    {
        containerConversion.put(new ItemStack(Items.dye, 1, 4), new ItemStack(ElementsOfPower.magicContainer, 1, 0));
        containerConversion.put(new ItemStack(Items.emerald, 1), new ItemStack(ElementsOfPower.magicContainer, 1, 1));
        containerConversion.put(new ItemStack(Items.diamond, 1), new ItemStack(ElementsOfPower.magicContainer, 1, 2));
    }

    public static boolean itemContainsMagic(ItemStack stack)
    {
        MagicAmounts amounts = getContainedMagic(stack);

        return amounts != null && !amounts.isEmpty();
    }

    public static boolean canItemContainMagic(ItemStack stack)
    {
        return stack.stackSize <= 1 && Utils.stackIsInMap(containerCapacity, stack);
    }

    public static MagicAmounts getMagicLimits(ItemStack stack)
    {
        if (stack.stackSize > 1)
        {
            return null;
        }

        return Utils.getFromMap(containerCapacity, stack);
    }

    public static boolean itemHasEssence(ItemStack stack)
    {
        if(stack.stackSize > 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
        }
        return Utils.stackIsInMap(itemEssences, stack);
    }

    public static MagicAmounts getEssences(ItemStack stack)
    {
        return Utils.getFromMap(itemEssences, stack);
    }

    public static MagicAmounts getContainedMagic(ItemStack output)
    {
        if (output == null)
        {
            return null;
        }

        if (output.stackSize != 1)
        {
            return null;
        }

        if (output.getItem() instanceof ItemWand)
        {
            if (ItemWand.isCreative(output))
                return new MagicAmounts().all(999);
        }

        if(!(output.getItem() instanceof ItemMagicContainer))
            return null;

        NBTTagCompound nbt = output.getTagCompound();

        if (nbt == null)
        {
            return null;
        }

        MagicAmounts amounts = new MagicAmounts();
        int max = 0;

        for (int i = 0; i < 8; i++)
        {
            try
            {
                int amount = nbt.getInteger("" + i);

                if (amount > max)
                {
                    max = amount;
                }

                amounts.amounts[i] = amount;
            }
            catch (NumberFormatException ex)
            {
                throw new ReportedException(new CrashReport("Exception while parsing NBT magic infromation", ex));
            }
        }

        if (max > 0)
        {
            return amounts;
        }

        return null;
    }

    public static ItemStack setContainedMagic(ItemStack output, MagicAmounts amounts)
    {
        if (output == null)
        {
            return null;
        }

        if (output.stackSize != 1)
        {
            return null;
        }

        Item item = output.getItem();
        if (item instanceof ItemWand)
        {
            if (ItemWand.isCreative(output))
                return output;
        }

        if (amounts != null)
        {
            if (amounts.isEmpty())
            {
                amounts = null;
            }
        }

        if (amounts != null)
        {
            NBTTagCompound nbt = output.getTagCompound();

            if (nbt == null)
            {
                if (Utils.stackIsInMap(containerConversion, output))
                {
                    output = Utils.getFromMap(containerConversion, output).copy();
                }

                nbt = new NBTTagCompound();
                output.setTagCompound(nbt);
            }

            for (int i = 0; i < 8; i++)
            {
                nbt.setInteger("" + i, amounts.amounts[i]);
            }

            return output;
        }
        else
        {
            output.setTagCompound(null);
            ItemStack is = Utils.findKeyForValue(containerConversion, output);

            if (is != null)
            {
                output = is.copy();
            }

            return output;
        }
    }

    public interface ItemEssenceConversion
    {
        ItemEssenceConversion all(int amount);
        ItemEssenceConversion fire(int amount);
        ItemEssenceConversion water(int amount);
        ItemEssenceConversion air(int amount);
        ItemEssenceConversion earth(int amount);
        ItemEssenceConversion light(int amount);
        ItemEssenceConversion darkness(int amount);
        ItemEssenceConversion life(int amount);
        ItemEssenceConversion death(int amount);

        void putAll(Map<ItemStack, MagicAmounts> essences);
    }

    private static class ItemEssenceEntry implements ItemEssenceConversion
    {
        ItemStack item;
        MagicAmounts amounts;

        public ItemEssenceEntry(ItemStack item, MagicAmounts amounts)
        {
            this.item = item;
            this.amounts = amounts;
        }

        @Override
        public void putAll(Map<ItemStack, MagicAmounts> essences)
        {
            essences.put(item, amounts);
        }

        @Override
        public ItemEssenceEntry all(int amount)
        {
            amounts.all(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry fire(int amount)
        {
            amounts.fire(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry water(int amount)
        {
            amounts.water(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry air(int amount)
        {
            amounts.air(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry earth(int amount)
        {
            amounts.earth(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry light(int amount)
        {
            amounts.light(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry darkness(int amount)
        {
            amounts.darkness(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry life(int amount)
        {
            amounts.life(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry death(int amount)
        {
            amounts.death(amount);
            return this;
        }
    }

    private static class ItemEssenceCollection extends ArrayList<ItemEssenceConversion> implements ItemEssenceConversion
    {
        @Override
        public void putAll(Map<ItemStack, MagicAmounts> essences)
        {
            for(ItemEssenceConversion c : this)
                c.putAll(essences);
        }

        @Override
        public ItemEssenceCollection all(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.all(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection fire(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.fire(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection water(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.water(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection air(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.air(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection earth(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.earth(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection light(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.light(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection darkness(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.darkness(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection life(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.life(amount);
            return this;
        }

        @Override
        public ItemEssenceCollection death(int amount)
        {
            for (ItemEssenceConversion e : this)
                e.death(amount);
            return this;
        }
    }
}

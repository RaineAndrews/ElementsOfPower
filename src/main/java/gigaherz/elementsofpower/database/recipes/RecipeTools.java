package gigaherz.elementsofpower.database.recipes;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class RecipeTools
{
    public static Map<ItemStack, List<ItemStack>> itemSources = new HashMap<>();
    public static List<ItemStack> itemRoots = new ArrayList<>();

    public static List<IRecipeHandler> recipeHandlers = new ArrayList<>();

    static {
        recipeHandlers.add(new ShapedRecipeHandler());
        recipeHandlers.add(new ShapelessRecipeHandler());
        recipeHandlers.add(new ShapedOreRecipeHandler());
        recipeHandlers.add(new ShapelessOreRecipeHandler());
    }

    static int gcd(int a, int b)
    {
        for (; ; )
        {
            if (a == 0) return b;
            b %= a;
            if (b == 0) return a;
            a %= b;
        }
    }

    static int lcm(int a, int b)
    {
        int temp = gcd(a, b);
        return temp > 0 ? (a / temp * b) : 0;
    }

    public static void gatherRecipes()
    {
        Set<Class<? extends IRecipe>> seenClasses = new HashSet<>();

        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList())
        {
            IRecipeInfoProvider provider = null;

            for(IRecipeHandler h : recipeHandlers)
            {
                if(h.accepts(recipe))
                {
                    provider = h.handle(recipe);
                    break;
                }
            }

            if (provider == null)
            {
                Class<? extends IRecipe> c = recipe.getClass();
                if(!seenClasses.contains(c))
                {
                    seenClasses.add(c);
                    ElementsOfPower.logger.warn("Ignoring unhandled recipe class: " + c.getName());
                }
                continue;
            }

            processRecipe(provider);
        }

        for (Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet())
        {
            processRecipe(new FurnaceRecipeInfo(entry.getKey(), entry.getValue()));
        }

        for (Map.Entry<ItemStack, List<ItemStack>> entry : itemSources.entrySet())
        {
            for (ItemStack s : entry.getValue())
            {
                boolean found = false;
                for (ItemStack l : itemRoots)
                {
                    if (Utils.compareItemStacksStrict(s, l))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    ItemStack t = s.copy();
                    t.stackSize = 1;
                    int d = t.getItemDamage();
                    if (d == OreDictionary.WILDCARD_VALUE)
                        t.setItemDamage(0);
                    itemRoots.add(t);
                }
            }
        }
    }

    private static void processRecipe(IRecipeInfoProvider recipe)
    {
        ItemStack output = recipe.getRecipeOutput();

        if (output == null)
        {
            return;
        }

        if (Utils.stackIsInMap(itemSources, output))
        {
            return;
        }

        List<ItemStack> items = recipe.getRecipeInputs();

        items = reduceItemsList(items);

        List<ItemStack> applied = new ArrayList<>();
        output = applyExistingRecipes(output, items, applied);

        applied = reduceItemsList(applied);

        boolean aggregates = false;
        if (applied.size() == 1)
        {
            if (applied.get(0).stackSize < output.stackSize)
            {
                aggregates = true;
            }
        }

        if (!aggregates)
        {
            replaceExistingSources(output, applied);
        }

        itemSources.put(output, applied);
    }

    private static void replaceExistingSources(ItemStack output, List<ItemStack> items)
    {

        List<ItemStack> stacksToRemove = new ArrayList<>();
        Map<ItemStack, List<ItemStack>> stacksToAdd = new HashMap<>();

        for (Map.Entry<ItemStack, List<ItemStack>> entry : itemSources.entrySet())
        {
            ItemStack result = entry.getKey().copy();
            List<ItemStack> stacks = new ArrayList<>();
            int totalMult = 1;
            boolean anythingChanged = false;

            for (ItemStack s : entry.getValue())
            {

                if (Utils.stackFitsInSlot(s, output))
                {

                    int numNeeded = s.stackSize;
                    int numProduced = output.stackSize;
                    int num = lcm(numNeeded, numProduced);

                    int mult = num / numNeeded;

                    result.stackSize *= mult;
                    for (ItemStack t : stacks)
                    {
                        t.stackSize *= mult;
                    }

                    totalMult *= mult;

                    int mult2 = num / numProduced;
                    for (ItemStack t : items)
                    {
                        ItemStack r = t.copy();
                        r.stackSize *= mult2;
                        stacks.add(r);
                    }

                    anythingChanged = true;
                }
                else
                {
                    ItemStack r = s.copy();
                    r.stackSize *= totalMult;
                    stacks.add(r);
                }
            }

            if (anythingChanged)
            {
                stacksToRemove.add(entry.getKey());
                stacksToAdd.put(result, stacks);
            }
        }

        for (ItemStack s : stacksToRemove)
        {
            itemSources.remove(s);
        }

        itemSources.putAll(stacksToAdd);
    }

    public static ItemStack applyExistingRecipes(ItemStack output, List<ItemStack> items, List<ItemStack> applied)
    {
        ItemStack result = output.copy();
        int totalMult = 1;

        for (ItemStack is : items)
        {

            Map.Entry<ItemStack, List<ItemStack>> r = findSources(is);

            if (r != null)
            {
                List<ItemStack> ss = r.getValue();

                if (ss.size() == 1)
                {
                    if (ss.get(0).stackSize < r.getKey().stackSize)
                    {
                        r = null;
                    }
                }
            }

            if (r != null)
            {
                int numNeeded = is.stackSize;
                int numProduced = output.stackSize;
                int num = lcm(numNeeded, numProduced);

                int mult = num / numNeeded;

                result.stackSize *= mult;
                for (ItemStack t : applied)
                {
                    t.stackSize *= mult;
                }

                totalMult *= mult;

                int mult2 = num / numProduced;
                for (ItemStack t : r.getValue())
                {
                    ItemStack q = t.copy();
                    q.stackSize *= mult2;
                    applied.add(q);
                }
            }
            else
            {
                ItemStack q = is.copy();
                q.stackSize *= totalMult;
                applied.add(q);
            }
        }

        if (result.stackSize > 1)
        {
            int cd = result.stackSize;
            for (ItemStack is : applied)
            {
                cd = gcd(cd, is.stackSize);
            }

            if (cd > 1)
            {
                for (ItemStack is : applied)
                {
                    is.stackSize /= cd;
                }
                result.stackSize /= cd;
            }
        }

        return result;
    }

    public static List<ItemStack> reduceItemsList(List<ItemStack> items)
    {
        List<ItemStack> itemsResolved = new ArrayList<>();

        for (ItemStack is : items)
        {
            if (is == null)
            {
                continue;
            }

            aggregateExisting(itemsResolved, is);
        }

        return itemsResolved;
    }

    private static void aggregateExisting(List<ItemStack> itemsResolved, ItemStack is)
    {
        ItemStack existing = Utils.getExistingInList(itemsResolved, is);

        if (existing != null)
        {
            if (existing != is)
            {
                existing.stackSize += is.stackSize;
            }
        }
        else
        {
            ItemStack isc = is.copy();
            itemsResolved.add(isc);
        }
    }

    private static Map.Entry<ItemStack, List<ItemStack>> findSources(ItemStack is)
    {
        for (Map.Entry<ItemStack, List<ItemStack>> entry : itemSources.entrySet())
        {
            if (Utils.compareItemStacksStrict(is, entry.getKey())) return entry;
        }

        return null;
    }
}
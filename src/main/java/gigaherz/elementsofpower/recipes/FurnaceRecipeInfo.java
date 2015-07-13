package gigaherz.elementsofpower.recipes;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FurnaceRecipeInfo implements IRecipeInfoProvider
{

    ArrayList<ItemStack> recipeItems;
    ItemStack recipeOutput;

    public FurnaceRecipeInfo(ItemStack input, ItemStack output)
    {
        ItemStack recipeInput = input.copy();

        ArrayList<ItemStack> inputs = new ArrayList<ItemStack>();
        inputs.add(recipeInput);
        inputs.add(new ItemStack(Items.coal));

        recipeItems = inputs;
        recipeOutput = output.copy();

        recipeInput.stackSize *= 8;
        recipeOutput.stackSize *= 8;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return recipeOutput;
    }

    @Override
    public List<ItemStack> getRecipeInputs()
    {
        return recipeItems;
    }
}

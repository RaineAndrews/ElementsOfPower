package gigaherz.elementsofpower.integration;

import gigaherz.elementsofpower.ElementsOfPower;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.INbtIgnoreList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        //registry.addRecipeCategories(new TestCategory(registry.getJeiHelpers()));
        registry.addRecipeHandlers(
                new ContainerChargeRecipeHandler(),
                new GemstoneChangeRecipeHandler());

        INbtIgnoreList nbtIgnoreList = registry.getJeiHelpers().getNbtIgnoreList();
        nbtIgnoreList.ignoreNbtTagNames(ElementsOfPower.magicWand, "gemstone", "quality");
        nbtIgnoreList.ignoreNbtTagNames(ElementsOfPower.magicStaff, "gemstone", "quality");
        nbtIgnoreList.ignoreNbtTagNames(ElementsOfPower.magicRing, "gemstone", "quality");
        nbtIgnoreList.ignoreNbtTagNames(ElementsOfPower.gemstone, "quality");

        ItemStack orb = new ItemStack(ElementsOfPower.magicOrb, 1, OreDictionary.WILDCARD_VALUE);
        ItemStack gemstone = new ItemStack(ElementsOfPower.gemstone, 1, OreDictionary.WILDCARD_VALUE);
        for (ItemStack stack : Arrays.asList(
                new ItemStack(ElementsOfPower.magicWand),
                new ItemStack(ElementsOfPower.magicStaff),
                new ItemStack(ElementsOfPower.magicRing)
        ))
        {
            registry.addRecipes(
                    Arrays.asList(
                            new GemstoneChangeRecipeWrapper(stack, stack, gemstone),
                            new ContainerChargeRecipeWrapper(stack, stack, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb, orb, orb)
                    ));
        }
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime)
    {
    }
}

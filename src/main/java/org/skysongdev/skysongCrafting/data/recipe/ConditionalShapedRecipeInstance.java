package org.skysongdev.skysongCrafting.data.recipe;

import org.bukkit.Material;
import org.skysongdev.skysongCrafting.data.output.OutputInstance;
import org.skysongdev.skysongstats.Utils.Utils;

import java.util.ArrayList;
import java.util.Map;

public class ConditionalShapedRecipeInstance extends ShapedMarkingRecipeInstance{
    ArrayList<String> conditions;
    public ConditionalShapedRecipeInstance(OutputInstance output, Material displayItem, String recipeName, String craftingTableType, String craftingSound, ArrayList<String> materialConsumption, Map<Utils.Skills, Integer> skillRequirements, ArrayList<Boolean> materialMarks, ArrayList<String> conditions) {
        super(output, displayItem, recipeName, craftingTableType, craftingSound, materialConsumption, skillRequirements, materialMarks);
        this.conditions = conditions;
    }


}

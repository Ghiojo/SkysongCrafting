package org.skysongdev.skysongCrafting.data.output;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.ArrayList;

public class RandomOutput extends OutputInstance{
    private ArrayList<Float> chances;
    private int lowerBoundOfRolls;
    private int upperBoundOfRolls;

    public RandomOutput(ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings, ArrayList<Float> chances, int lowerBoundOfRolls, int upperBoundOfRolls) {
        super(outputItems, outputStrings, OutputType.RANDOM);
        this.chances = chances;
        this.lowerBoundOfRolls = lowerBoundOfRolls;
        this.upperBoundOfRolls = upperBoundOfRolls;
    }

    @Override
    public ArrayList<SkysongItemWithAmmount> getOutput(ArrayList<ItemStack> markedItems) {
        int rolls = (int) Math.floor(Math.random() * (upperBoundOfRolls - lowerBoundOfRolls + 1) + lowerBoundOfRolls);
        ArrayList<SkysongItemWithAmmount> finalOutput = new ArrayList<>();
        // Normalize chances so they sum to 1.0
        float total = 0f;
        for (float c : chances) total += c;
        if (total <= 0) return finalOutput;

        for (int i = 0; i < rolls; i++) {
            float roll = (float) Math.random();
            float cumulativeChance = 0f;

            for (int j = 0; j < chances.size(); j++) {
                cumulativeChance += chances.get(j) / total; // normalize
                if (roll <= cumulativeChance) {
                    finalOutput.add(getOutputItems().get(j));
                    break;
                }
            }
        }

        return finalOutput;
    }

    @Override
    public ArrayList<String> getOutputStrings() {
        ArrayList<String> finalOutput = new ArrayList<String>();
        for(int i = 0; i < outputStrings.size(); i++){
            finalOutput.add("<gray>" + chances.get(i)*100 + "% chance of " + outputStrings.get(i));
        }
        return finalOutput;
    }
}

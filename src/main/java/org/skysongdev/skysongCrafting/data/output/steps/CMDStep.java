package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.CompositeOutput;
import org.skysongdev.skysongCrafting.data.output.data.AmountType;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;
import org.skysongdev.skysongCrafting.utils.TypeCastingUtils;

import java.util.ArrayList;

import static org.skysongdev.skysongCrafting.SkysongCrafting.Instance;
import static org.skysongdev.skysongCrafting.utils.Constants.AMOUNT_TYPE_MAP;

public class CMDStep extends OutputStep{
    String operation;
    AmountType amountType;
    int index;
    ArrayList<String> amounts;

    public CMDStep(String operation, AmountType amountType, int index, ArrayList<String> amounts) {
        super();
        this.operation = operation;
        this.amountType = amountType;
        this.index = index;
        this.amounts = amounts;
    }

    @Override
    public StepData executeStep(StepData stepData) {
        StepData newData = stepData;
        ItemStack item = stepData.getPivotItem();
        ItemMeta meta = item.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();

        float numberAmount = 0;
        if(amountType == AmountType.INTEGER || amountType == AmountType.FLOAT){
            for(String amount : amounts){
                String parsedAmount = PlaceholderParser.parsePlaceholder(amount, stepData);
                try{
                    numberAmount += Float.parseFloat(parsedAmount);
                } catch (NumberFormatException e){
                    Instance().getLogger().warning("Invalid number format for tag amount: " + parsedAmount);
                    this.doesGoToNextStep = false;
                    return newData;
                }
            }
        }
        Object finalAmount;
        switch(amountType){
            case INTEGER -> finalAmount = Math.round(numberAmount);
            case FLOAT -> finalAmount = numberAmount;
            case BOOLEAN -> finalAmount = Boolean.parseBoolean(amounts.get(0));
            default -> finalAmount = amounts.get(0); // STRING
        }
        ArrayList currentCMD;
        switch(amountType){
            case INTEGER:
                currentCMD = new ArrayList<Color>(cmdComponent.getColors());
                break;
            case FLOAT:
                currentCMD = new ArrayList<Float>(cmdComponent.getFloats());
                break;
            case STRING:
                currentCMD = new ArrayList<String>(cmdComponent.getStrings());
                break;
            case BOOLEAN:
                currentCMD = new ArrayList<Boolean>(cmdComponent.getFlags());
                break;
            default:
                Instance().getLogger().warning("Invalid amount type: " + amountType);
                this.doesGoToNextStep = false;
                return newData;
        }

        switch(operation){
            case "set":
                if(index < 0 || index >= currentCMD.size()){
                    Instance().getLogger().warning("Invalid index for setting custom model data: " + index);
                    this.doesGoToNextStep = false;
                    return newData;
                }
                currentCMD.set(index, AMOUNT_TYPE_MAP.get(amountType).cast(finalAmount));
                break;
            case "add":
                currentCMD.add(AMOUNT_TYPE_MAP.get(amountType).cast(finalAmount));
                break;
            case "remove":
                if(index < 0 || index >= currentCMD.size()){
                    Instance().getLogger().warning("Invalid index for removing from custom model data: " + index);
                    this.doesGoToNextStep = false;
                    return newData;
                }
                currentCMD.remove(index);
                break;
        }

        switch(amountType){
            case INTEGER:
                cmdComponent.setColors((ArrayList<Color>) currentCMD);
                break;
            case FLOAT:
                cmdComponent.setFloats((ArrayList<Float>) currentCMD);
                break;
            case STRING:
                cmdComponent.setStrings((ArrayList<String>) currentCMD);
                break;
            case BOOLEAN:
                cmdComponent.setFlags((ArrayList<Boolean>) currentCMD);
                break;
        }

        meta.setCustomModelDataComponent(cmdComponent);
        item.setItemMeta(meta);
        newData.setPivotItem(item);

        return newData;
    }

    @Override
    public boolean goToNextStep() {
        return doesGoToNextStep;
    }
}

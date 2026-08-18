package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.CompositeOutput;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import static org.skysongdev.skysongCrafting.SkysongCrafting.Instance;

public class PropertyStep extends OutputStep {
    String operation;
    String propertyId;
    public PropertyStep(String operation, String propertyId) {
        super();
        this.operation = operation;
        this.propertyId = propertyId;
    }

    @Override
    public StepData executeStep(StepData stepData) {
        StepData newData = stepData;
        if(!Utils.isAProperty(propertyId)){
            Instance().getLogger().warning("Invalid property: " + propertyId);
            doesGoToNextStep = false;
            return newData;
        }
        ItemStack item = stepData.getPivotItem();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        switch (operation){
            case "set":
                String properties = container.get(Constants.propertiesKey, PersistentDataType.STRING);
                String[] currentProperties = properties != null ? properties.split(";") : new String[0];
                for(String property : currentProperties){
                    if(property.equals(propertyId)){
                        Instance().getLogger().warning("Item already has property: " + propertyId);
                        return newData;
                    }
                }
                properties = (properties == null || properties.isEmpty()) ? propertyId : properties + ";" + propertyId;
                container.set(Constants.propertiesKey, PersistentDataType.STRING, properties);
                break;
            case "remove":
                properties = container.get(Constants.propertiesKey, PersistentDataType.STRING);
                if(properties == null){
                    Instance().getLogger().warning("Item does not have any properties to remove.");
                    return newData;
                }
                currentProperties = properties.split(";");
                String newProperties = "";
                for(String property : currentProperties) {
                    if (property.equals(propertyId)) {
                        continue;
                    }
                    if (newProperties.equals("")) {
                        newProperties = property;
                    } else {
                        newProperties = newProperties + ";" + property;
                    }
                }
                container.set(Constants.propertiesKey, PersistentDataType.STRING, newProperties);
                break;
            default:
                Instance().getLogger().warning("Invalid operation: " + operation);
                doesGoToNextStep = false;
                return newData;
        }
        item.setItemMeta(meta);
        newData.setPivotItem(item);
        return newData;
    }

    @Override
    public boolean goToNextStep() {
        return doesGoToNextStep;
    }
}

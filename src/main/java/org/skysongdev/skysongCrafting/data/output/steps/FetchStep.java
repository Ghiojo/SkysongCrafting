package org.skysongdev.skysongCrafting.data.output.steps;

import org.skysongdev.skysongCrafting.data.MarkedItem;
import org.skysongdev.skysongCrafting.data.output.CompositeOutput;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.utils.Constants;
import org.skysongdev.skysongitems.utils.SkysongItem;
import org.skysongdev.skysongitems.utils.Utils;

import java.io.File;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class FetchStep extends OutputStep{
    public enum FetchType {
        MARKED,
        GENERATED
    }

    String idToFetch;
    FetchType fetchType;

    public FetchStep(String idToFetch, FetchType fetchType) {
        super();
        this.idToFetch = idToFetch;
        this.fetchType = fetchType;
    }

    @Override
    public StepData executeStep(StepData stepData) {
        StepData returnData = stepData;
        if(fetchType == FetchType.GENERATED) {
            File itemFile = Constants.ITEMS_PATH.resolve(idToFetch + ".yml").toFile();
            if(!itemFile.exists()) {
                System.out.println("Item file not found for id: " + idToFetch);
                return stepData;
            }
            SkysongItem item = Utils.LoadSkysongItem(itemFile);
            returnData.setPivotItem(getPlugin().getUtils().GenerateSkysongItem(item));
        } else if(fetchType == FetchType.MARKED) {
            int index = 0;
            try {
                index = Integer.parseInt(idToFetch);
            } catch (NumberFormatException e) {
                System.out.println("Invalid id to fetch: " + idToFetch);
                return stepData;
            }
            if(index < 0 || index >= stepData.getMarkedItems().size()) {
                System.out.println("Index out of bounds for marked items: " + index);
                return stepData;
            }
            MarkedItem itemToFetch = stepData.getMarkedItems().get(index);
            returnData.setPivotItem(itemToFetch.getItemStack());
        }
        return returnData;
    }

    @Override
    public boolean goToNextStep() {
        return true;
    }
}

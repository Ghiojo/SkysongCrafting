package org.skysongdev.skysongCrafting.utils;

import org.bukkit.Bukkit;
import org.skysongdev.skysongCrafting.data.MaterialCount;
import org.skysongdev.skysongCrafting.data.recipe.RecipeInstance;
import org.skysongdev.skysongCrafting.data.workstations.Workstation;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;

import java.util.*;

public class Utils {
    private static Utils instance;

    // Workbench Recipes
    private static Map<ArrayList<String>, ArrayList<RecipeInstance>> shapedRecipeMap;
    //TODO: When creating the ArrayLists for MaterialCounts, it should make sure the order of the materials is in alphabetical order by ID.
    private static Map<ArrayList<MaterialCount>, ArrayList<RecipeInstance>> shapelessRecipeMap;

    //Workstation Types
    private static Map<String, Workstation> workstationTypes;

    //Workstation References
    private static Map<ArrayList<Integer>, ArrayList<WorkstationReference>> workstationReferences;

    //Tag Map
    public static Map<String, ArrayList<String>> tagMap;

    public Map<ArrayList<String>, ArrayList<RecipeInstance>> getShapedRecipeMap() {
        return shapedRecipeMap;
    }

    public Map<ArrayList<MaterialCount>, ArrayList<RecipeInstance>> getShapelessRecipeMap() {
        return shapelessRecipeMap;
    }

    public Map<String, ArrayList<String>> getTagMap() {
        return tagMap;
    }

    public Map<String, Workstation> getWorkstationTypes() {
        return workstationTypes;
    }

    public static Utils Instance(){
        if(instance == null){
            instance = new Utils();
        }
        return instance;
    }

    public static Map<ArrayList<Integer>, ArrayList<WorkstationReference>> getWorkstationReferences(){
        return Instance().workstationReferences;
    }

    private Utils() {
        instance = this;
        shapedRecipeMap = new HashMap<>();
        shapelessRecipeMap = new HashMap<>();
        workstationTypes = new HashMap<>();
        workstationReferences = new HashMap<>();
        tagMap = new HashMap<>();
    }


    public ArrayList<MaterialCount> inputToMaterialCountList(ArrayList<String> input) {
        ArrayList<MaterialCount> result = new ArrayList<>();
        if (input == null || input.isEmpty()) return result;

        // Usar un mapa para agrupar por id (case-insensitive usando el id en la forma original)
        Map<String, MaterialCount> map = new HashMap<>();

        for (String raw : input) {
            if (raw == null) continue;
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;

            String id;
            int amount = 1;

            String[] parts = trimmed.split("\\*", 2); // dividir en como máximo 2 partes
            id = parts[0].trim();
            if (parts.length == 2) {
                try {
                    amount = Integer.parseInt(parts[1].trim());
                    if (amount < 1) amount = 1;
                } catch (NumberFormatException e) {
                    // Si no es un número válido, usar 1
                    amount = 1;
                }
            }

            // Si ya existe el mismo id (ignorando mayúsculas), sumar la cantidad
            // Mantener la primera forma de id encontrada como getId()
            String key = id.toLowerCase(Locale.ROOT);
            MaterialCount existing = map.get(key);
            if (existing == null) {
                map.put(key, new MaterialCount(id, amount));
            } else {
                existing.setCount(existing.getCount() + amount);
            }
        }

        result.addAll(map.values());

        // Ordenar alfabéticamente por getId(), case-insensitive
        result.sort(Comparator.comparing(mc -> mc.getId(), String.CASE_INSENSITIVE_ORDER));

        return result;
    }

    public static List<List<String>> generateTagCombinations(ArrayList<String> inputTags) {
        ArrayList<List<String>> options = new ArrayList<>();
        for(String material : inputTags){
            if (material.startsWith("#")) {
                String tag = material.substring(1);
                List<String> tagItems = org.skysongdev.skysongCrafting.utils.Utils.Instance().tagMap.get(tag);
                if (tagItems == null || tagItems.isEmpty()) {
                    tagItems = List.of("");
                }
                options.add(tagItems);
            } else {
                options.add(List.of(material));
            }
        }

        List<List<String>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<>());

        for (List<String> option : options) {
            List<List<String>> newCombinations = new ArrayList<>();
            for (List<String> combination : combinations) {
                for (String item : option) {
                    List<String> newCombination = new ArrayList<>(combination);
                    newCombination.add(item);
                    newCombinations.add(newCombination);
                }
            }
            combinations = newCombinations;
        }

        for(List<String> combo : combinations){
            Bukkit.getLogger().info("Generated combination: " + combo);
        }

        return combinations;
    }

    public ArrayList<WorkstationReference> addOrReplaceWorkstationReference(ArrayList<WorkstationReference> list, WorkstationReference newRef) {
        if (newRef == null) {
            return (list != null) ? list : new ArrayList<>();
        }

        if (list == null) {
            list = new ArrayList<>();
            list.add(newRef);
            return list;
        }

        String newWorld = newRef.getWorldName();

        for (int i = 0; i < list.size(); i++) {
            WorkstationReference existing = list.get(i);
            if (existing == null) continue;
            String existingWorld = existing.getWorldName();

            if ((newWorld == null && existingWorld == null) ||
                    (newWorld != null && newWorld.equals(existingWorld))) {
                list.set(i, newRef);
                return list; // reemplazado
            }
        }

        // no se encontró coincidencia por world, añadir al final
        list.add(newRef);
        return list;
    }

    public ArrayList<WorkstationReference> removeWorkstationReferenceByWorld(ArrayList<WorkstationReference> list, String worldName) {
        if (list == null || worldName == null) return list;

        var it = list.iterator();
        while (it.hasNext()) {
            WorkstationReference ref = it.next();
            if (ref == null) continue;
            String refWorld = ref.getWorldName();

            if ((refWorld == null && worldName == null) ||
                    (refWorld != null && refWorld.equals(worldName))) {
                it.remove();
            }
        }

        return list.isEmpty() ? null : list;
    }

    public WorkstationReference getWorkstationReferenceByWorld(ArrayList<WorkstationReference> list, String worldName) {
        if (list == null) return null;

        for (WorkstationReference ref : list) {
            if (ref == null) continue;
            String refWorld = ref.getWorldName();
            if ((refWorld == null && worldName == null) ||
                    (refWorld != null && refWorld.equals(worldName))) {
                return ref;
            }
        }

        return null;
    }
}

package org.skysongdev.skysongCrafting;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.skysongdev.skysongCrafting.commands.SkysongCraftingCommand;
import org.skysongdev.skysongCrafting.data.database.Database;
import org.skysongdev.skysongCrafting.listeners.GUIListener;
import org.skysongdev.skysongCrafting.listeners.RandomIdListener;
import org.skysongdev.skysongCrafting.listeners.WorkstationListener;
import org.skysongdev.skysongCrafting.utils.Constants;
import org.skysongdev.skysongCrafting.utils.GUIUtils;
import org.skysongdev.skysongCrafting.utils.Utils;
import org.skysongdev.skysongCrafting.utils.YamlParsingUtils;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.skysongdev.skysongCrafting.utils.Constants.RECIPES_PATH;

public final class SkysongCrafting extends JavaPlugin {
    public static SkysongCrafting instance;
    private Database database;
    public Database getDatabase(){ return database; }
    public Constants constants;

    public SkysongCraftingCommand skysongCraftingCommand = new SkysongCraftingCommand();

    public static SkysongCrafting Instance(){
        if(instance == null) {
            instance = new SkysongCrafting();
        }
        return instance;
    }

    private MiniMessage miniMessage;

    public static MiniMessage GetMM(){
        SkysongCrafting instance = Instance();
        if(instance.miniMessage == null) {
            instance.miniMessage = MiniMessage.miniMessage();
        }
        return instance.miniMessage;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        Utils.Instance();
        GUIUtils.Instance();
        constants = new Constants() {};
        saveDefaultConfig();
        initializeDefaultResources();
        InitializeDatabase();
        InitializeFiles();
        InitializeCommands();
        AssignListeners();
        try{
            database.dumpWorkstationData();
        }catch(SQLException e){
            Bukkit.getLogger().warning("[SkysongItems] Unable to load workstations from database!");
            e.printStackTrace();
        }

        Bukkit.getLogger().info("WorkstationReferences loaded: " + Utils.getWorkstationReferences().size());
        Bukkit.getLogger().info("WorkstationReferences details: " + Utils.getWorkstationReferences().toString());
    }

    public void InitializeDatabase(){
        try{
            this.database = new Database(
                    getConfig().getString("database.host"),
                    getConfig().getString("database.port"),
                    getConfig().getString("database.user"),
                    getConfig().getString("database.password"),
                    getConfig().getString("database.database_name"));
            database.initializeDatabase();
        }catch(SQLException e){
            Bukkit.getLogger().warning("[SkysongCrafting] Unable to connect to database and create tables!");
            e.printStackTrace();
        }
    }

    public void InitializeFiles(){
        YamlParsingUtils.InstanceTags();
        YamlParsingUtils.initializeWorkbenches();
        initializeRecipes();
    }

    public void initializeRecipes(){
        Utils.Instance().getShapedRecipeMap().clear();
        Utils.Instance().getShapelessRecipeMap().clear();
        for(File file : RECIPES_PATH.resolve("shaped").toFile().listFiles()){
            YamlParsingUtils.initializeRecipeInstance(file);
        }
        for(File file : RECIPES_PATH.resolve("shapeless").toFile().listFiles()){
            YamlParsingUtils.initializeRecipeInstance(file);
        }
        for(File file : RECIPES_PATH.resolve("smelting").toFile().listFiles()){
            YamlParsingUtils.initializeRecipeInstance(file);
        }
    }

    public void InitializeCommands(){
        getCommand("sc").setExecutor(skysongCraftingCommand);
    }

    public void AssignListeners(){
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new RandomIdListener(), this);
        getServer().getPluginManager().registerEvents(new WorkstationListener(), this);
    }

    public void initializeDefaultResources() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            getLogger().warning("No se pudo crear la carpeta de datos: " + dataFolder.getPath());
            return;
        }

        String[] prefixes = new String[] {
                "items/",
                "output/",
                "tags/",
                "recipes/",
                "workstations/",
                "config.yml"
        };

        try {
            URL codeLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
            Path codePath = Paths.get(codeLocation.toURI());

            // Determinar qué prefijos saltar: si existe cualquier archivo en la carpeta destino
            java.util.Set<String> skipPrefixes = new java.util.HashSet<>();
            for (String p : prefixes) {
                Path target = dataFolder.toPath().resolve(p).normalize();
                try {
                    if (p.endsWith("/")) {
                        // Para prefijos que son directorios, si existe la carpeta y contiene cualquier archivo (a cualquier profundidad), saltar
                        if (Files.exists(target) && Files.isDirectory(target)) {
                            try (java.util.stream.Stream<Path> s = Files.walk(target)) {
                                boolean hasAnyFile = s.anyMatch(path -> !Files.isDirectory(path));
                                if (hasAnyFile) skipPrefixes.add(p);
                            }
                        }
                    } else {
                        // Para prefijos que son archivos (ej. config.yml), si ya existe cualquier archivo en la carpeta de datos, saltar
                        try (java.util.stream.Stream<Path> s = Files.list(dataFolder.toPath())) {
                            boolean anyFile = s.anyMatch(path -> !Files.isDirectory(path));
                            if (anyFile) skipPrefixes.add(p);
                        }
                        // Además, si el archivo específico ya existe, también saltar
                        if (Files.exists(target)) skipPrefixes.add(p);
                    }
                } catch (IOException e) {
                    getLogger().warning("Error comprobando existencia de recursos en " + target + ": " + e.getMessage());
                }
            }

            if (Files.isDirectory(codePath)) {
                for (String prefix : prefixes) {
                    if (skipPrefixes.contains(prefix)) continue;
                    Path src = codePath.resolve(prefix);
                    if (!Files.exists(src)) continue;
                    Files.walk(src).forEach(sourcePath -> {
                        try {
                            Path relative = src.relativize(sourcePath);
                            Path target = dataFolder.toPath().resolve(prefix).resolve(relative).normalize();
                            if (Files.isDirectory(sourcePath)) {
                                Files.createDirectories(target);
                            } else {
                                if (!Files.exists(target)) {
                                    Files.createDirectories(target.getParent());
                                    Files.copy(sourcePath, target);
                                }
                            }
                        } catch (IOException e) {
                            getLogger().warning("Error copiando recurso " + sourcePath + ": " + e.getMessage());
                        }
                    });
                }
            } else {
                try (JarFile jar = new JarFile(codePath.toFile())) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        for (String p : prefixes) {
                            if (skipPrefixes.contains(p)) continue;
                            if (name.equals(p) || name.startsWith(p)) {
                                File outFile = new File(dataFolder, name);
                                if (entry.isDirectory()) {
                                    if (!outFile.exists()) outFile.mkdirs();
                                } else {
                                    File parent = outFile.getParentFile();
                                    if (parent != null && !parent.exists()) parent.mkdirs();
                                    if (outFile.exists()) break; // no overwriting
                                    try (InputStream in = jar.getInputStream(entry);
                                         OutputStream out = new FileOutputStream(outFile)) {
                                        in.transferTo(out);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            getLogger().info("Default resources initialized in " + dataFolder.getPath());
        } catch (Exception e) {
            getLogger().warning("Failed to initialize default resources: " + e.getMessage());
        }
    }
}

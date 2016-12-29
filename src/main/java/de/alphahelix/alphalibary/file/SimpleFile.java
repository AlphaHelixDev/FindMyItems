/*
 *     Copyright (C) <2016>  <AlphaHelixDev>
 *
 *     This program is free software: you can redistribute it under the
 *     terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.alphahelix.alphalibary.file;

import de.alphahelix.alphalibary.AlphaPlugin;
import de.alphahelix.alphalibary.item.ItemBuilder;
import de.alphahelix.alphalibary.utils.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleFile<P extends AlphaPlugin> extends YamlConfiguration {

    private P pl;
    private File source = null;

    /**
     * Create a new {@link SimpleFile} inside the given path with the name 'name'
     *
     * @param path   the path where the {@link File} should be created in
     * @param name   the name which the {@link File} should have
     * @param plugin your main class which extends '{@link AlphaPlugin}'
     */
    public SimpleFile(String path, String name, P plugin) {
        this.pl = plugin;
        new File(path).mkdirs();
        source = new File(path, name);
        createIfNotExist();
    }

    /**
     * Create a new {@link SimpleFile} inside the plugin path with the name 'name'
     *
     * @param plugin your main class which extends '{@link AlphaPlugin}'
     * @param name   the name which the file should have
     */
    public SimpleFile(String name, P plugin) {
        if (plugin == null) {
            return;
        }
        this.pl = plugin;
        new File(plugin.getDataFolder().getPath()).mkdirs();
        source = new File(plugin.getDataFolder().getPath(), name);
        createIfNotExist();
    }

    /**
     * Convert a normal {@link File} into a {@link SimpleFile}
     *
     * @param f      the old File which you want to convert
     * @param plugin your main class which extends {@link AlphaPlugin}
     */
    public SimpleFile(File f, P plugin) {
        this.pl = plugin;
        source = f;
        createIfNotExist();
    }

    /**
     * Finish the setup of the {@link SimpleFile}
     */
    private void finishSetup() {
        try {
            load(source);
        } catch (Exception ignored) {

        }
    }

    /**
     * Overridden method to add new standart values to a config
     */
    public void addValues() {

    }

    /**
     * Create a new {@link SimpleFile} if it's not existing
     */
    private void createIfNotExist() {

        options().copyDefaults(true);
        if (source == null || !source.exists()) {
            try {
                source.createNewFile();
            } catch (IOException e) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            source.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTaskLaterAsynchronously(getPluginInstance(), 20);
            }
        }
        finishSetup();
    }

    /**
     * Get a colored {@link String}
     *
     * @param path the path inside this {@link SimpleFile}
     * @return the {@link String} with Colors
     */
    public String getColorString(String path) {
        if (!contains(path))
            return "";

        try {
            String toReturn = getString(path);
            return ChatColor.translateAlternateColorCodes('&', toReturn);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get a colored {@link ArrayList} out of this {@link SimpleFile}
     *
     * @param path the path inside this {@link SimpleFile}
     * @return the {@link ArrayList} with Colors
     */
    public ArrayList<String> getColorStringList(String path) {
        if (!configContains(path)) return new ArrayList<>();
        if (!isList(path)) return new ArrayList<>();

        try {
            ArrayList<String> tR = new ArrayList<>();
            for (String str : getStringList(path)) {
                tR.add(str.replace("&", "§"));
            }
            return tR;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getPlaceholderString(String path, String placeHolder, String placeHolderObject) {
        if(!configContains(path)) return "";
        if(!isString(path)) return "";

        return getColorString(path).replace("["+placeHolder+"]", placeHolderObject);
    }

    /**
     * Save a base64 encoded {@link ItemStack}[] inside this {@link SimpleFile}
     *
     * @param path   where to save the {@link ItemStack}[]
     * @param toSave the {@link ItemStack}[] to save
     */
    public void setBase64ItemStackArray(String path, ItemStack... toSave) {
        SerializationUtil<ItemStack[]> serializer = new SerializationUtil<ItemStack[]>();

        if (configContains(path)) {
            override(path, SerializationUtil.jsonToString(serializer.serialize(toSave)));
        } else {
            setDefault(path, SerializationUtil.jsonToString(serializer.serialize(toSave)));
        }
    }

    /**
     * Gets a base64 encoded {@link ItemStack}[] out of this {@link SimpleFile}
     *
     * @param path where the {@link ItemStack}[] should be located at
     * @return the {@link ItemStack}[] which was saved
     */
    public ItemStack[] getBase64ItemStackArray(String path) {
        SerializationUtil<ItemStack[]> serializer = new SerializationUtil<ItemStack[]>();

        return serializer.deserialize(SerializationUtil.stringToJson(getString(path)));
    }

    /**
     * Save a {@link ItemStack}[] inside this {@link SimpleFile}
     *
     * @param path   where to save the {@link ItemStack}[]
     * @param toSave the {@link ItemStack}[] to save
     */
    public void setItemStackArray(String path, ItemStack... toSave) {
        if (configContains(path)) {
            for (ItemStack is : toSave) {
                int keys = getConfigurationSection(path).getKeys(false).size();
                override(path + "." + keys + ".type", is.getType().name().toLowerCase().replace("_", " "));
                if (is.hasItemMeta()) {
                    if (is.getItemMeta().hasDisplayName()) {
                        override(path + "." + keys + ".name", is.getItemMeta().getDisplayName());
                    } else {
                        override(path + "." + keys + ".name", is.getType().name().toLowerCase().replace("_", " "));
                    }
                    if (is.getItemMeta().hasLore()) {
                        setArgumentList(path + "." + keys + ".lore", is.getItemMeta().getLore().toArray(new String[is.getItemMeta().getLore().size()]));
                    } else {
                        setArgumentList(path + "." + keys + ".lore", "");
                    }
                } else {
                    override(path + "." + keys + ".name", is.getType().name().toLowerCase().replace("_", " "));
                    setArgumentList(path + "." + keys + ".lore", "");
                }
            }
        } else {
            int keys = 0;
            for (ItemStack is : toSave) {
                setDefault(path + "." + keys + ".type", is.getType().name().toLowerCase().replace("_", " "));
                if (is.hasItemMeta()) {
                    if (is.getItemMeta().hasDisplayName()) {
                        setDefault(path + "." + keys + ".name", is.getItemMeta().getDisplayName());
                    } else {
                        setDefault(path + "." + keys + ".name", is.getType().name().toLowerCase().replace("_", " "));
                    }
                    if (is.getItemMeta().hasLore()) {
                        setArgumentList(path + "." + keys + ".lore", is.getItemMeta().getLore().toArray(new String[is.getItemMeta().getLore().size()]));
                    } else {
                        setArgumentList(path + "." + keys + ".lore", "");
                    }
                } else {
                    setDefault(path + "." + keys + ".name", is.getType().name().toLowerCase().replace("_", " "));
                    setArgumentList(path + "." + keys + ".lore", "");
                }
                keys++;
            }
        }
    }

    /**
     * Gets a {@link ItemStack}[] out of this {@link SimpleFile}
     *
     * @param path where the {@link ItemStack}[] should be located at
     * @return the {@link ItemStack}[] which was saved
     */
    public ItemStack[] getItemStackArray(String path) {
        if (configContains(path)) {
            ArrayList<ItemStack> items = new ArrayList<>();
            for (int key = 0; key < getConfigurationSection(path).getKeys(false).size(); key++) {
                items.add(new ItemBuilder(
                        Material.getMaterial(getString(path + "." + key + ".type").toUpperCase().replace(" ", "_")))
                        .setName(getColorString(path + "." + key + ".name"))
                        .setLore(getColorStringList(path + "." + key + ".lore").toArray(new String[getColorStringList(path + "." + key + ".lore").size()]))
                        .build()
                );
            }
            return items.toArray(new ItemStack[items.size()]);
        }
        return new ItemStack[0];
    }

    /**
     * Saves an array of {@link Material} names as a {@link List} inside this {@link SimpleFile}
     *
     * @param path      where to save the {@link List}
     * @param materials the name of the {@link Material}s you want to save
     */
    public void setMaterialStringList(String path, String... materials) {
        ArrayList<String> stacks = new ArrayList<>();
        Collections.addAll(stacks, materials);
        set(path, stacks);
        save();
    }

    /**
     * Gets the {@link List} with all {@link Material} names from this {@link SimpleFile}
     *
     * @param path where the {@link List} should be located at
     * @return the {@link List} with all {@link Material} names
     */
    public List<String> getMaterialStringList(String path) {
        return getStringList(path);
    }

    /**
     * Saves a {@link Inventory} inside this {@link SimpleFile}
     *
     * @param path   where to save the {@link Inventory}
     * @param toSave the {@link Inventory} to save
     */
    public void setBase64Inventory(String path, Inventory toSave) {
        if (configContains(path)) {
            override(path + ".title", toSave.getTitle());
            override(path + ".size", ((toSave.getSize() / 9) + 1) * 9);
            setBase64ItemStackArray(path + ".content", toSave.getContents());
        } else {
            setDefault(path + ".title", toSave.getTitle());
            setDefault(path + ".size", ((toSave.getSize() / 9) + 1) * 9);
            setBase64ItemStackArray(path + ".content", toSave.getContents());
        }
    }

    /**
     * Gets the {@link Inventory} from this {@link SimpleFile}
     *
     * @param path where the {@link Inventory} is located at
     * @return the {@link Inventory}
     */
    public Inventory getBase64Inventory(String path) {
        Inventory toReturn = Bukkit.createInventory(null, getInt(path + ".size"), getColorString(path + ".title"));

        for (ItemStack is : getBase64ItemStackArray(path + ".content")) {
            if (is != null && is.getType() != Material.AIR)
                toReturn.addItem(is);
        }

        return toReturn;
    }

    public void setMaterial(String path, Material material) {
        if(configContains(path)) {
            override(path, material.name().replace("_", " ").toUpperCase());
        } else {
            setDefault(path, material.name().replace("_", " ").toUpperCase());
        }
    }

    public Material getMaterial(String path) {
        if(configContains(path)) {
            return Material.getMaterial(getString(path).replace(" ", "_").toUpperCase());
        }
        return Material.AIR;
    }

    /**
     * Saves a {@link Location} inside the {@link SimpleFile}
     *
     * @param path where to save the {@link Location}
     * @param loc  the {@link Location} to save
     */
    public void setLocation(String path, Location loc) {
        SerializationUtil<Location> serializer = new SerializationUtil<>();

        set(path, SerializationUtil.jsonToString(serializer.serialize(loc)));
        save();
    }

    /**
     * Gets a {@link Location} from this {@link SimpleFile}
     *
     * @param path where the {@link Location} should be located at
     * @return the {@link Location} which is saved
     */
    public Location getLocation(String path) {
        SerializationUtil serializer = new SerializationUtil<Location>();

        return (Location) serializer.deserialize(SerializationUtil.stringToJson(getString(path)));
    }

    public <T> void setBase64ArgumentList(String path, T... listArguments) {
        List<String> argsAtBase64 = new ArrayList<>();
        SerializationUtil<T> serializer = new SerializationUtil<>();

        for (T arg : listArguments) {
            argsAtBase64.add(SerializationUtil.jsonToString(serializer.serialize(arg)));
        }

        if (configContains(path))
            override(path, argsAtBase64);
        else
            setDefault(path, argsAtBase64);
    }

    public <T> ArrayList<T> getBase64ArgumentList(String path) {
        ArrayList<T> args = new ArrayList<>();
        SerializationUtil<T> serializer = new SerializationUtil<>();

        if (configContains(path))
            for (Object base64arg : getList(path)) {
                args.add(serializer.deserialize(SerializationUtil.stringToJson((String) base64arg)));
            }

        return args;
    }

    public <T> void addBase64ArgumentsToList(String path, T... arguments) {
        ArrayList<T> args = getBase64ArgumentList(path);

        for (T arg : arguments) {
            args.add(arg);
        }

        setBase64ArgumentList(path, args.toArray());
    }

    public <T> void removeBase64ArgumentsFromList(String path, T... arguments) {
        ArrayList<T> args = getBase64ArgumentList(path);

        for (T arg : arguments) {
            if (args.contains(arg))
                args.remove(arg);
        }

        if (!args.isEmpty())
            setBase64ArgumentList(path, args.toArray());
        else
            override(path, null);
    }

    public void setArgumentList(String path, String... listArguments) {
        List<String> args = new ArrayList<>();

        for (String arg : listArguments) {
            args.add(arg);
        }

        if (configContains(path))
            override(path, args);
        else
            setDefault(path, args);
    }

    public void addArgumentsToList(String path, String... arguments) {
        List<String> args = getStringList(path);

        for (String arg : arguments) {
            args.add(arg);
        }

        setArgumentList(path, args.toArray(new String[args.size()]));
    }

    public void removeArgumentsFromList(String path, String... arguments) {
        List<String> args = getStringList(path);

        for (String arg : arguments) {
            if (args.contains(arg))
                args.remove(arg);
        }

        if (!args.isEmpty())
            setArgumentList(path, args.toArray(new String[args.size()]));
        else
            override(path, null);
    }


    /**
     * Checks if this {@link SimpleFile} contains a specific {@link String}
     *
     * @param toCheck {@link String} which might be inside this {@link SimpleFile}
     * @return whether or not this {@link SimpleFile} contains the {@link String}
     */
    public boolean configContains(String toCheck) {
        boolean cContains = false;
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(this.getKeys(true));
        for (String key : keys)
            if (key.equalsIgnoreCase(toCheck))
                cContains = true;

        return cContains;

    }

    /**
     * Save and load this {@link SimpleFile}
     */
    public void save() {
        try {
            if (source == null) return;
            save(source);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Add a new value to this {@link SimpleFile}
     *
     * @param path  where the value should be saved at
     * @param value the value which you want to save
     */
    public void setDefault(String path, Object value) {
        if (value instanceof String)
            value = ((String) value).replaceAll("§", "&");

        addDefault(path, value);
        save();
    }

    /**
     * Replaces a value inside this {@link SimpleFile}
     *
     * @param path  where the value is located at
     * @param value the new value which should be saved
     */
    public void override(String path, Object value) {
        if (value instanceof String)
            value = ((String) value).replaceAll("§", "&");

        set(path, value);
        save();
    }

    /**
     * Gets the {@link AlphaPlugin} which was used to create this {@link SimpleFile}
     *
     * @return the {@link AlphaPlugin}
     */
    public P getPluginInstance() {
        return pl;
    }
}

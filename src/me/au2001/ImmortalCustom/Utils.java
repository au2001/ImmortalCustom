package me.au2001.ImmortalCustom;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Utils {
	private Class<?> packetTitle;
	private Class<?> packetActions;
	private Class<?> nmsChatSerializer;
	private Class<?> chatBaseComponent;
	private Class<?> chatMessageType;
	private String title = "";
	private ChatColor titleColor = ChatColor.WHITE;
	private String subtitle = "";
	private ChatColor subtitleColor = ChatColor.WHITE;
	private int fadeInTime = -1;
	private int stayTime = -1;
	private int fadeOutTime = -1;
	private boolean ticks = true;

	private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<Class<?>, Class<?>>();

	/**
	 * Create a new 1.8 title
	 * 
	 * @param title
	 *            Title
	 */
	public Utils(String title) {
		this.title = title;
		loadClasses();
	}

	/**
	 * Create a new 1.8 title
	 * 
	 * @param title
	 *            Title text
	 * @param subtitle
	 *            Subtitle text
	 */
	public Utils(String title, String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
		loadClasses();
	}

	/**
	 * Copy 1.8 title
	 * 
	 * @param title
	 *            Title
	 */
	public Utils(Utils title) {
		if (title != null) {
			this.title = title.title;
			this.subtitle = title.subtitle;
			this.titleColor = title.titleColor;
			this.subtitleColor = title.subtitleColor;
			this.fadeInTime = title.fadeInTime;
			this.fadeOutTime = title.fadeOutTime;
			this.stayTime = title.stayTime;
			this.ticks = title.ticks;
		}
		loadClasses();
	}

	/**
	 * Create a new 1.8 title
	 * 
	 * @param title
	 *            Title text
	 * @param subtitle
	 *            Subtitle text
	 * @param fadeInTime
	 *            Fade in time
	 * @param stayTime
	 *            Stay on screen time
	 * @param fadeOutTime
	 *            Fade out time
	 */
	public Utils(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeInTime = fadeInTime;
		this.stayTime = stayTime;
		this.fadeOutTime = fadeOutTime;
		loadClasses();
	}

	/**
	 * Load spigot and NMS classes
	 */
	private void loadClasses() {
		packetTitle = getNMSClass("PacketPlayOutTitle");
		packetActions = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
		chatBaseComponent = getNMSClass("IChatBaseComponent");
		nmsChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
		if (nmsChatSerializer == null) nmsChatSerializer = getNMSClass("ChatSerializer");
		chatMessageType = getNMSClass("ChatMessageType");
	}

	/**
	 * Set title text
	 * 
	 * @param title
	 *            Title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get title text
	 * 
	 * @return Title text
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Set subtitle text
	 * 
	 * @param subtitle
	 *            Subtitle text
	 */
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	/**
	 * Get subtitle text
	 * 
	 * @return Subtitle text
	 */
	public String getSubtitle() {
		return this.subtitle;
	}

	/**
	 * Set the title color
	 * 
	 * @param color
	 *            Chat color
	 */
	public void setTitleColor(ChatColor color) {
		this.titleColor = color;
	}

	/**
	 * Set the subtitle color
	 * 
	 * @param color
	 *            Chat color
	 */
	public void setSubtitleColor(ChatColor color) {
		this.subtitleColor = color;
	}

	/**
	 * Set title fade in time
	 * 
	 * @param time
	 *            Time
	 */
	public void setFadeInTime(int time) {
		this.fadeInTime = time;
	}

	/**
	 * Set title fade out time
	 * 
	 * @param time
	 *            Time
	 */
	public void setFadeOutTime(int time) {
		this.fadeOutTime = time;
	}

	/**
	 * Set title stay time
	 * 
	 * @param time
	 *            Time
	 */
	public void setStayTime(int time) {
		this.stayTime = time;
	}

	/**
	 * Set timings to seconds
	 */
	public void setTimingsToSeconds() {
		ticks = false;
	}

	/**
	 * Set timings to ticks
	 */
	public void setTimingsToTicks() {
		ticks = true;
	}

	/**
	 * Send the title to a player
	 * 
	 * @param player
	 *            Player
	 */
	public void send(Player player) {
		if (packetTitle != null) {
			resetTitle(player);
			try {
				Object handle = getHandle(player);
				Object connection = getField(handle.getClass(), "playerConnection").get(handle);
				Object[] actions = packetActions.getEnumConstants();
				Method sendPacket = getMethod(connection.getClass(), "sendPacket");
				Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(actions[2], null, fadeInTime * (ticks ? 1 : 20), stayTime * (ticks ? 1 : 20), fadeOutTime * (ticks ? 1 : 20));
				if (fadeInTime != -1 && fadeOutTime != -1 && stayTime != -1) sendPacket.invoke(connection, packet);

				Object serialized = getMethod(nmsChatSerializer, "a", String.class).invoke(null, "{text:\"" + ChatColor.translateAlternateColorCodes('&', title) + "\",color:" + titleColor.name().toLowerCase() + "}");
				packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[0], serialized);
				sendPacket.invoke(connection, packet);
				if (subtitle != "") {
					serialized = getMethod(nmsChatSerializer, "a", String.class).invoke(null, "{text:\"" + ChatColor.translateAlternateColorCodes('&', subtitle) + "\",color:" + subtitleColor.name().toLowerCase() + "}");
					packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[1], serialized);
					sendPacket.invoke(connection, packet);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Broadcast the title to all players
	 */
	public void broadcast() {
		for (Player p : Bukkit.getOnlinePlayers()) send(p);
	}

	/**
	 * Clear the title
	 * 
	 * @param player
	 *            Player
	 */
	public void clearTitle(Player player) {
		try {
			Object handle = getHandle(player);
			Object connection = getField(handle.getClass(), "playerConnection").get(handle);
			Object[] actions = packetActions.getEnumConstants();
			Method sendPacket = getMethod(connection.getClass(), "sendPacket");
			Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[3], null);
			sendPacket.invoke(connection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reset the title settings
	 * 
	 * @param player
	 *            Player
	 */
	public void resetTitle(Player player) {
		try {
			Object handle = getHandle(player);
			Object connection = getField(handle.getClass(), "playerConnection").get(handle);
			Object[] actions = packetActions.getEnumConstants();
			Method sendPacket = getMethod(connection.getClass(), "sendPacket");
			Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[4], null);
			sendPacket.invoke(connection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Class<?> getPrimitiveType(Class<?> clazz) {
		return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES.get(clazz) : clazz;
	}

	private Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
		int a = classes != null ? classes.length : 0;
		Class<?>[] types = new Class<?>[a];
		for (int i = 0; i < a; i++)
			types[i] = getPrimitiveType(classes[i]);
		return types;
	}

	private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
		if (a.length != o.length) return false;
		for (int i = 0; i < a.length; i++)
			if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i]))
				return false;
		return true;
	}

	private Object getHandle(Object obj) {
		try {
			return getMethod("getHandle", obj.getClass()).invoke(obj);
		} catch (Exception e) {
			return null;
		}
	}

	private Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
		Class<?>[] t = toPrimitiveTypeArray(paramTypes);
		for (Method m : clazz.getMethods()) {
			Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
			if (m.getName().equals(name) && equalsTypeArray(types, t))
				return m;
		}
		return null;
	}

	private String getVersion() {
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String version = name.substring(name.lastIndexOf('.') + 1) + ".";
		return version;
	}

	private Class<?> getNMSClass(String className) {
		String fullName = "net.minecraft.server." + getVersion() + className;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(fullName);
		} catch (Exception e) {
			return null;
		}
		return clazz;
	}

	private Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			return null;
		}
	}

	private Method getMethod(Class<?> clazz, String name, Class<?>... args) {
		if (clazz != null) {
			for (Method m : clazz.getMethods()) {
				if (m.getName().equals(name) && (args.length == 0 || ClassListEqual(args, m.getParameterTypes()))) {
					m.setAccessible(true);
					return m;
				}
			}
		}
		return null;
	}

	private boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
		boolean equal = true;
		if (l1.length != l2.length) return false;
		for (int i = 0; i < l1.length; i++) {
			if (l1[i] != l2[i]) {
				equal = false;
				break;
			}
		}
		return equal;
	}
	
	public static void sendActionBar (Player player, String message) {
		Utils title = new Utils((Utils) null);
		try {
			Object handle = title.getHandle(player);
			Object connection = title.getField(handle.getClass(), "playerConnection").get(handle);
			Method sendPacket = title.getMethod(connection.getClass(), "sendPacket");
			Class<?> actionPacket = title.getNMSClass("PacketPlayOutChat");
			message = "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', message) + "\"}";
			Object serialized = title.getMethod(title.nmsChatSerializer, "a", String.class).invoke(null, message);
			Object packet;
			try {
				packet = actionPacket.getConstructor(title.chatBaseComponent, byte.class).newInstance(serialized, (byte) 2);
			} catch (Exception e) {
				Object actionBarType = title.chatMessageType.getDeclaredField("GAME_INFO").get(null);
				packet = actionPacket.getConstructor(title.chatBaseComponent, title.chatMessageType).newInstance(serialized, actionBarType);
			}
			sendPacket.invoke(connection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getMD5 (String path) {
		File file;
		boolean temp = false;
		
		try {
			URL url = new URL(path);
			file = File.createTempFile("ImmortalCustom", "AutoUpdater");
			FileUtils.copyURLToFile(url, file);
			temp = true;
		} catch (MalformedURLException e) {
			file = new File(path);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			String md5 = DigestUtils.md5Hex(new FileInputStream(file));
			if (temp) file.delete();
		    return md5;
		} catch (Exception e) {
			if (temp) file.delete();
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isActive (Location loc) {
		boolean enabled = !ImmortalCustom.plugin.getConfig().getStringList("disabledWorlds").contains(loc.getWorld().getName());
		if (ImmortalCustom.plugin.getConfig().isSet("areas." + loc.getWorld().getName())) {
			for (String select : ImmortalCustom.plugin.getConfig().getStringList("areas." + loc.getWorld().getName())) {
				if (select.matches("\\-?[0-9]+ [0-9]+ \\-?[0-9]+ & \\-?[0-9]+ [0-9]+ \\-?[0-9]+")) {
					String[] pos1 = select.split(" & ")[0].split(" ");
					String[] pos2 = select.split(" & ")[1].split(" ");
					if (loc.getX() >= Integer.parseInt(pos1[0]) && loc.getY() >= Integer.parseInt(pos1[1]) && loc.getZ() >= Integer.parseInt(pos1[2]))
						if (loc.getX() <= Integer.parseInt(pos2[0]) && loc.getY() <= Integer.parseInt(pos2[1]) && loc.getZ() <= Integer.parseInt(pos2[2]))
							enabled = !enabled;
				}
			}
		}
		return enabled;
	}

	public static String getEnchants (ItemStack is) {
		List<String> e = new ArrayList<String>();
		Map<Enchantment, Integer> en = is.getEnchantments();
		for (Enchantment t : en.keySet()) e.add(t.getName() + ":" + en.get(t));
		return e.isEmpty()? "null" : StringUtils.join(e, ",");
	}

	@SuppressWarnings("deprecation")
	public static String deserialize(ItemStack item) {
		String is = item.getType().name() + ";";
		is += item.getAmount() + ";";
		is += item.getDurability() + ";";
		is += item.getData().getData() + ";";
		String n = item.getItemMeta().getDisplayName();
		if (n == null) n = "null";
		else if (n.endsWith("\\")) n += " ";
		is += n.replace(";", "\\;") + ";";
		String l = StringUtils.join(item.getItemMeta().getLore(), "\n");
		if (l == null) l = "null";
		else if (l.endsWith("\\")) l += " ";
		is += l.replace(";", "\\;") + ";";
		is += getEnchants(item) + ";";
		return is;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack serialize(String is) {
		String[] a = is.split("(?<!\\\\);");
		ItemStack item = new ItemStack(Material.getMaterial(a[0]), Integer.parseInt(a[1]));
		item.setDurability((short) Integer.parseInt(a[2]));
		MaterialData d = item.getData();
		d.setData(Byte.parseByte(a[3]));
		item.setData(d);
		ItemMeta meta = item.getItemMeta(); 
		if (!a[4].equalsIgnoreCase("null")) meta.setDisplayName(a[4]);
		if (!a[5].equalsIgnoreCase("null")) meta.setLore(Arrays.asList(a[5].split("\n")));
		item.setItemMeta(meta);
		if (!a[6].equalsIgnoreCase("null")) {
			for (String s : a[6].split(",")) {
				String label = s.split(":")[0];
				String amplifier = s.split(":")[1];
				Enchantment type = Enchantment.getByName(label);
				if (type == null) continue;
				try {
					item.addEnchantment(type, Integer.parseInt(amplifier));
				} catch(Exception ex) {}
			}
		}
		return item;
	}

	public static boolean isClickable (Block type) {
		if (type == null || type.getType().equals(Material.AIR)) return false;
		switch (type.getType()) {
		case DISPENSER:
		case NOTE_BLOCK:
		case BED_BLOCK:
		case WORKBENCH:
		case FURNACE:
		case BURNING_FURNACE:
		case WOODEN_DOOR:
		case LEVER:
		case IRON_DOOR_BLOCK:
		case STONE_BUTTON:
		case JUKEBOX:
		case CAKE_BLOCK:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case TRAP_DOOR:
		case FENCE_GATE:
		case ENCHANTMENT_TABLE:
		case BREWING_STAND:
		case DRAGON_EGG:
		case ENDER_CHEST:
		case COMMAND:
		case BEACON:
		case WOOD_BUTTON:
		case ANVIL:
		case TRAPPED_CHEST:
		case REDSTONE_COMPARATOR_OFF:
		case REDSTONE_COMPARATOR_ON:
		case HOPPER:
		case DROPPER:
		case SIGN:
		case SIGN_POST:
			return true;
		default:
			return false;
		}
	}
	
}
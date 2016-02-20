package me.au2001.ImmortalCustom;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import com.google.gson.Gson;

public class Backpacks implements Listener {

	public static Connection mysql = null;
	public static HashMap<UUID, Integer> opens = new HashMap<UUID, Integer>();

	public Backpacks () {
		sqlite();
	}

	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event) {
		event.getPlayer().closeInventory();
	}

	@EventHandler
	public void onPlayerKick (PlayerKickEvent event) {
		event.getPlayer().closeInventory();
	}

	@EventHandler
	public void onInventoryClose (InventoryCloseEvent event) {
		UUID user = event.getPlayer().getUniqueId();
		if (opens.containsKey(user)) save(user, event.getInventory(), opens.remove(user));
	}

	public static ItemStack giveItem (UUID user, ItemStack item) {
		for (Integer id : list(user, null)) {
			Inventory inv = get(user, id);
			HashMap<Integer, ItemStack> hmp = inv.addItem(item);
			save(user, inv, id);
			if (hmp.isEmpty()) return null;
			ItemStack rest = hmp.get(0);
			if (rest.isSimilar(item)) item.setAmount(rest.getAmount());
			else item = rest;
		}
		return item;
	}

	public static Inventory get (UUID user, int id) {		
		Inventory inv = null;
		Statement state = null;
		try {
			state = mysql.createStatement();
			ResultSet set = state.executeQuery("SELECT * FROM backpacks WHERE user='" + user + "' AND backpack='" + id + "'");
			if (set.next()) {
				ConfigurationSection tier = ImmortalCustom.plugin.getConfig().getConfigurationSection("backpacks." + set.getString("tier"));
				String title = ChatColor.translateAlternateColorCodes('&', tier.getString("title"));
				inv = Bukkit.createInventory(null, tier.getInt("size"), title.replace("{ID}", "" + id));
				HashMap<?, ?> items = new Gson().fromJson(set.getString("items"), HashMap.class);
				for (Object item : items.keySet())
					inv.setItem(Integer.parseInt((String) item), Utils.serialize((String) items.get(item)));
			}
			state.close();
		} catch (Exception e) {
			try {
				if (state != null) state.close();
			} catch (SQLException ee) {
				ee.printStackTrace();
			}
			e.printStackTrace();
			sqlite();
			if (mysql != null) return get(user, id);
			else if (Bukkit.getPlayer(user) != null && Bukkit.getPlayer(user).isOnline())
				Bukkit.getPlayer(user).sendMessage(ChatColor.RED + "Unfortunately an error occured. Contact an administrator.");
		}
		return inv;
	}

	public static void save (UUID user, Inventory inv, int id) {
		HashMap<Integer, String> items = new HashMap<Integer, String>();
		for (int i = 0; i < inv.getSize(); i++) if (inv.getItem(i) != null) items.put(i, Utils.deserialize(inv.getItem(i)));
		String json = StringEscapeUtils.escapeSql(JSONObject.toJSONString(items));
		Statement state = null;
		try {
			state = mysql.createStatement();
			state.execute("UPDATE backpacks SET items='" + json + "' WHERE user='" + user + "' AND backpack='" + id + "'");
			state.close();
		} catch (Exception e) {
			try {
				if (state != null) state.close();
			} catch (SQLException ee) {
				ee.printStackTrace();
			}
			e.printStackTrace();
			sqlite();
			if (mysql != null) save(user, inv, id);
			else if (Bukkit.getPlayer(user) != null && Bukkit.getPlayer(user).isOnline())
				Bukkit.getPlayer(user).sendMessage(ChatColor.RED + "Unfortunately an error occured. Contact an administrator.");
		}
	}

	public static List<Integer> list (UUID user, String tier) {
		List<Integer> bps = new ArrayList<Integer>();
		Statement state = null;
		try {
			state = mysql.createStatement();
			ResultSet set = state.executeQuery("SELECT backpack FROM backpacks WHERE user='" + user + "'" + (tier != null? " AND tier='" + tier + "'" : ""));
			while (set.next()) bps.add(set.getInt("backpack"));
			state.close();
		} catch (Exception e) {
			try {
				if (state != null) state.close();
			} catch (SQLException ee) {
				ee.printStackTrace();
			}
			e.printStackTrace();
			sqlite();
			if (mysql != null) return list(user, tier);
			else if (Bukkit.getPlayer(user) != null && Bukkit.getPlayer(user).isOnline())
				Bukkit.getPlayer(user).sendMessage(ChatColor.RED + "Unfortunately an error occured. Contact an administrator.");
		}
		return bps;
	}

	public static void give (UUID user, String tier) {
		Statement state = null;
		try {
			state = mysql.createStatement();
			ResultSet set;
			int id = 0;
			do set = state.executeQuery("SELECT backpack FROM backpacks WHERE user='" + user + "' AND backpack='" + ++id + "'");
			while (set.next());
			String json = new JSONObject().toJSONString();
			state.execute("INSERT INTO backpacks VALUES (NULL, '" + user + "', '" + id + "', '" + json + "', '" + tier + "')");
			state.close();
		} catch (Exception e) {
			try {
				if (state != null) state.close();
			} catch (SQLException ee) {
				ee.printStackTrace();
			}
			e.printStackTrace();
			sqlite();
			if (mysql != null) give(user, tier);
			else if (Bukkit.getPlayer(user) != null && Bukkit.getPlayer(user).isOnline())
				Bukkit.getPlayer(user).sendMessage(ChatColor.RED + "Unfortunately an error occured. Contact an administrator.");
		}
	}

	public static void take (UUID user, int id) {
		Statement state = null;
		try {
			state = mysql.createStatement();
			state.execute("DELETE FROM backpacks WHERE user='" + user + "' AND backpack='" + id + "'");
			state.close();
		} catch (Exception e) {
			try {
				if (state != null) state.close();
			} catch (SQLException ee) {
				ee.printStackTrace();
			}
			e.printStackTrace();
			sqlite();
			if (mysql != null) take(user, id);
			else if (Bukkit.getPlayer(user) != null && Bukkit.getPlayer(user).isOnline())
				Bukkit.getPlayer(user).sendMessage(ChatColor.RED + "Unfortunately an error occured. Contact an administrator.");
		}
	}

	public static void sqlite () {
		Statement state = null;
		try {
			Class.forName("org.sqlite.JDBC");
			File sqlite = new File(ImmortalCustom.plugin.getDataFolder(), "backpacks.db");
			mysql = DriverManager.getConnection("jdbc:sqlite:" + sqlite.getAbsolutePath());

			state = mysql.createStatement();
			state.execute("CREATE TABLE IF NOT EXISTS backpacks (id INTEGER PRIMARY KEY AUTOINCREMENT, user TEXT, backpack INTEGER, items TEXT, tier TEXT)");
			state.close();
		} catch (Exception e) {
			try {
				if (state != null) state.close();
			} catch (SQLException ee) {
				ee.printStackTrace();
			}
			mysql = null;
			e.printStackTrace();
			return;
		}
	}

}

package me.au2001.ImmortalCustom;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoUpdate {

	public static void start () {
		if (ImmortalCustom.plugin.getConfig().getString("autoupdater.path").isEmpty()) {
			String path = ImmortalCustom.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			ImmortalCustom.plugin.getConfig().set("autoupdater.path", path);
			ImmortalCustom.plugin.saveConfig();
		}

		if (ImmortalCustom.plugin.getConfig().getBoolean("autoupdater.enabled", true)) {
			if (ImmortalCustom.plugin.getConfig().getLong("autoupdater.delay") > -1) {
				new BukkitRunnable () {
					public void run() {
						if (ImmortalCustom.plugin.isEnabled()) {
							try {
								if (check()) {
									if (ImmortalCustom.plugin.getConfig().getBoolean("autoupdater.download", false)) {
										broadcast("&6[&eImmortalCustom&6] &eAn update was found, downloading from URL...");
										download();
										broadcast("&6[&eImmortalCustom&6] &eDone! Please restart or reload for changes to take effect.");
									} else {
										broadcast("&6[&eImmortalCustom&6] &eAn update was found, please download it here:");
										broadcast("&6&l> &e" + ImmortalCustom.plugin.getConfig().getString("autoupdater.url"));
										broadcast("&6&l> &eOr use /fortune update to download it in-game.");
									}
								}
							} catch (NullPointerException e) {
								broadcast("&6[&eImmortalCustom&6] &cAn error occured while comparing versions.");
								e.printStackTrace();
							}
						} else cancel();
					}
				}.runTaskTimerAsynchronously(ImmortalCustom.plugin, 0, ImmortalCustom.plugin.getConfig().getLong("autoupdater.delay"));
			} else {
				new BukkitRunnable() {
					public void run() {
						try {
							if (check()) {
								if (ImmortalCustom.plugin.getConfig().getBoolean("autoupdater.download", false)) {
									broadcast("&6[&eImmortalCustom&6] &eAn update was found, downloading from URL...");
									download();
									broadcast("&6[&eImmortalCustom&6] &eDone! Please restart or reload for changes to take effect.");
								} else {
									broadcast("&6[&eImmortalCustom&6] &eAn update was found, please download it here:");
									broadcast("&6&l> &e" + ImmortalCustom.plugin.getConfig().getString("autoupdater.url"));
									broadcast("&6&l> &eOr use /fortune update to download it in-game.");
								}

							}
						} catch (NullPointerException e) {
							broadcast("&6[&eImmortalCustom&6] &cAn error occured while comparing versions.");
							e.printStackTrace();
						}
					}
				}.runTaskAsynchronously(ImmortalCustom.plugin);
			}
		}
	}

	public static void broadcast (String message) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		ImmortalCustom.plugin.getLogger().info(ChatColor.stripColor(message).replace("[ImmortalCustom] ", ""));
		if (ImmortalCustom.plugin.getConfig().getBoolean("autoupdater.admins", true)) for (Player p : Bukkit.getOnlinePlayers())
			if (p.hasPermission("ImmortalCustom.autoupdater") || p.hasPermission("ic.autoupdater")) p.sendMessage(message);
	}

	public static void download () {
		try {
			URL website = new URL(ImmortalCustom.plugin.getConfig().getString("autoupdater.url"));
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(ImmortalCustom.plugin.getConfig().getString("autoupdater.path"));
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean check () {
		String newest = Utils.getMD5(ImmortalCustom.plugin.getConfig().getString("autoupdater.url"));
		String current = Utils.getMD5(ImmortalCustom.plugin.getConfig().getString("autoupdater.path"));
		if (current == null) throw new NullPointerException("Current JAR path doesn't seem to exist or isn't valid");
		if (newest == null) throw new NullPointerException("Update URL doesn't seem to exist or isn't valid");
		return !current.equals(newest);
	}

}
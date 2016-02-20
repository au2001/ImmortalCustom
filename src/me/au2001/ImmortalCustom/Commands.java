package me.au2001.ImmortalCustom;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class Commands implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 0) {
			if (sender.hasPermission("ImmortalCustom.version") || sender.hasPermission("ic.version")) {
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.STRIKETHROUGH + "=============" + ChatColor.GOLD + "[ " + ChatColor.DARK_AQUA + "ImmortalCustom" + ChatColor.GOLD + " ]" + ChatColor.LIGHT_PURPLE + ChatColor.STRIKETHROUGH + "=============");
				sender.sendMessage(ChatColor.GOLD + "Plugin: " + ChatColor.YELLOW + "ImmortalCustom");
				sender.sendMessage(ChatColor.GOLD + "Author: " + ChatColor.YELLOW + "au2001");
				sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.YELLOW + ImmortalCustom.plugin.getDescription().getVersion());
				sender.sendMessage(ChatColor.GOLD + "Commands: " + ChatColor.YELLOW + "/ic help");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.STRIKETHROUGH + "=========================================");
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("enchant")) {
			if (sender instanceof Player) {
				if (sender.hasPermission("ImmortalCustom.enchant") || sender.hasPermission("ic.enchant")) {
					Player player = (Player)sender;
					ItemStack item = player.getItemInHand();
					try {
						int level = Integer.parseInt(args[1]);
						item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, level);
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "Second argument must be a number!");
						return false;
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "Error while enchanting this item!");
						return false;
					}
					sender.sendMessage(ChatColor.AQUA + "Successfully enchanted!");
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You have to be a player to execute this command!");
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("ImmortalCustom.reload") || sender.hasPermission("ic.reload")) {
				ImmortalCustom.plugin.reloadConfig();
				sender.sendMessage(ChatColor.AQUA + "Successfully reloaded the configuration!");
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args[0].equalsIgnoreCase("help")) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "[" + ChatColor.STRIKETHROUGH + "==================" + ChatColor.LIGHT_PURPLE + "]" + ChatColor.BLUE + " ImmortalCustom " + ChatColor.LIGHT_PURPLE + "[" + ChatColor.STRIKETHROUGH + "==================" + ChatColor.LIGHT_PURPLE + "]");
			for (String command : ImmortalCustom.plugin.getDescription().getCommands().keySet()) {
				if (ImmortalCustom.plugin.getCommand(command).getPermission() == null || sender.hasPermission(ImmortalCustom.plugin.getCommand(command).getPermission())) {
					sender.sendMessage(ChatColor.DARK_AQUA + "/" + command + " " + ChatColor.BLUE + ImmortalCustom.plugin.getCommand(command).getDescription());
					if (ImmortalCustom.plugin.getCommand(command).getPermission() != null) {
						sender.sendMessage(ChatColor.GOLD + "    Permission: " + ChatColor.YELLOW + ImmortalCustom.plugin.getCommand(command).getPermission());
					}
				}
			}
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "[" + ChatColor.STRIKETHROUGH + "===================================================" + ChatColor.LIGHT_PURPLE + "]");
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("fortunelist")) {
			if (sender instanceof Player) {
				if (args[1].equalsIgnoreCase("add")) {
					if (sender.hasPermission("ImmortalCustom.fortunelist.add") || sender.hasPermission("ic.fortunelist.add")) {
						if (!((Player) sender).getItemInHand().getType().equals(Material.AIR)) {
							if (!ImmortalCustom.plugin.getConfig().getStringList("fortuneBlocks").contains(((Player) sender).getItemInHand().getType().toString())) {
								List<String> list = ImmortalCustom.plugin.getConfig().getStringList("fortuneBlocks");
								list.add(((Player) sender).getItemInHand().getType().toString());
								ImmortalCustom.plugin.getConfig().set("fortuneBlocks", list);
								ImmortalCustom.plugin.saveConfig();
								sender.sendMessage(ChatColor.GREEN + "Successfully added " + ((Player) sender).getItemInHand().getType().toString() + " to the fortunelist.");
							} else {
								sender.sendMessage(ChatColor.RED + "This block is already in the fortunelist.");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args[1].equalsIgnoreCase("remove")) {
					if (sender.hasPermission("ImmortalCustom.fortunelist.remove") || sender.hasPermission("ic.fortunelist.remove")) {
						if (!((Player) sender).getItemInHand().getType().equals(Material.AIR)) {
							if (ImmortalCustom.plugin.getConfig().getStringList("fortuneBlocks").contains(((Player) sender).getItemInHand().getType().toString())) {
								List<String> list = ImmortalCustom.plugin.getConfig().getStringList("fortuneBlocks");
								list.remove(((Player) sender).getItemInHand().getType().toString());
								ImmortalCustom.plugin.getConfig().set("fortuneBlocks", list);
								ImmortalCustom.plugin.saveConfig();
								sender.sendMessage(ChatColor.GREEN + "Successfully removed " + ((Player) sender).getItemInHand().getType().toString() + " from the fortunelist.");
							} else {
								sender.sendMessage(ChatColor.RED + "This block is not in the fortunelist.");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args[1].equalsIgnoreCase("on")) {
					if (sender.hasPermission("ImmortalCustom.fortunelist.on") || sender.hasPermission("ic.fortunelist.on")) {
						if (!ImmortalCustom.plugin.getConfig().getBoolean("fortunelist")) {
							ImmortalCustom.plugin.getConfig().set("fortunelist", true);
							ImmortalCustom.plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Successfully turned on the fortunelist.");
						} else {
							sender.sendMessage(ChatColor.RED + "The fortunelist is already turned on.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args[1].equalsIgnoreCase("off")) {
					if (sender.hasPermission("ImmortalCustom.fortunelist.off") || sender.hasPermission("ic.fortunelist.off")) {
						if (ImmortalCustom.plugin.getConfig().getBoolean("fortunelist")) {
							ImmortalCustom.plugin.getConfig().set("fortunelist", false);
							ImmortalCustom.plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Successfully turned off the fortunelist.");
						} else {
							sender.sendMessage(ChatColor.RED + "The fortunelist is already turned off.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use add, remove, on or off.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You have to be a player to execute this command!");
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("pickuplist")) {
			if (sender instanceof Player) {
				if (args[1].equalsIgnoreCase("add")) {
					if (sender.hasPermission("ImmortalCustom.pickuplist.add") || sender.hasPermission("ic.pickuplist.add")) {
						if (!((Player) sender).getItemInHand().getType().equals(Material.AIR)) {
							if (!ImmortalCustom.plugin.getConfig().getStringList("pickupBlocks").contains(((Player) sender).getItemInHand().getType().toString())) {
								List<String> list = ImmortalCustom.plugin.getConfig().getStringList("pickupBlocks");
								list.add(((Player) sender).getItemInHand().getType().toString());
								ImmortalCustom.plugin.getConfig().set("pickupBlocks", list);
								ImmortalCustom.plugin.saveConfig();
								sender.sendMessage(ChatColor.GREEN + "Successfully added " + ((Player) sender).getItemInHand().getType().toString() + " to the pickuplist.");
							} else {
								sender.sendMessage(ChatColor.RED + "This block is already in the pickuplist.");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args[1].equalsIgnoreCase("remove")) {
					if (sender.hasPermission("ImmortalCustom.pickuplist.remove") || sender.hasPermission("ic.pickuplist.remove")) {
						if (!((Player) sender).getItemInHand().getType().equals(Material.AIR)) {
							if (ImmortalCustom.plugin.getConfig().getStringList("pickupBlocks").contains(((Player) sender).getItemInHand().getType().toString())) {
								List<String> list = ImmortalCustom.plugin.getConfig().getStringList("pickupBlocks");
								list.remove(((Player) sender).getItemInHand().getType().toString());
								ImmortalCustom.plugin.getConfig().set("pickupBlocks", list);
								ImmortalCustom.plugin.saveConfig();
								sender.sendMessage(ChatColor.GREEN + "Successfully removed " + ((Player) sender).getItemInHand().getType().toString() + " from the pickuplist.");
							} else {
								sender.sendMessage(ChatColor.RED + "This block is not in the pickuplist.");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args[1].equalsIgnoreCase("on")) {
					if (sender.hasPermission("ImmortalCustom.pickuplist.on") || sender.hasPermission("ic.pickuplist.on")) {
						if (!ImmortalCustom.plugin.getConfig().getBoolean("pickuplist")) {
							ImmortalCustom.plugin.getConfig().set("pickuplist", true);
							ImmortalCustom.plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Successfully turned on the pickuplist.");
						} else {
							sender.sendMessage(ChatColor.RED + "The pickuplist is already turned on.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args[1].equalsIgnoreCase("off")) {
					if (sender.hasPermission("ImmortalCustom.pickuplist.off") || sender.hasPermission("ic.pickuplist.off")) {
						if (ImmortalCustom.plugin.getConfig().getBoolean("pickuplist")) {
							ImmortalCustom.plugin.getConfig().set("pickuplist", false);
							ImmortalCustom.plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Successfully turned off the pickuplist.");
						} else {
							sender.sendMessage(ChatColor.RED + "The pickuplist is already turned off.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use add, remove, on or off.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You have to be a player to execute this command!");
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("worlds")) {
			if (sender instanceof Player) {
				if (args[1].equalsIgnoreCase("add")) {
					if (sender.hasPermission("ImmortalCustom.worlds.add") || sender.hasPermission("ic.worlds.add")) {
						if (!ImmortalCustom.plugin.getConfig().getStringList("disabledWorlds").contains(((Player) sender).getWorld().getName())) {
							List<String> list = ImmortalCustom.plugin.getConfig().getStringList("disabledWorlds");
							list.add(((Player) sender).getWorld().getName());
							ImmortalCustom.plugin.getConfig().set("disabledWorlds", list);
							ImmortalCustom.plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Successfully added " + ((Player) sender).getWorld().getName() + " to the disabled worlds.");
						} else {
							sender.sendMessage(ChatColor.RED + "This world is already in the disabled worlds.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args[1].equalsIgnoreCase("remove")) {
					if (sender.hasPermission("ImmortalCustom.worlds.remove") || sender.hasPermission("ic.worlds.remove")) {
						if (ImmortalCustom.plugin.getConfig().getStringList("disabledWorlds").contains(((Player) sender).getWorld().getName())) {
							List<String> list = ImmortalCustom.plugin.getConfig().getStringList("disabledWorlds");
							list.remove(((Player) sender).getWorld().getName());
							ImmortalCustom.plugin.getConfig().set("disabledWorlds", list);
							ImmortalCustom.plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Successfully removed " + ((Player) sender).getWorld().getName() + " from the disabled worlds.");
						} else {
							sender.sendMessage(ChatColor.RED + "This world is not in the disabled worlds.");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use add or remove.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You have to be a player to execute this command!");
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("multiplier")) {
			if (sender.hasPermission("ImmortalCustom.multiplier") || sender.hasPermission("ic.multiplier")) {
				try {
					ImmortalCustom.plugin.getConfig().set("multiplier", Double.parseDouble(args[1]));
					ImmortalCustom.plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully set the multiplier to " + Double.parseDouble(args[1]));
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Second argument must be a number!");
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("difference")) {
			if (sender.hasPermission("ImmortalCustom.multiplier") || sender.hasPermission("ic.multiplier")) {
				try {
					ImmortalCustom.plugin.getConfig().set("difference", Integer.parseInt(args[1]));
					ImmortalCustom.plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully set the difference to " + Integer.parseInt(args[1]));
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Second argument must be an integer!");
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("autopickup")) {
			if (args[1].equalsIgnoreCase("on")) {
				if (sender.hasPermission("ImmortalCustom.autopickup.on") || sender.hasPermission("ic.autopickup.on")) {
					if (!ImmortalCustom.plugin.getConfig().getBoolean("autopickup")) {
						ImmortalCustom.plugin.getConfig().set("autopickup", true);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned on the autopickup.");
					} else {
						sender.sendMessage(ChatColor.RED + "The autopickup is already turned on.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else if (args[1].equalsIgnoreCase("off")) {
				if (sender.hasPermission("ImmortalCustom.autopickup.off") || sender.hasPermission("ic.autopickup.off")) {
					if (ImmortalCustom.plugin.getConfig().getBoolean("autopickup")) {
						ImmortalCustom.plugin.getConfig().set("autopickup", false);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned off the autopickup.");
					} else {
						sender.sendMessage(ChatColor.RED + "The autopickup is already turned off.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use on or off.");
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("flames")) {
			if (args[1].equalsIgnoreCase("on")) {
				if (sender.hasPermission("ImmortalCustom.flames.on") || sender.hasPermission("ic.flames.on")) {
					if (!ImmortalCustom.plugin.getConfig().getBoolean("flames")) {
						ImmortalCustom.plugin.getConfig().set("flames", true);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned on the flames when smelting.");
					} else {
						sender.sendMessage(ChatColor.RED + "The flames are already turned on.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else if (args[1].equalsIgnoreCase("off")) {
				if (sender.hasPermission("ImmortalCustom.flames.off") || sender.hasPermission("ic.flames.off")) {
					if (ImmortalCustom.plugin.getConfig().getBoolean("flames")) {
						ImmortalCustom.plugin.getConfig().set("flames", false);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned off the flames when smelting.");
					} else {
						sender.sendMessage(ChatColor.RED + "The flames are already turned off.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use on or off.");
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("dropdown")) {
			if (args[1].equalsIgnoreCase("on")) {
				if (sender.hasPermission("ImmortalCustom.dropdown.on") || sender.hasPermission("ic.dropdown.on")) {
					if (!ImmortalCustom.plugin.getConfig().getBoolean("dropdown")) {
						ImmortalCustom.plugin.getConfig().set("dropdown", true);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned on the drop downs.");
					} else {
						sender.sendMessage(ChatColor.RED + "The drop downs are already turned on.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else if (args[1].equalsIgnoreCase("off")) {
				if (sender.hasPermission("ImmortalCustom.dropdown.off") || sender.hasPermission("ic.dropdown.off")) {
					if (ImmortalCustom.plugin.getConfig().getBoolean("dropdown")) {
						ImmortalCustom.plugin.getConfig().set("dropdown", false);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned off the drop downs.");
					} else {
						sender.sendMessage(ChatColor.RED + "The drop downs are already turned off.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use on or off.");
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("smeltFortune")) {
			if (sender.hasPermission("ImmortalCustom.smeltFortune") || sender.hasPermission("ic.smeltFortune")) {
				try {
					ImmortalCustom.plugin.getConfig().set("smeltFortune", Integer.parseInt(args[1]));
					ImmortalCustom.plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully set the minimum fortune for autosmelt to " + Integer.parseInt(args[1]));
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Second argument must be an integer!");
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args[0].equalsIgnoreCase("fullmessage")) {
			if (sender.hasPermission("ImmortalCustom.fullmessage") || sender.hasPermission("ic.fullmessage")) {
				if (args.length > 1) {
					String message = "";
					for (String word : args) message += " " + word;
					ImmortalCustom.plugin.getConfig().set("fullmessage", message.substring(1));
					ImmortalCustom.plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully set the fullmessage.");
				} else {
					ImmortalCustom.plugin.getConfig().set("fullmessage", "");
					ImmortalCustom.plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully removed the fullmessage.");
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("orestoblocks")) {
			if (args[1].equalsIgnoreCase("on")) {
				if (sender.hasPermission("ImmortalCustom.orestoblocks.on") || sender.hasPermission("ic.orestoblocks.on")) {
					if (!ImmortalCustom.plugin.getConfig().getBoolean("orestoblocks")) {
						ImmortalCustom.plugin.getConfig().set("orestoblocks", true);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned on the orestoblocks.");
					} else {
						sender.sendMessage(ChatColor.RED + "The orestoblocks is already turned on.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else if (args[1].equalsIgnoreCase("off")) {
				if (sender.hasPermission("ImmortalCustom.orestoblocks.off") || sender.hasPermission("ic.orestoblocks.off")) {
					if (ImmortalCustom.plugin.getConfig().getBoolean("orestoblocks")) {
						ImmortalCustom.plugin.getConfig().set("orestoblocks", false);
						ImmortalCustom.plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully turned off the orestoblocks.");
					} else {
						sender.sendMessage(ChatColor.RED + "The orestoblocks is already turned off.");
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use on or off.");
			}
		} else if (args[0].equalsIgnoreCase("override")) {
			if (sender.hasPermission("ImmortalCustom.override") || sender.hasPermission("ic.override")) {
				if (sender instanceof Player) {
					if (ImmortalCustom.WE != null) {
						Selection s = ((WorldEditPlugin) ImmortalCustom.WE).getSelection((Player) sender);
						if (s != null) {
							String select = s.getMinimumPoint().getBlockX() + " " + s.getMinimumPoint().getBlockY() + " " + s.getMinimumPoint().getBlockZ();
							select += " & " + s.getMaximumPoint().getBlockX() + " " + s.getMaximumPoint().getBlockY() + " " + s.getMaximumPoint().getBlockZ();
							List<String> list = ImmortalCustom.plugin.getConfig().getStringList("areas");
							if (!list.remove(select)) list.add(select);
							ImmortalCustom.plugin.getConfig().set("areas." + s.getWorld().getName(), list);
							ImmortalCustom.plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "ImmortalCustom is now toggled in this area!");
						} else {
							sender.sendMessage(ChatColor.RED + "Please select a region first.");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You must have WorldEdit installed!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You have to be a player to execute this command!");
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args[0].equalsIgnoreCase("update")) {
			if (sender.hasPermission("ImmortalCustom.update") || sender.hasPermission("ic.update")) {
				final CommandSender msg = sender;
				new BukkitRunnable() {
					public void run() {
						try {
							if (AutoUpdate.check()) {
								msg.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&eImmortalCustom&6] &eAn update was found, downloading..."));
								AutoUpdate.download();
								msg.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&eImmortalCustom&6] &eDone! Please restart or reload."));
							} else {
								msg.sendMessage(ChatColor.RED + "No update is available, you are running the latest release.");
							}
						} catch (NullPointerException e) {
							msg.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&eImmortalCustom&6] &cAn error occured while comparing versions."));
						}
					}
				}.runTaskAsynchronously(ImmortalCustom.plugin);
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args[0].equalsIgnoreCase("minebow")) {
			if (sender.hasPermission("ImmortalCustom.minebow") || sender.hasPermission("ic.minebow")) {
				if (args.length > 1) {
					Player player = Bukkit.getPlayer(args[1]);
					if (player != null) {
						int amount = 1;
						if (args.length > 2) {
							try {
								amount = Integer.parseInt(args[2]);
							} catch (NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "\"" + args[2] + "\" is not a valid number.");
								return false;
							}
						}
						Minebow.bow.setAmount(amount);
						player.getInventory().addItem(Minebow.bow);
						player.updateInventory();
						Minebow.updateBows(player, 0);
						player.sendMessage(Minebow.prefix + ChatColor.GREEN + "You received " + (amount == 1? "a" : amount) + " minebow" + (amount == 1? "" : "s") + "!");
						if (sender != player) sender.sendMessage(ChatColor.GREEN + "This player received " + (amount == 1? "a" : amount) + " minebow" + (amount == 1? "" : "s") + ".");
					} else {
						sender.sendMessage(ChatColor.RED + "This player doesn't exist or isn't online.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Please specify a player.");
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args[0].equalsIgnoreCase("minearrow")) {
			if (sender.hasPermission("ImmortalCustom.minearrow") || sender.hasPermission("ic.minearrow")) {
				if (args.length > 1) {
					Player player = Bukkit.getPlayer(args[1]);
					if (player != null) {
						int amount = 1;
						if (args.length > 2) {
							try {
								amount = Integer.parseInt(args[2]);
							} catch (NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "\"" + args[2] + "\" is not a valid number.");
								return false;
							}
						}
						Minebow.arrow.setAmount(amount);
						player.getInventory().addItem(Minebow.arrow);
						player.updateInventory();
						Minebow.updateBows(player, 0);
						player.sendMessage(Minebow.prefix + ChatColor.GREEN + "You received " + (amount == 1? "a" : amount) + " minearrow" + (amount == 1? "" : "s") + "!");
						if (sender != player) sender.sendMessage(ChatColor.GREEN + "This player received " + (amount == 1? "a" : amount) + " minebow" + (amount == 1? "" : "s") + ".");
					} else {
						sender.sendMessage(ChatColor.RED + "This player doesn't exist or isn't online.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Please specify a player.");
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
			}
		} else if (args[0].equalsIgnoreCase("backpack")) {
			if (Backpacks.mysql != null) {
				if (args.length >= 2 && args[1].equalsIgnoreCase("give")) {
					if (sender.hasPermission("Backpacks.give")) {
						if (args.length >= 4) {
							Player player = Bukkit.getPlayer(args[2]);
							if (player != null && player.getUniqueId() != null) {
								args[3] = args[3].toLowerCase();
								if (ImmortalCustom.plugin.getConfig().isConfigurationSection("backpacks." + args[3])) {
									Backpacks.give(player.getUniqueId(), args[3]);
									int amount = Backpacks.list(player.getUniqueId(), args[3]).size();
									String message = ChatColor.GREEN + "This player has now " + ChatColor.DARK_GREEN + "{AMOUNT} " + ChatColor.GREEN + "backpacks of this tier.";
									sender.sendMessage(message.replace("{AMOUNT}", "" + amount));
								} else {
									sender.sendMessage(ChatColor.RED + "This backpack tier couldn't be found!");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "This player couldn't be found!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Usage: /bp give <user> <tier>");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (args.length >= 2 && args[1].equalsIgnoreCase("take")) {
					if (sender.hasPermission("Backpacks.take")) {
						if (args.length >= 4) {
							Player player = Bukkit.getPlayer(args[2]);
							if (player != null && player.getUniqueId() != null) {
								try {
									Backpacks.take(player.getUniqueId(), Integer.parseInt(args[3]));
									String list = StringUtils.join(Backpacks.list(player.getUniqueId(), args[3]), ",");
									int amount = list == null? 0 : list.split(",").length;
									String message = ChatColor.GREEN + "You removed backpack " + ChatColor.DARK_GREEN + "#{ID} " + ChatColor.GREEN + "of this player.";
									sender.sendMessage(message.replace("{AMOUNT}", "" + amount));
								} catch (NumberFormatException e) {
									sender.sendMessage(ChatColor.RED + "This is not a valid backpack id!");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "This player couldn't be found!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Usage: /bp give <user> <tier>");
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ImmortalCustom.plugin.getConfig().getString("permission")));
					}
				} else if (sender instanceof Player) {
					if (args.length == 1) {
						sender.sendMessage(ChatColor.GREEN + "List of your backpacks per tier:");
						int total = 0;
						String format = ChatColor.GREEN + "Tier " + ChatColor.DARK_GREEN + "{TIER}" + ChatColor.GREEN + ": " + ChatColor.DARK_GREEN + "{LIST}";
						for (String tier : ImmortalCustom.plugin.getConfig().getConfigurationSection("backpacks").getKeys(false)) {
							String list = StringUtils.join(Backpacks.list(((Player) sender).getUniqueId(), tier), ",");
							if (list != null) {
								sender.sendMessage(format.replace("{TIER}", tier).replace("{LIST}", list));
								total += list.split(",").length;
							} 
						}
						String message = ChatColor.GREEN + "You have a total of " + ChatColor.DARK_GREEN + "{TOTAL} " + ChatColor.GREEN + "backpacks.";
						if (total == 0) sender.sendMessage(ChatColor.RED + "You don't have any backpack.");
						else sender.sendMessage(message.replace("{TOTAL}", "" + total));
					} else {
						try {
							int id = Integer.parseInt(args[1]);
							Inventory inv = Backpacks.get(((Player) sender).getUniqueId(), id);
							if (inv != null) {
								Backpacks.opens.put(((Player) sender).getUniqueId(), id);
								String message = ChatColor.GREEN + "Opening backpack " + ChatColor.DARK_GREEN + "#{ID}" + ChatColor.GREEN + ".";
								sender.sendMessage(message.replace("{ID}", "" + id));
								((Player) sender).openInventory(inv);
							} else {
								sender.sendMessage(ChatColor.RED + "This is not a valid backpack id!");
							}
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "This is not a valid backpack id!");
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You must be a player to use that!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Unfortunately an error occured. Contact an administrator.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid command parameters! Use /help ImmortalCustom");
		}
		return false;
	}

}
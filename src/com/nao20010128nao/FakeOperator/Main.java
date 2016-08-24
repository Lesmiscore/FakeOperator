package com.nao20010128nao.FakeOperator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.ServerException;
import cn.nukkit.utils.TextFormat;

public class Main extends PluginBase implements Listener {
	Map<String, Simulation> players = new HashMap<>();
	File fileDir;
	Gson gson = new Gson();
	ConsoleCommandSender ccs;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		ccs = new ConsoleCommandSender();
		getDataFolder().mkdirs();
		if ((fileDir = new File(getDataFolder(), "players.json")).exists()) {
			Reader r = null;
			try {
				r = openReader(fileDir);
				players = gson.fromJson(r, HashMap.class);
			} catch (Throwable e) {
				getServer().getLogger().alert("Failed to load the list of players. (File or directory exists)");
				e.printStackTrace();
			} finally {
				try {
					if (r != null)
						r.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public void onDisable() {
		Writer w = null;
		try {
			w = openWriter(fileDir);
			gson.toJson(players, w);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				if (w != null)
					w.close();
			} catch (IOException e) {
			}
		}
	}

	private Reader openReader(File f) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(f)));
	}

	private Writer openWriter(File f) throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		if (!sender.isOp())
			return false;
		if (args.length == 0)
			return false;
		if (command.getName().equalsIgnoreCase("fakeop")) {
			String resolved, mes;
			switch (args[0]) {
				case "r":
				case "reg":
				case "register":
					resolved = resolvePlayerName(args[1]);
					players.put(resolved.toLowerCase(), new Simulation());
					mes = "Added " + resolved + " in the list";
					sender.sendMessage(mes);
					ccs.sendMessage("[FakeOperator] " + command.getName() + ": " + mes);
					if (getServer().getPlayer(resolved) != null)
						getServer().getPlayer(resolved).sendMessage(TextFormat.GRAY + "You are now op!");
					return true;
				case "u":
				case "unreg":
				case "unregister":
					resolved = resolvePlayerName(args[1]);
					players.remove(resolved.toLowerCase());
					mes = "Removed " + resolved + " from the list";
					sender.sendMessage(mes);
					ccs.sendMessage("[FakeOperator] " + command.getName() + ": " + mes);
					if (getServer().getPlayer(resolved) != null)
						getServer().getPlayer(resolved).sendMessage(TextFormat.GRAY + "You are no longer op!");
					return true;
				case "list":
					StringBuilder sb = new StringBuilder();
					sb.append("There are ").append(players.size()).append(" players in the list:\n");
					for (String s : players.keySet())
						sb.append(s).append(", ");
					sb.setLength(sb.length() - 2);
					sender.sendMessage(sb.toString());
					return true;
			}
		}
		return false;
	}

	private String resolvePlayerName(String s) {
		Player player = getServer().getPlayer(s);
		if (player == null)
			return s;
		else
			return player.getName();
	}

	/**
	 * <a href=
	 * "https://github.com/Nukkit/Nukkit/tree/master/src/main/java/cn/nukkit/command/defaults">
	 * https://github.com/Nukkit/Nukkit/tree/master/src/main/java/cn/nukkit/
	 * command/defaults</a>
	 */
	@EventHandler
	public void commandPreProp(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().isOp())
			return;// The player has operator permission now, so we don't spoof
					// the permission
		if (!players.containsKey(event.getPlayer().getName().toLowerCase()))
			return;// The player isn't in the list
		String cmd = event.getMessage();
		if (!cmd.startsWith("/"))
			return;// it's not a command
		cmd = cmd.substring(1);
		String[] cmdSplitted = cmd.split(" ");
		String cmdIdentifier = cmdSplitted[0];
		boolean needCancel = false;

		CommandSender sender = event.getPlayer();
		String[] args = deleteFirst(cmdSplitted);
		try {
			switch (cmdIdentifier) {
				case "ban":// let players go away from the command sender
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.ban.usage"));
						return;
					}
					String name = args[0];
					Player player = sender.getServer().getPlayerExact(name);
					if (player != event.getPlayer())
						player.despawnFrom(event.getPlayer());// instead of ban
					else
						player.setBanned(true);

					sender.sendMessage(new TranslationContainer("%commands.ban.success",
							player != null ? player.getName() : name));

					players.get(sender.getName().toLowerCase()).ban.add(name.toLowerCase());
				}
					break;
				case "ban-ip":// let players go away from the command sender
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.banip.usage"));
						return;
					}
					String value = args[0];
					String reason = "";
					for (int i = 1; i < args.length; i++)
						reason += args[i] + " ";
					if (reason.length() > 0)
						reason = reason.substring(0, reason.length() - 1);
					if (Pattern.matches(
							"^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$",
							value)) {
						this.processIPBan(value, sender, reason);
						sender.sendMessage(
								new TranslationContainer("commands.banip.success", value));
					} else {
						Player player = sender.getServer().getPlayer(value);
						if (player != null) {
							this.processIPBan(player.getAddress(), sender, reason);
							sender.sendMessage(
									new TranslationContainer("commands.banip.success.players",
											new String[] { player.getAddress(), player.getName() }));
						} else {
							String name = value.toLowerCase();
							String path = sender.getServer().getDataPath() + "players/";
							File file = new File(path + name + ".dat");
							CompoundTag nbt = null;
							if (file.exists())
								try {
									nbt = NBTIO.readCompressed(new FileInputStream(file));
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							if (nbt != null && nbt.contains("lastIP")
									&& Pattern.matches(
											"^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$",
											value = nbt.getString("lastIP"))) {
								this.processIPBan(value, sender, reason);

								sender.sendMessage(
										new TranslationContainer("commands.banip.success", value));
							} else {
								sender.sendMessage(new TranslationContainer("commands.banip.invalid"));
								return;
							}
						}
					}
					players.get(sender.getName().toLowerCase()).ipban.add(value.toLowerCase());
				}
					break;
				case "banlist":// let the command sender simulated ban list
					needCancel = true; {

					Set<String> list;
					String arg;
					if (args.length > 0) {
						arg = args[0].toLowerCase();
						if ("ips".equals(arg))
							list = players.get(sender.getName().toLowerCase()).ipban;
						else if ("players".equals(arg))
							list = players.get(sender.getName().toLowerCase()).ban;
						else {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.banlist.usage"));
							return;
						}
					} else {
						list = players.get(sender.getName().toLowerCase()).ban;
						arg = "players";
					}

					String message = "";
					for (String entry : list)
						message += entry + ", ";

					if ("ips".equals(arg))
						sender.sendMessage(
								new TranslationContainer("commands.banlist.ips", String.valueOf(list.size())));
					else
						sender.sendMessage(
								new TranslationContainer("commands.banlist.players", String.valueOf(list.size())));

					if (message.length() > 0)
						message = message.substring(0, message.length() - 2);
					sender.sendMessage(message);
				}
					break;
				case "defaultgamemode":// only messages
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage",
										new String[] { "%commands.defaultgamemode.usage" }));
						return;
					}
					int gameMode = Server.getGamemodeFromString(args[0]);
					if (gameMode != -1)
						sender.sendMessage(new TranslationContainer("commands.defaultgamemode.success",
								new String[] { Server.getGamemodeString(gameMode) }));
					else
						sender.sendMessage("Unknown game mode"); //
				}
				case "deop":// only messages
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.deop.usage"));
						return;
					}

					String playerName = args[0];
					IPlayer player = sender.getServer().getOfflinePlayer(playerName);
					player.setOp(false);

					if (player instanceof Player)
						((Player) player).sendMessage(TextFormat.GRAY + "You are no longer op!");

					sender.sendMessage(
							new TranslationContainer("commands.deop.success", new String[] { player.getName() }));
				}
				case "difficulty":// only messages
					needCancel = true; {

					if (args.length != 1) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.difficulty.usage"));
						return;
					}

					int difficulty = Server.getDifficultyFromString(args[0]);

					if (sender.getServer().isHardcore())
						difficulty = 3;
					if (difficulty != -1)
						sender.sendMessage(
								new TranslationContainer("commands.difficulty.success", String.valueOf(difficulty)));
					else {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.difficulty.usage"));
						return;
					}
				}
				case "effect":// don't work
					needCancel = true; {
					Player player = sender.getServer().getPlayer(args[0]);
					if (player == null) {
						sender.sendMessage(
								new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
						return;
					}
					try {
						Effect.getEffect(Integer.parseInt(args[1]));
					} catch (NumberFormatException | ServerException a) {
						try {
							Effect.getEffectByName(args[1]);
						} catch (Exception e) {
							sender.sendMessage(new TranslationContainer("commands.effect.notFound", args[1]));
							return;
						}
					}
					if (args.length >= 3)
						try {
							Integer.valueOf(args[2]);
						} catch (NumberFormatException a) {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.effect.usage"));
							return;
						}
					if (args.length >= 4)
						try {
							Integer.valueOf(args[3]);
						} catch (NumberFormatException a) {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.effect.usage"));
							return;
						}
					sender.sendMessage(
							new TranslationContainer("commands.effect.failure.notActive.all", player.getDisplayName()));
				}
				case "enchant":// don't work
					needCancel = true; {
					if (args.length < 2) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.enchant.usage"));
						return;
					}
					Player player = sender.getServer().getPlayer(args[0]);
					if (player == null) {
						sender.sendMessage(
								new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
						return;
					}
					int enchantId;
					int enchantLevel;
					try {
						enchantId = Integer.parseInt(args[1]);
						enchantLevel = args.length == 3 ? Integer.parseInt(args[2]) : 1;
					} catch (NumberFormatException e) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.enchant.usage"));
						return;
					}
					Enchantment enchantment = Enchantment.getEnchantment(enchantId);
					if (enchantment == null) {
						sender.sendMessage(
								new TranslationContainer("commands.enchant.notFound", String.valueOf(enchantId)));
						return;
					}
					enchantment.setLevel(enchantLevel);
					Item item = player.getInventory().getItemInHand();
					if (item.getId() <= 0) {
						sender.sendMessage(new TranslationContainer("commands.enchant.noItem"));
						return;
					}
					sender.sendMessage(new TranslationContainer("%commands.enchant.success"));
				}
				case "gamemode":// don't work
					needCancel = true; {

					if (args.length == 0) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.gamemode.usage"));
						return;
					}

					int gameMode = Server.getGamemodeFromString(args[0]);

					if (gameMode == -1) {
						sender.sendMessage("Unknown game mode");
						return;
					}

					CommandSender target = sender;

					if (args.length > 1) {
						target = sender.getServer().getPlayer(args[1]);
						if (target == null) {
							sender.sendMessage(
									new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
							return;
						}
					} else if (!(sender instanceof Player)) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.gamemode.usage"));
						return;
					}

					sender.sendMessage("Game mode update for " + target.getName() + " failed");

				}
				case "give":// don't work
					needCancel = true; {
					if (args.length < 2) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%nukkit.command.give.usage"));
						return;
					}

					Player player = sender.getServer().getPlayer(args[0]);
					Item item;

					try {
						item = Item.fromString(args[1]);
					} catch (Exception e) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%nukkit.command.give.usage"));
						return;
					}

					if (player != null) {
						if (item.getId() == 0) {
							sender.sendMessage(
									new TranslationContainer(TextFormat.RED + "%commands.give.item.notFound", args[1]));
							return;
						}
					} else {
						sender.sendMessage(
								new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
						return;
					}
					sender.sendMessage(new TranslationContainer(
							"%commands.give.success",
							new String[] {
									item.getName() + " (" + item.getId() + ":" + item.getDamage() + ")",
									String.valueOf(item.getCount()),
									player.getName()
							}));
				}
				case "kill":// suiciding will work, but killing other player
							// will failure
					needCancel = true; {
					if (args.length >= 2) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%nukkit.command.kill.usage"));
						return;
					}
					if (args.length == 1) {
						if (!sender.hasPermission("nukkit.command.kill.other")) {
							sender.sendMessage(
									new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
							return;
						}
						Player player = sender.getServer().getPlayer(args[0]);
						if (player != null)
							sender.sendMessage(
									new TranslationContainer("commands.kill.successful", player.getName()));
						else
							sender.sendMessage(
									new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
						return;
					}
					needCancel = false;// suicide
				}
				case "list":// exclude "banned" player
					needCancel = true; {
					String online = "";
					int onlineCount = 0;
					for (Player player : sender.getServer().getOnlinePlayers().values())
						if (!players.get(sender.getName().toLowerCase()).ban.contains(player.getName().toLowerCase())
								& player.isOnline()
								&& (!(sender instanceof Player) || ((Player) sender).canSee(player))) {
							online += player.getDisplayName() + ", ";
							++onlineCount;
						}

					if (online.length() > 0)
						online = online.substring(0, online.length() - 2);

					sender.sendMessage(new TranslationContainer("commands.players.list",
							new String[] { String.valueOf(onlineCount),
									String.valueOf(sender.getServer().getMaxPlayers()) }));
					sender.sendMessage(online);
					return;
				}
				case "op":// don't work
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.op.usage"));
						return;
					}

					String name = args[0];
					IPlayer player = sender.getServer().getOfflinePlayer(name);

					sender.sendMessage(new TranslationContainer("commands.op.success", player.getName()));
					if (player instanceof Player)
						((Player) player).sendMessage(TextFormat.GRAY + "You are now op!");

					return;
				}
				case "aaaaaaaaaaaaaaaaa":// template
					needCancel = true; {
				}
			}
		} finally {
			event.setCancelled(needCancel);
		}
	}

	private void processIPBan(String ip, CommandSender sender, String reason) {
		if (sender instanceof Player)
			for (Player player : new ArrayList<>(sender.getServer().getOnlinePlayers().values()))
				if (player.getAddress().equals(ip))
					if (player == sender) {
						player.setBanned(true);
						return;
					} else
						player.despawnFrom((Player) sender);
	}

	public String[] deleteFirst(String[] a) {
		List<String> data = new ArrayList<String>(Arrays.asList(a));
		data.remove(0);
		return data.toArray(new String[a.length - 1]);
	}

	public static class Simulation {
		public Set<String> ban;
		public Set<String> ipban;
	}
}

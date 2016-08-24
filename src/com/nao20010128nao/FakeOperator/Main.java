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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import cn.nukkit.IPlayer;
import cn.nukkit.Nukkit;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.AngryVillagerParticle;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.level.particle.EnchantParticle;
import cn.nukkit.level.particle.EnchantmentTableParticle;
import cn.nukkit.level.particle.ExplodeParticle;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.level.particle.HappyVillagerParticle;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.HugeExplodeParticle;
import cn.nukkit.level.particle.InkParticle;
import cn.nukkit.level.particle.InstantEnchantParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.level.particle.LargeExplodeParticle;
import cn.nukkit.level.particle.LavaDripParticle;
import cn.nukkit.level.particle.LavaParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.level.particle.PortalParticle;
import cn.nukkit.level.particle.RainSplashParticle;
import cn.nukkit.level.particle.RedstoneParticle;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.level.particle.SplashParticle;
import cn.nukkit.level.particle.SporeParticle;
import cn.nukkit.level.particle.TerrainParticle;
import cn.nukkit.level.particle.WaterDripParticle;
import cn.nukkit.level.particle.WaterParticle;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.Task;
import cn.nukkit.timings.Timings;
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

		final CommandSender sender = event.getPlayer();
		final String[] args = deleteFirst(cmdSplitted);
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
					break;
				case "deop":// only messages
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.deop.usage"));
						return;
					}

					String playerName = args[0];
					IPlayer player = sender.getServer().getOfflinePlayer(playerName);

					players.remove(player.getName().toLowerCase());
					if (player instanceof Player)
						((Player) player).sendMessage(TextFormat.GRAY + "You are no longer op!");

					sender.sendMessage(
							new TranslationContainer("commands.deop.success", new String[] { player.getName() }));
				}
					break;
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
					break;
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
					break;
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
					break;
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
					break;
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
					break;
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
					break;
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
				case "op":// gives a fake operator permission
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.op.usage"));
						return;
					}

					String name = args[0];
					IPlayer player = sender.getServer().getOfflinePlayer(name);

					sender.sendMessage(new TranslationContainer("commands.op.success", player.getName()));
					players.put(player.getName().toLowerCase(), new Simulation());
					if (player instanceof Player)
						((Player) player).sendMessage(TextFormat.GRAY + "You are now op!");

					return;
				}
				case "pardon":// delete from the list
					needCancel = true; {
					if (args.length != 1) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.unban.usage"));
						return;
					}
					players.get(sender.getName().toLowerCase()).ban.remove(args[0].toLowerCase());
					sender.sendMessage(new TranslationContainer("%commands.unban.success", args[0]));
				}
				case "pardon-ip":// delete from the list
					needCancel = true; {
					if (args.length != 1) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.unbanip.usage"));
						return;
					}
					String value = args[0];
					if (Pattern.matches(
							"^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$",
							value)) {
						players.get(sender.getName().toLowerCase()).ipban.remove(value.toLowerCase());
						sender.sendMessage(new TranslationContainer("commands.unbanip.success", value));
					} else
						sender.sendMessage(new TranslationContainer("commands.unbanip.invalid"));
				}
					break;
				case "particle":// don't apply particle(s)
					needCancel = true; {
					if (args.length < 7) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%nukkit.command.particle.usage"));
						return;
					}

					Level level;
					if (sender instanceof Player)
						level = ((Player) sender).getLevel();
					else
						level = sender.getServer().getDefaultLevel();

					String name = args[0].toLowerCase();

					float[] floats = new float[6];
					for (int i = 0; i < floats.length; i++)
						try {
							double d = Double.valueOf(args[i + 1]);
							floats[i] = (float) d;
						} catch (Exception e) {
							return;
						}

					Vector3 pos = new Vector3(floats[0], floats[1], floats[2]);
					float xd = floats[3];
					float yd = floats[4];
					float zd = floats[5];

					int count = 1;
					if (args.length > 7)
						try {
							double c = Double.valueOf(args[7]);
							count = (int) c;
						} catch (Exception e) {
							// ignore
						}
					count = Math.max(1, count);

					Integer data = null;
					if (args.length > 8)
						try {
							double d = Double.valueOf(args[8]);
							data = (int) d;
						} catch (Exception e) {
							// ignore
						}

					Particle particle = this.getParticle(name, pos, xd, yd, zd, data);

					if (particle == null) {
						sender.sendMessage(
								new TranslationContainer(TextFormat.RED + "%commands.particle.notFound", name));
						return;
					}

					sender.sendMessage(new TranslationContainer("commands.particle.success",
							new String[] { name, String.valueOf(count) }));
				}
					break;
				case "plugins":
					// send this plugin only (if the query is off)
					// send the list of plugins (if the query is on)
					needCancel = true; {
					if (getServer().getPropertyBoolean("enable-query"))
						this.sendPluginList(sender);
					else
						this.sendFakePluginList(sender);
				}
					break;
				case "reload":// simulate the reloading
					needCancel = true; {
					sender.sendMessage(new TranslationContainer(
							TextFormat.YELLOW + "%nukkit.command.reload.reloading" + TextFormat.WHITE));
					getServer().getScheduler().scheduleDelayedTask(new Task() {
						@Override
						public void onRun(int currentTick) {
							// TODO 自動生成されたメソッド・スタブ
							sender.sendMessage(new TranslationContainer(
									TextFormat.YELLOW + "%nukkit.command.reload.reloaded" + TextFormat.WHITE));
						}
					}, 20 * 10);
				}
					break;
				case "save-all":// simulate the saving
					needCancel = true; {
					sender.sendMessage(new TranslationContainer("commands.save.start"));
					getServer().getScheduler().scheduleDelayedTask(new Task() {

						@Override
						public void onRun(int currentTick) {
							// TODO 自動生成されたメソッド・スタブ
							sender.sendMessage(new TranslationContainer("commands.save.success"));
						}
					}, 20 * 10);
				}
					break;
				case "save-off":// only messages
					needCancel = true; {
					sender.sendMessage(new TranslationContainer("commands.save.disabled"));
				}
					break;
				case "save-on":// only messages
					needCancel = true; {
					sender.sendMessage(new TranslationContainer("commands.save.enabled"));
				}
					break;
				case "say":// only messages
					needCancel = true; {
					if (args.length == 0) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.say.usage"));
						return;
					}

					String senderString;
					if (sender instanceof Player)
						senderString = ((Player) sender).getDisplayName();
					else if (sender instanceof ConsoleCommandSender)
						senderString = "Server";
					else
						senderString = sender.getName();

					String msg = "";
					for (String arg : args)
						msg += arg + " ";
					if (msg.length() > 0)
						msg = msg.substring(0, msg.length() - 1);

					sender.sendMessage(new TranslationContainer(
							TextFormat.LIGHT_PURPLE + "%chat.type.announcement",
							new String[] { senderString, TextFormat.LIGHT_PURPLE + msg }));
				}
					break;
				case "setworldspawn":// only messages
					needCancel = true; {
					Level level;
					Vector3 pos;
					if (args.length == 0) {
						if (sender instanceof Player) {
							level = ((Player) sender).getLevel();
							pos = ((Player) sender).round();
						} else {
							sender.sendMessage(new TranslationContainer("commands.generic.ingame"));
							return;
						}
					} else if (args.length == 3) {
						level = sender.getServer().getDefaultLevel();
						try {
							pos = new Vector3(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
									Integer.parseInt(args[2]));
						} catch (NumberFormatException e1) {
							sender.sendMessage(new TranslationContainer("commands.generic.usage",
									"%commands.setworldspawn.usage"));
							return;
						}
					} else {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.setworldspawn.usage"));
						return;
					}
					level.setSpawnLocation(pos);
					DecimalFormat round2 = new DecimalFormat("##0.00");
					sender.sendMessage(new TranslationContainer("commands.setworldspawn.success",
							new String[] { round2.format(pos.x), round2.format(pos.y), round2.format(pos.z) }));
				}
					break;
				case "spawnpoint":// only messages
					needCancel = true; {
					Player target;
					if (args.length == 0) {
						if (sender instanceof Player)
							target = (Player) sender;
						else {
							sender.sendMessage(new TranslationContainer("commands.generic.ingame"));
							return;
						}
					} else {
						target = sender.getServer().getPlayer(args[0]);
						if (target == null) {
							sender.sendMessage(
									new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
							return;
						}
					}
					Level level = target.getLevel();
					DecimalFormat round2 = new DecimalFormat("##0.00");
					if (args.length == 4) {
						if (level != null) {
							int x;
							int y;
							int z;
							try {
								x = Integer.parseInt(args[1]);
								y = Integer.parseInt(args[2]);
								z = Integer.parseInt(args[3]);
							} catch (NumberFormatException e1) {
								sender.sendMessage(
										new TranslationContainer("commands.generic.usage",
												"%commands.spawnpoint.usage"));
								return;
							}
							if (y < 0)
								y = 0;
							if (y > 128)
								y = 128;
							target.setSpawn(new Position(x, y, z, level));
							sender.sendMessage(new TranslationContainer("commands.spawnpoint.success", new String[] {
									target.getName(), round2.format(x), round2.format(y), round2.format(z)
							}));
							return;
						}
					} else if (args.length <= 1)
						if (sender instanceof Player) {
							Position pos = (Position) sender;
							target.setSpawn(pos);
							sender.sendMessage(
									new TranslationContainer("commands.spawnpoint.success", new String[] {
											target.getName(),
											round2.format(pos.x),
											round2.format(pos.y),
											round2.format(pos.z)
									}));
							return;
						} else {
							sender.sendMessage(new TranslationContainer("commands.generic.ingame"));
							return;
						}
					sender.sendMessage(
							new TranslationContainer("commands.generic.usage", "%commands.spawnpoint.usage"));

				}
					break;
				case "status":// real status
					needCancel = true; {
					Server server = sender.getServer();
					sender.sendMessage(TextFormat.GREEN + "---- " + TextFormat.WHITE + "Server status"
							+ TextFormat.GREEN + " ----");

					long time = (System.currentTimeMillis() - Nukkit.START_TIME) / 1000;
					int seconds = NukkitMath.floorDouble(time % 60);
					int minutes = NukkitMath.floorDouble(time % 3600 / 60);
					int hours = NukkitMath.floorDouble(time % (3600 * 24) / 3600);
					int days = NukkitMath.floorDouble(time / (3600 * 24));
					String upTimeString = TextFormat.RED + "" + days + TextFormat.GOLD + " days " +
							TextFormat.RED + hours + TextFormat.GOLD + " hours " +
							TextFormat.RED + minutes + TextFormat.GOLD + " minutes " +
							TextFormat.RED + seconds + TextFormat.GOLD + " seconds";
					sender.sendMessage(TextFormat.GOLD + "Uptime: " + upTimeString);

					TextFormat tpsColor = TextFormat.GREEN;
					float tps = server.getTicksPerSecond();
					if (tps < 17)
						tpsColor = TextFormat.GOLD;
					else if (tps < 12)
						tpsColor = TextFormat.RED;

					sender.sendMessage(TextFormat.GOLD + "Current TPS: " + tpsColor + NukkitMath.round(tps, 2));

					sender.sendMessage(TextFormat.GOLD + "Load: " + tpsColor + server.getTickUsage() + "%");

					sender.sendMessage(TextFormat.GOLD + "Network upload: " + TextFormat.GREEN
							+ NukkitMath.round(server.getNetwork().getUpload() / 1024 * 1000, 2) + " kB/s");

					sender.sendMessage(TextFormat.GOLD + "Network download: " + TextFormat.GREEN
							+ NukkitMath.round(server.getNetwork().getDownload() / 1024 * 1000, 2) + " kB/s");

					sender.sendMessage(
							TextFormat.GOLD + "Thread count: " + TextFormat.GREEN + Thread.getAllStackTraces().size());

					Runtime runtime = Runtime.getRuntime();
					double totalMB = NukkitMath.round((double) runtime.totalMemory() / 1024 / 1024, 2);
					double usedMB = NukkitMath
							.round((double) (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024, 2);
					double maxMB = NukkitMath.round((double) runtime.maxMemory() / 1024 / 1024, 2);
					double usage = usedMB / maxMB * 100;
					TextFormat usageColor = TextFormat.GREEN;

					if (usage > 85)
						usageColor = TextFormat.GOLD;

					sender.sendMessage(TextFormat.GOLD + "Used memory: " + usageColor + usedMB + " MB. ("
							+ NukkitMath.round(usage, 2) + "%)");

					sender.sendMessage(TextFormat.GOLD + "Total memory: " + TextFormat.RED + totalMB + " MB.");

					sender.sendMessage(TextFormat.GOLD + "Maximum VM memory: " + TextFormat.RED + maxMB + " MB.");

					sender.sendMessage(TextFormat.GOLD + "Available processors: " + TextFormat.GREEN
							+ runtime.availableProcessors());

					TextFormat playerColor = TextFormat.GREEN;
					if ((float) server.getOnlinePlayers().size() / (float) server.getMaxPlayers() > 0.85)
						playerColor = TextFormat.GOLD;

					sender.sendMessage(TextFormat.GOLD + "Players: " + playerColor + server.getOnlinePlayers().size()
							+ TextFormat.GREEN + " online, " +
							TextFormat.RED + server.getMaxPlayers() + TextFormat.GREEN + " max. ");

					for (Level level : server.getLevels().values())
						sender.sendMessage(
								TextFormat.GOLD + "World \"" + level.getFolderName() + "\""
										+ (!Objects.equals(level.getFolderName(), level.getName())
												? " (" + level.getName() + ")" : "")
										+ ": " +
										TextFormat.RED + level.getChunks().size() + TextFormat.GREEN + " chunks, " +
										TextFormat.RED + level.getEntities().length + TextFormat.GREEN + " entities, " +
										TextFormat.RED + level.getBlockEntities().size() + TextFormat.GREEN
										+ " blockEntities." +
										" Time "
										+ (level.getTickRate() > 1 || level.getTickRateTime() > 40 ? TextFormat.RED
												: TextFormat.YELLOW)
										+ NukkitMath.round(level.getTickRateTime(), 2) + "ms" +
										(level.getTickRate() > 1 ? " (tick rate " + level.getTickRate() + ")" : ""));
				}
					break;
				case "stop":// ban myself
					needCancel = true; {
					sender.sendMessage(new TranslationContainer("commands.stop.start"));
					getServer().getScheduler().scheduleDelayedTask(new Task() {
						@Override
						public void onRun(int currentTick) {
							Player player = (Player) sender;
							player.kick("Server closed");
							player.setBanned(true);
							sender.getServer().getNetwork().blockAddress(player.getAddress(), -1);
						}
					}, 10);
				}
					break;
				case "tp":// failure
					needCancel = true; {
					if (args.length < 1 || args.length > 6) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.tp.usage"));
						return;
					}
					CommandSender target;
					CommandSender origin = sender;
					if (args.length == 1 || args.length == 3) {
						if (sender instanceof Player)
							target = sender;
						else {
							sender.sendMessage(new TranslationContainer("commands.generic.ingame"));
							return;
						}
						if (args.length == 1) {
							target = sender.getServer().getPlayer(args[0]);
							if (target == null) {
								sender.sendMessage(TextFormat.RED + "Can't find player " + args[0]);
								return;
							}
						}
					} else {
						target = sender.getServer().getPlayer(args[0]);
						if (target == null) {
							sender.sendMessage(TextFormat.RED + "Can't find player " + args[0]);
							return;
						}
						if (args.length == 2) {
							origin = target;
							target = sender.getServer().getPlayer(args[1]);
							if (target == null) {
								sender.sendMessage(TextFormat.RED + "Can't find player " + args[1]);
								return;
							}
						}
					}
					if (args.length < 3) {
						sender.sendMessage("Failed to teleport");
						return;
					} else if (((Player) target).getLevel() != null) {
						int pos;
						if (args.length == 4 || args.length == 6)
							pos = 1;
						else
							pos = 0;
						double x;
						double y;
						double z;
						double yaw;
						double pitch;
						try {
							x = Double.parseDouble(args[pos++]);
							y = Double.parseDouble(args[pos++]);
							z = Double.parseDouble(args[pos++]);
							yaw = ((Player) target).getYaw();
							pitch = ((Player) target).getPitch();
						} catch (NumberFormatException e1) {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.tp.usage"));
							return;
						}
						if (y < 0)
							y = 0;
						if (y > 128)
							y = 128;
						if (args.length == 6 || args.length == 5 && pos == 3) {
							yaw = Integer.parseInt(args[pos++]);
							pitch = Integer.parseInt(args[pos++]);
						}
						sender.sendMessage("Failed to teleport");
						return;
					}
					sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.tp.usage"));
				}
					break;
				case "time":// simulate the change
					needCancel = true; {
					Simulation sim = players.get(sender.getName().toLowerCase());
					if ("start".equals(args[0])) {
						sim.timeStopping = false;
						sim.timeStop.clear();
						sender.sendMessage("Restarted the time");
						return;
					} else if ("stop".equals(args[0])) {
						sim.timeStopping = true;
						for (Level level : sender.getServer().getLevels().values())
							sim.timeStop.put(level.getName(), level.getTime());
						sender.sendMessage("Stopped the time");
						return;
					} else if ("query".equals(args[0])) {
						Level level;
						if (sender instanceof Player)
							level = ((Player) sender).getLevel();
						else
							level = sender.getServer().getDefaultLevel();
						sender.sendMessage(
								new TranslationContainer("commands.time.query",
										String.valueOf(sim.getSimulatingTime(level))));
						return;
					}

					if (args.length < 2) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%nukkit.command.time.usage"));
						return;
					}

					if ("set".equals(args[0])) {
						int value;
						if ("day".equals(args[1]))
							value = Level.TIME_DAY;
						else if ("night".equals(args[1]))
							value = Level.TIME_NIGHT;
						else
							try {
								value = Math.max(0, Integer.parseInt(args[1]));
							} catch (Exception e) {
								sender.sendMessage(
										new TranslationContainer("commands.generic.usage",
												"%nukkit.command.time.usage"));
								return;
							}

						if (sim.timeStopping)
							for (Level level : sender.getServer().getLevels().values())
								sim.timeStop.put(level.getName(), value);
						else
							sim.timeShift = ((Player) sender).getLevel().getTime() - value;

						sender.sendMessage(new TranslationContainer("commands.time.set", String.valueOf(value)));
					} else if ("add".equals(args[0])) {
						int value;
						try {
							value = Math.max(0, Integer.parseInt(args[1]));
						} catch (Exception e) {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%nukkit.command.time.usage"));
							return;
						}

						if (sim.timeStopping)
							for (Level level : sender.getServer().getLevels().values())
								sim.timeStop.put(level.getName(), sim.timeStop.get(level.getName()) + value);
						else
							sim.timeShift += value;
						sender.sendMessage(new TranslationContainer("commands.time.added", String.valueOf(value)));
					} else
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%nukkit.command.time.usage"));
				}
					break;
				case "timings":// don't work
					needCancel = true; {

					if (args.length != 1) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%nukkit.command.timings.usage"));
						return;
					}

					String mode = args[0].toLowerCase();

					if (mode.equals("on")) {
						sender.sendMessage(new TranslationContainer("nukkit.command.timings.enable"));
						return;
					} else if (mode.equals("off")) {
						sender.sendMessage(new TranslationContainer("nukkit.command.timings.disable"));
						return;
					}

					if (!Timings.isTimingsEnabled())
						return;

					switch (mode) {
						case "verbon":
							sender.sendMessage(new TranslationContainer("nukkit.command.timings.verboseEnable"));
							break;
						case "verboff":
							sender.sendMessage(new TranslationContainer("nukkit.command.timings.verboseDisable"));
							break;
						case "reset":
							sender.sendMessage(new TranslationContainer("nukkit.command.timings.reset"));
							break;
						case "report":
						case "paste":
							break;
					}
				}
					break;
				case "weather":// simulate the weather(only clear or rain)
					needCancel = true; {
					if (args.length == 0 || args.length > 2) {
						sender.sendMessage(
								new TranslationContainer("commands.weather.usage", "%commands.weather.usage"));
						return;
					}

					String weather = args[0];
					if (args.length > 1)
						try {
							Integer.parseInt(args[1]);
						} catch (Exception e) {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.weather.usage"));
							return;
						}

					switch (weather) {
						case "clear":
						case "rain":
						case "thunder":
							sender.sendMessage("Failed to set the weather");
							return;
						default:
							sender.sendMessage(
									new TranslationContainer("commands.weather.usage", "%commands.weather.usage"));
							return;
					}
				}
				case "whitelist":// simulate the changes
					needCancel = true; {
					Simulation sim = players.get(sender.getName().toLowerCase());
					if (args.length == 0 || args.length > 2) {
						sender.sendMessage(
								new TranslationContainer("commands.generic.usage", "%commands.whitelist.usage"));
						return;
					}

					if (args.length == 1)
						switch (args[0].toLowerCase()) {
							case "reload":
								sender.sendMessage(new TranslationContainer("commands.whitelist.reloaded"));
								return;
							case "on":
								sim.whitelisting = true;
								sender.sendMessage(new TranslationContainer("commands.whitelist.enabled"));
								return;
							case "off":
								sim.whitelisting = false;
								sender.sendMessage(new TranslationContainer("commands.whitelist.disabled"));
								return;
							case "list":
								String result = "";
								int count = 0;
								for (String player : sim.whitelist) {
									result += player + ", ";
									++count;
								}
								sender.sendMessage(new TranslationContainer("commands.whitelist.list",
										new String[] { String.valueOf(count), String.valueOf(count) }));
								sender.sendMessage(result.length() > 0 ? result.substring(0, result.length() - 2) : "");
								return;

							case "add":
								sender.sendMessage(new TranslationContainer("commands.generic.usage",
										"%commands.whitelist.add.usage"));
								return;

							case "remove":
								sender.sendMessage(new TranslationContainer("commands.generic.usage",
										"%commands.whitelist.remove.usage"));
								return;
						}
					else if (args.length == 2)
						switch (args[0].toLowerCase()) {
							case "add":
								sim.whitelist.add(args[1].toLowerCase());
								sender.sendMessage(new TranslationContainer("commands.whitelist.add.success", args[1]));
								return;
							case "remove":
								sim.whitelist.remove(args[1].toLowerCase());
								sender.sendMessage(
										new TranslationContainer("commands.whitelist.remove.success", args[1]));
								return;
						}

				}
					break;
				case "xp":// failure
					needCancel = true; {
					String amountString;
					String playerName = "";
					Player player = null;
					if (!(sender instanceof Player)) {
						if (args.length != 2) {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.xp.usage"));
							return;
						}
						amountString = args[0];
						playerName = args[1];
						player = sender.getServer().getPlayer(playerName);
					} else if (args.length == 1) {
						amountString = args[0];
						player = (Player) sender;
					} else if (args.length == 2) {
						amountString = args[0];
						playerName = args[1];
						player = sender.getServer().getPlayer(playerName);
					} else {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.xp.usage"));
						return;
					}

					if (player == null) {
						sender.sendMessage(
								new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
						return;
					}

					int amount;
					boolean isLevel = false;
					if (amountString.endsWith("l") || amountString.endsWith("L")) {
						amountString = amountString.substring(0, amountString.length() - 1);
						isLevel = true;
					}

					try {
						amount = Integer.parseInt(amountString);
					} catch (NumberFormatException e1) {
						sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.xp.usage"));
						return;
					}

					if (isLevel) {
						sender.sendMessage("Failure to give experiences");
						return;
					} else {
						if (amount < 0) {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.xp.usage"));
							return;
						}
						sender.sendMessage("Failure to give experiences");
						return;
					}
				}
				case "aaaaaaaaaaaaaaaaa":// template
					needCancel = true; {
				}
					break;
			}
		} finally {
			event.setCancelled(needCancel);
		}
	}

	@EventHandler
	public void dataPacketSend(DataPacketSendEvent event) {

	}

	/**
	 * For ban-ip command
	 */
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

	/**
	 * For particle command
	 */
	private Particle getParticle(String name, Vector3 pos, float xd, float yd, float zd, Integer data) {
		switch (name) {
			case "explode":
				return new ExplodeParticle(pos);
			case "largeexplode":
				return new LargeExplodeParticle(pos);
			case "hugeexplosion":
				return new HugeExplodeParticle(pos);
			case "bubble":
				return new BubbleParticle(pos);
			case "splash":
				return new SplashParticle(pos);
			case "wake":
			case "water":
				return new WaterParticle(pos);
			case "crit":
				return new CriticalParticle(pos);
			case "smoke":
				return new SmokeParticle(pos, data != null ? data : 0);
			case "spell":
				return new EnchantParticle(pos);
			case "instantspell":
				return new InstantEnchantParticle(pos);
			case "dripwater":
				return new WaterDripParticle(pos);
			case "driplava":
				return new LavaDripParticle(pos);
			case "townaura":
			case "spore":
				return new SporeParticle(pos);
			case "portal":
				return new PortalParticle(pos);
			case "flame":
				return new FlameParticle(pos);
			case "lava":
				return new LavaParticle(pos);
			case "reddust":
				return new RedstoneParticle(pos, data != null ? data : 1);
			case "snowballpoof":
				return new ItemBreakParticle(pos, Item.get(Item.SNOWBALL));
			case "slime":
				return new ItemBreakParticle(pos, Item.get(Item.SLIMEBALL));
			case "itembreak":
				if (data != null && data != 0)
					return new ItemBreakParticle(pos, Item.get(data));
				break;
			case "terrain":
				if (data != null && data != 0)
					return new TerrainParticle(pos, Block.get(data));
				break;
			case "heart":
				return new HeartParticle(pos, data != null ? data : 0);
			case "ink":
				return new InkParticle(pos, data != null ? data : 0);
			case "droplet":
				return new RainSplashParticle(pos);
			case "enchantmenttable":
				return new EnchantmentTableParticle(pos);
			case "happyvillager":
				return new HappyVillagerParticle(pos);
			case "angryvillager":
				return new AngryVillagerParticle(pos);

		}

		if (name.startsWith("iconcrack_")) {
			String[] d = name.split("_");
			if (d.length == 3)
				return new ItemBreakParticle(pos, Item.get(Integer.valueOf(d[1]), Integer.valueOf(d[2])));
		} else if (name.startsWith("blockcrack_")) {
			String[] d = name.split("_");
			if (d.length == 2)
				return new TerrainParticle(pos, Block.get(Integer.valueOf(d[1]) & 0xff, Integer.valueOf(d[1]) >> 12));
		} else if (name.startsWith("blockdust_")) {
			String[] d = name.split("_");
			if (d.length >= 4)
				return new DustParticle(pos, Integer.valueOf(d[1]) & 0xff, Integer.valueOf(d[2]) & 0xff,
						Integer.valueOf(d[3]) & 0xff, d.length >= 5 ? Integer.valueOf(d[4]) & 0xff : 255);
		}

		return null;
	}

	/**
	 * For plugins command
	 */
	private void sendPluginList(CommandSender sender) {
		String list = "";
		Map<String, Plugin> plugins = sender.getServer().getPluginManager().getPlugins();
		for (Plugin plugin : plugins.values()) {
			if (list.length() > 0)
				list += TextFormat.WHITE + ", ";
			list += plugin.isEnabled() ? TextFormat.GREEN : TextFormat.RED;
			list += plugin.getDescription().getFullName();
		}

		sender.sendMessage(new TranslationContainer("nukkit.command.plugins.success",
				new String[] { String.valueOf(plugins.size()), list }));
	}

	/**
	 * For plugins command
	 */
	private void sendFakePluginList(CommandSender sender) {
		String list = "";
		for (Plugin plugin : new Plugin[] { this }) {
			if (list.length() > 0)
				list += TextFormat.WHITE + ", ";
			list += plugin.isEnabled() ? TextFormat.GREEN : TextFormat.RED;
			list += plugin.getDescription().getFullName();
		}

		sender.sendMessage(new TranslationContainer("nukkit.command.plugins.success",
				new String[] { String.valueOf(1), list }));
	}

	public String[] deleteFirst(String[] a) {
		List<String> data = new ArrayList<String>(Arrays.asList(a));
		data.remove(0);
		return data.toArray(new String[a.length - 1]);
	}

	public static class Simulation {
		public Set<String> ban = new HashSet<>();
		public Set<String> ipban = new HashSet<>();
		public Set<String> whitelist = new HashSet<>();
		public boolean whitelisting = false;
		public int timeShift = -1;
		public Map<String, Integer> timeStop = new HashMap<>();
		public boolean timeStopping = false;

		public int getSimulatingTime(Level lev) {
			if (timeStopping) {
				if (timeStop.containsKey(lev.getName()))
					timeStop.put(lev.getName(), lev.getTime());
				return timeStop.get(lev.getName());
			} else
				return lev.getTime() + timeShift;
		}
	}
}

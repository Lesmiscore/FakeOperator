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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.permission.BanList;
import cn.nukkit.plugin.PluginBase;

public class Main extends PluginBase implements Listener {
	Set<String> players = new HashSet<>();
	File fileDir;
	Gson gson = new Gson();
	ConsoleCommandSender ccs;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		ccs = new ConsoleCommandSender();
		if ((fileDir = new File(getDataFolder(), "players.json")).exists()) {
			Reader r = null;
			try {
				r = openReader(fileDir);
				players = gson.fromJson(r, HashSet.class);
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
		if (command.getName().equalsIgnoreCase("fakeop")) {
			String resolved, mes;
			switch (args[0]) {
				case "r":
				case "reg":
				case "register":
					resolved = resolvePlayerName(args[1]);
					players.add(resolved.toLowerCase());
					mes = "Added " + resolved + " in the list";
					sender.sendMessage(mes);
					ccs.sendMessage("[FakeOperator] " + command.getName() + ": " + mes);
					return true;
				case "u":
				case "unreg":
				case "unregister":
					resolved = resolvePlayerName(args[1]);
					players.remove(resolved.toLowerCase());
					mes = "Removed " + resolved + " from the list";
					sender.sendMessage(mes);
					ccs.sendMessage("[FakeOperator] " + command.getName() + ": " + mes);
					return true;
				case "list":
					StringBuilder sb = new StringBuilder();
					sb.append("There are ").append(players.size()).append(" players in the list:\n");
					for (String s : players)
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

	@EventHandler
	public void commandPreProp(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().isOp())
			return;// The player has operator permission now, so we don't spoof
					// the permission
		if (!players.contains(event.getPlayer().getName().toLowerCase()))
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
					player.despawnFrom(event.getPlayer());// instead of ban
					sender.sendMessage(new TranslationContainer("%commands.ban.success",
							player != null ? player.getName() : name));
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
						Command.broadcastCommandMessage(sender,
								new TranslationContainer("commands.banip.success", value));
					} else {
						Player player = sender.getServer().getPlayer(value);
						if (player != null) {
							this.processIPBan(player.getAddress(), sender, reason);
							Command.broadcastCommandMessage(sender,
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

								Command.broadcastCommandMessage(sender,
										new TranslationContainer("commands.banip.success", value));
							} else {
								sender.sendMessage(new TranslationContainer("commands.banip.invalid"));
								return;
							}
						}
					}

				}
					break;
				case "banlist":// let the command sender blank ban list
					needCancel = true; {

					BanList list;
					String arg;
					if (args.length > 0) {
						arg = args[0].toLowerCase();
						if ("ips".equals(arg))
							list = sender.getServer().getIPBans();
						else if ("players".equals(arg))
							list = sender.getServer().getNameBans();
						else {
							sender.sendMessage(
									new TranslationContainer("commands.generic.usage", "%commands.banlist.usage"));
							return;
						}
					} else {
						list = sender.getServer().getNameBans();
						arg = "players";
					}

					String message = "";

					if ("ips".equals(arg))
						sender.sendMessage(
								new TranslationContainer("commands.banlist.ips", String.valueOf(0)));
					else
						sender.sendMessage(
								new TranslationContainer("commands.banlist.players", String.valueOf(0)));

					if (message.length() > 0)
						message = message.substring(0, message.length() - 2);
					sender.sendMessage(message);
				}
					break;

			}
		} finally {
			event.setCancelled(needCancel);
		}
	}

	private void processIPBan(String ip, CommandSender sender, String reason) {
		if (sender instanceof Player)
			for (Player player : new ArrayList<>(sender.getServer().getOnlinePlayers().values()))
				if (player.getAddress().equals(ip))
					player.despawnFrom((Player) sender);
	}

	public String[] deleteFirst(String[] a) {
		List<String> data = new ArrayList<String>(Arrays.asList(a));
		data.remove(0);
		return data.toArray(new String[a.length - 1]);
	}
}

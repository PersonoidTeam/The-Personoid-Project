package com.personoid.api.utils.bukkit;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message implements MessageBase {
    private static final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private static String prefix = "[Personoid] ";

    private TextComponent message = new TextComponent();
    private final List<CommandSender> receivers = new ArrayList<>();
    private boolean usePrefix;

    public Message() {

    }

    public Message(String message) {
        this.message = new TextComponent(toColor(message));
    }

    public Message(TextComponent message) {
        this.message = new TextComponent(toColor(message.getText()));
    }

    public Message send() {
        console.spigot().sendMessage(new TextComponent(usePrefix ? prefix + message.getText() : message.getText()));
        return this;
    }

    private Message sendTo() {
        for (CommandSender receiver : receivers) {
            if (receiver instanceof Player) {
                Player player = (Player) receiver;
                player.spigot().sendMessage(new TextComponent(usePrefix ? prefix + message.getText() : message.getText()));
            }
        }
        return this;
    }

    @Override
    public Message send(Preset... presets) {
        for (Preset preset : presets) {
            if (preset == Preset.ALL_PLAYERS) {
                receivers.clear();
                receivers.addAll(Bukkit.getOnlinePlayers());
            }
        }
        return sendTo();
    }

    @Override
    public Message send(Player... players) {
        receivers.clear();
        receivers.addAll(Arrays.asList(players));
        return sendTo();
    }

    @Override
    public Message send(CommandSender... senders) {
        receivers.clear();
        receivers.addAll(Arrays.asList(senders));
        return sendTo();
    }

    @Override
    public Message send(Collection<Player> players) {
        receivers.clear();
        receivers.addAll(players);
        return sendTo();
    }

    public Message prefix() {
        usePrefix = true;
        return this;
    }

    public enum Preset {
        ALL_PLAYERS,
    }

    public enum Action {
        RUN_COMMAND,
        SUGGEST_COMMAND,
        OPEN_URL,
        COPY_TO_CLIPBOARD,
        CHANGE_PAGE,
        OPEN_FILE,
        SHOW_TOOLTIP_TEXT
    }

    public static void setPrefix(String prefix) {
        Message.prefix = prefix;
    }

    public static String toColor(String text) {
        return toColor('&', new String[]{text}).get(0);
    }

    public static List<String> toColor(String... text) {
        return toColor('&', text);
    }

    public static String toColor(Character character, String text) {
        return toColor(character, new String[]{text}).get(0);
    }

    public static List<String> toColor(Character character, String... text) {
        List<String> coloured = new ArrayList<>();
        for (String string : text) {
            Matcher match = pattern.matcher(string);
            while (match.find()) {
                String colour = string.substring(match.start(), match.end());
                string = string.replace(colour, String.valueOf(net.md_5.bungee.api.ChatColor.of(colour)));
                match = pattern.matcher(string);
            }
            coloured.add(ChatColor.translateAlternateColorCodes(character, string));
        }
        return coloured;
    }
}

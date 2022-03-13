package com.notnotdoddy.personoid.utils.message;

import com.notnotdoddy.personoid.PersonoidAPI;
import com.notnotdoddy.personoid.utils.task.Task;
import com.notnotdoddy.personoid.utils.task.TaskRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message implements MessageBase {
    private static final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private static String prefix = "[" + PersonoidAPI.getPlugin().getName() + "] ";

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
            if (receiver instanceof Player player) {
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
        receivers.addAll(List.of(players));
        return sendTo();
    }

    @Override
    public Message send(CommandSender... senders) {
        receivers.clear();
        receivers.addAll(List.of(senders));
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

    public Title title(String title, String subtitle) {
        return new Title(title, subtitle);
    }

    public Actionbar actionbar(String message) {
        return new Actionbar(message);
    }

    public static class Title implements MessageBase {
        private final String title;
        private String subtitle = "";
        private int in = 20;
        private int stay = 60;
        private int out = 20;
        private Runnable onFinish;

        public Title(String title) {
            this.title = toColor(title);
        }

        public Title(String title, String subtitle) {
            this.title = toColor(title);
            this.subtitle = toColor(subtitle);
        }

        public Title in(int in) {
            this.in = in;
            return this;
        }

        public Title stay(int stay) {
            this.stay = stay;
            return this;
        }

        public Title out(int out) {
            this.out = out;
            return this;
        }

        public Title onFinish(Runnable runnable) {
            onFinish = runnable;
            return this;
        }

        private Title sendTo() {
            for (Player player : receivers) {
                player.sendTitle(title, subtitle, in, stay, out);
            }
            if (onFinish != null) {
                new Task((TaskRunnable) onFinish).run(in + stay + out);
            }
            return this;
        }

        @Override
        public Title send(Preset... presets) {
            for (Preset preset : presets) {
                if (preset == Preset.ALL_PLAYERS) {
                    receivers.clear();
                    receivers.addAll(Bukkit.getOnlinePlayers());
                }
            }
            return sendTo();
        }

        @Override
        public Title send(Player... players) {
            receivers.clear();
            receivers.addAll(List.of(players));
            return sendTo();
        }

        @Override
        public Title send(Collection<Player> players) {
            receivers.clear();
            receivers.addAll(players);
            return sendTo();
        }

        @Override
        public MessageBase send(CommandSender... senders) {
            receivers.clear();
            receivers.addAll(List.of((Player[])senders));
            return sendTo();
        }
    }

    public static class Actionbar implements MessageBase {
        private final String message;
        private Runnable onFinish;
        private int stay = 40;

        public Actionbar(String message) {
            this.message = toColor(message);
        }

        public Actionbar onFinish(Runnable runnable) {
            onFinish = runnable;
            return this;
        }

        public Actionbar stay(int stay) {
            this.stay = Math.max(stay, 40);
            return this;
        }

        private Actionbar sendTo() {
            if (stay > 40) {
                new Task(this::sendMessage).repeat(0, 1, stay - 40);
            } else {
                sendMessage();
            }
            if (onFinish != null) {
                new Task((TaskRunnable) onFinish).run(stay + 20);
            }
            return this;
        }

        @Override
        public Actionbar send(Preset... presets) {
            for (Preset preset : presets) {
                if (preset == Preset.ALL_PLAYERS) {
                    receivers.clear();
                    receivers.addAll(Bukkit.getOnlinePlayers());
                }
            }
            return sendTo();
        }

        @Override
        public Actionbar send(Player... players) {
            receivers.clear();
            receivers.addAll(List.of(players));
            return sendTo();
        }

        @Override
        public Actionbar send(Collection<Player> players) {
            receivers.clear();
            receivers.addAll(players);
            return sendTo();
        }

        @Override
        public MessageBase send(CommandSender... senders) {
            receivers.clear();
            receivers.addAll(List.of((Player[])senders));
            return sendTo();
        }

        private void sendMessage() {
            for (Player player : receivers) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        }
    }

    public static class Builder {
        private TextComponent text;

        public Builder() {
            this.text = new TextComponent();
        }

        public Builder(String text) {
            this.text = new TextComponent(toColor(text));
        }

        public Builder(String text, Map<Action, String> actions) {
            add(text, actions);
        }

        public Builder add(String text, Map<Action, String> actions) {
            TextComponent component = new TextComponent(toColor(text));
            for (Map.Entry<Action, String> entry : actions.entrySet()) {
                if (entry.getKey() != Action.SHOW_TOOLTIP_TEXT) {
                    component.setClickEvent(new ClickEvent(toAction(entry.getKey()), entry.getValue()));
                } else {
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(entry.getValue())));
                }
            }
            if (this.text != null) {
                this.text.addExtra(component);
            } else {
                this.text = component;
            }
            return this;
        }

        public Builder add(String text, Action type, String action) {
            TextComponent component = new TextComponent(toColor(text));
            if (type != Action.SHOW_TOOLTIP_TEXT) {
                component.setClickEvent(new ClickEvent(toAction(type), action));
            } else {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(action)));
            }
            this.text.addExtra(component);
            return this;
        }

        public Builder add(String text) {
            this.text.addExtra(toColor(text));
            return this;
        }

        private ClickEvent.Action toAction(Action action) {
            switch (action) {
                case RUN_COMMAND -> { return ClickEvent.Action.RUN_COMMAND; }
                case SUGGEST_COMMAND -> { return ClickEvent.Action.SUGGEST_COMMAND; }
                case OPEN_URL -> { return ClickEvent.Action.OPEN_URL; }
                case COPY_TO_CLIPBOARD -> { return ClickEvent.Action.COPY_TO_CLIPBOARD; }
                case CHANGE_PAGE -> { return ClickEvent.Action.CHANGE_PAGE; }
                case OPEN_FILE -> { return ClickEvent.Action.OPEN_FILE; }
                default -> { return null; }
            }
        }

        public TextComponent build() {
            return text;
        }
    }
}

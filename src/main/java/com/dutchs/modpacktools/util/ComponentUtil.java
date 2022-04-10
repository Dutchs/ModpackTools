package com.dutchs.modpacktools.util;

import com.dutchs.modpacktools.Constants;
import net.minecraft.network.chat.*;
import net.minecraft.util.Tuple;

import java.util.List;

public class ComponentUtil {

    private static final HoverEvent HOVEREVENT_COPY = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Copy"));
    private static final HoverEvent HOVEREVENT_SUGGEST_COMMAND = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Copy Command"));
    private static final HoverEvent HOVEREVENT_RUN_COMMAND = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Run Command"));
    private static final HoverEvent HOVEREVENT_TELEPORT_COMMAND = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Teleport"));

    public static Component withCopy(Component component, String copyText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText)).withHoverEvent(HOVEREVENT_COPY));
    }

    public static Component withSuggestCommand(Component component, String commandText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandText)).withHoverEvent(HOVEREVENT_SUGGEST_COMMAND));
    }

    public static Component withRunCommand(Component component, String commandText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandText)).withHoverEvent(HOVEREVENT_RUN_COMMAND));
    }

    public static Component withTeleportCommand(Component component, String commandText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandText)).withHoverEvent(HOVEREVENT_TELEPORT_COMMAND));
    }

    public static Component formatTitleContent(String title, String content) {
        MutableComponent comp = new TextComponent(title + "\n").withStyle(Constants.TITLE_FORMAT);
        comp.append(new TextComponent("----------------------------------\n").withStyle(Constants.BORDER_FORMAT));
        comp.append(new TextComponent(content + "\n").withStyle(Constants.CHAT_FORMAT));
        return comp;
    }

    public static Component formatTitleContentWithCopy(String title, String content) {
        return ComponentUtil.withCopy(formatTitleContent(title, content), content);
    }

    public static Component formatTitleKeyValueWithCopy(String title, String key, String value) {
        MutableComponent comp = new TextComponent(title + "\n").withStyle(Constants.TITLE_FORMAT);
        comp.append(new TextComponent("----------------------------------\n").withStyle(Constants.BORDER_FORMAT));
        comp.append(new TextComponent(key + ": ").withStyle(Constants.TITLE_FORMAT));
        comp.append(new TextComponent(value + "\n").withStyle(Constants.CHAT_FORMAT));
        return ComponentUtil.withCopy(comp, value);
    }

    public static Component formatTitleKeyValueWithCopy(String title, List<Tuple<String, String>> keyValues) {
        MutableComponent comp = new TextComponent(title + "\n").withStyle(Constants.TITLE_FORMAT);
        comp.append(new TextComponent("----------------------------------").withStyle(Constants.BORDER_FORMAT));
        if (keyValues != null && keyValues.size() > 0) {
            comp.append("\n");
            for (Tuple<String, String> keyValue : keyValues) {
                comp.append(new TextComponent(keyValue.getA() + ": ").withStyle(Constants.TITLE_FORMAT));
                comp.append(ComponentUtil.withCopy(new TextComponent(keyValue.getB() + "\n").withStyle(Constants.CHAT_FORMAT), keyValue.getB()));
            }
        }
        return comp;
    }

    public static Component formatKeyValueWithCopy(String key, String value) {
        MutableComponent comp = new TextComponent("");
        comp.append(new TextComponent(key + ": ").withStyle(Constants.TITLE_FORMAT));
        comp.append(new TextComponent(String.valueOf(value)).withStyle(Constants.CHAT_FORMAT));
        return ComponentUtil.withCopy(comp, String.valueOf(value));
    }

    public static void appendKeyValueWithCopy(MutableComponent comp, String title, String key, String value) {
        comp.append(new TextComponent(key + ": ").withStyle(Constants.TITLE_FORMAT));
        comp.append(ComponentUtil.withCopy(new TextComponent(value).withStyle(Constants.CHAT_FORMAT), value));
    }
}

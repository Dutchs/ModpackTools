package com.dutchs.modpacktools.util;

import com.dutchs.modpacktools.Constants;
import net.minecraft.network.chat.*;
import net.minecraft.util.Tuple;

import java.util.List;

public class ComponentUtil {

    private static final HoverEvent HOVEREVENT_COPY = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy"));
    private static final HoverEvent HOVEREVENT_SUGGEST_COMMAND = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy Command"));
    private static final HoverEvent HOVEREVENT_RUN_COMMAND = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Run Command"));
    private static final HoverEvent HOVEREVENT_TELEPORT_COMMAND = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Teleport"));
    private static final HoverEvent HOVEREVENT_OPEN_FILE = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Open File"));

    public static Component withCopy(Component component, String copyText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText)).withHoverEvent(HOVEREVENT_COPY));
    }

    public static Component withSuggestCommand(Component component, String commandText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandText)).withHoverEvent(HOVEREVENT_SUGGEST_COMMAND));
    }

    public static Component withRunCommand(Component component, String commandText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandText)).withHoverEvent(HOVEREVENT_RUN_COMMAND));
    }

    public static Component withOpenFile(Component component, String commandText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, commandText)).withHoverEvent(HOVEREVENT_OPEN_FILE));
    }

    public static Component withTeleportCommand(Component component, String commandText) {
        return component.copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandText)).withHoverEvent(HOVEREVENT_TELEPORT_COMMAND));
    }

    public static Component formatTitleContent(String title, String content) {
        MutableComponent comp = Component.literal(title + "\n").withStyle(Constants.TITLE_FORMAT);
        comp.append(Component.literal("----------------------------------\n").withStyle(Constants.BORDER_FORMAT));
        comp.append(Component.literal(content + "\n").withStyle(Constants.CHAT_FORMAT));
        return comp;
    }

    public static Component formatTitleContent(String title, Component content) {
        MutableComponent comp = Component.literal(title + "\n").withStyle(Constants.TITLE_FORMAT);
        comp.append(Component.literal("----------------------------------\n").withStyle(Constants.BORDER_FORMAT));
        comp.append(content);
        comp.append("\n");
        return comp;
    }

    public static Component formatTitleContentWithCopy(String title, String content) {
        return ComponentUtil.withCopy(formatTitleContent(title, content), content);
    }

    public static Component formatTitleContentWithCopy(String title, String content, String copyContent) {
        return ComponentUtil.withCopy(formatTitleContent(title, content), copyContent);
    }

    public static Component formatTitleKeyValueWithCopy(String title, String key, String value) {
        MutableComponent comp = Component.literal(title + "\n").withStyle(Constants.TITLE_FORMAT);
        comp.append(Component.literal("----------------------------------\n").withStyle(Constants.BORDER_FORMAT));
        comp.append(Component.literal(key + ": ").withStyle(Constants.TITLE_FORMAT));
        comp.append(Component.literal(value + "\n").withStyle(Constants.CHAT_FORMAT));
        return ComponentUtil.withCopy(comp, value);
    }

    public static Component formatTitleKeyValueWithCopy(String title, List<Tuple<String, String>> keyValues) {
        MutableComponent comp = Component.literal(title + "\n").withStyle(Constants.TITLE_FORMAT);
        comp.append(Component.literal("----------------------------------").withStyle(Constants.BORDER_FORMAT));
        if (keyValues != null && keyValues.size() > 0) {
            comp.append("\n");
            for (Tuple<String, String> keyValue : keyValues) {
                comp.append(Component.literal(keyValue.getA() + ": ").withStyle(Constants.TITLE_FORMAT));
                comp.append(ComponentUtil.withCopy(Component.literal(keyValue.getB() + "\n").withStyle(Constants.CHAT_FORMAT), keyValue.getB()));
            }
        }
        return comp;
    }

    public static Component formatKeyValueWithCopy(String key, String value) {
        MutableComponent comp = Component.literal("");
        comp.append(Component.literal(key + ": ").withStyle(Constants.TITLE_FORMAT));
        comp.append(Component.literal(String.valueOf(value)).withStyle(Constants.CHAT_FORMAT));
        return ComponentUtil.withCopy(comp, String.valueOf(value));
    }

    public static void appendKeyValueWithCopy(MutableComponent comp, String title, String key, String value) {
        comp.append(Component.literal(key + ": ").withStyle(Constants.TITLE_FORMAT));
        comp.append(ComponentUtil.withCopy(Component.literal(value).withStyle(Constants.CHAT_FORMAT), value));
    }
}

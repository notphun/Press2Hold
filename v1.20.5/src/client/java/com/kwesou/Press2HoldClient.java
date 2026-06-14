package com.kwesou;

import com.kwesou.common.ConfigManager;
import com.kwesou.common.HoldManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Press2HoldClient implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static final HoldManager holdManager = new HoldManager();
    private static final ConfigManager configManager = new ConfigManager();
    private static final Path configDir = FabricLoader.getInstance().getConfigDir();
    private static final Logger LOGGER = Press2Hold.LOGGER;
    public String displayType;
    private int tickCounter = 0;
    public static final String[] supportedPropertyKeys = {"displayType"};
    public static final String[] supportedDisplayTypes = {"Action", "Chat", "Title"};


    @Override
    public void onInitializeClient() {
        configManager.resolveConfig(configDir, LOGGER);
        this.displayType = configManager.getDisplayType();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommandManager.literal("press2hold")
                                .then(ClientCommandManager.argument("propertyKey", StringArgumentType.word())
                                        .suggests((context, builder) ->
                                                net.minecraft.command.CommandSource.suggestMatching(
                                                        Arrays.asList(supportedPropertyKeys), builder
                                                )
                                        )
                                        .executes(context -> executeWithOneArg(context))

                                        .then(ClientCommandManager.argument("propertyValue", StringArgumentType.word())
                                                .suggests((context, builder) ->
                                                        net.minecraft.command.CommandSource.suggestMatching(
                                                                Arrays.asList(supportedDisplayTypes), builder
                                                        )
                                                )
                                                .executes(context -> executeWithTwoArgs(context))
                                        )
                                )
                )
        );

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.press2hold.latch",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_G,
                "key.categories.press2hold"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {

        while (toggleKey.wasPressed()) {
            boolean latched = holdManager.toggle();

            if (latched) {
                Set<String> keys = capturePressedKeys(client);
                holdManager.setKeys(keys);

                if (holdManager.isEmpty()) {
                    holdManager.toggle();
                    showToPlayer(client, "No valid inputs pressed", true);
                } else {
                    String clientMsg = "Latched: [" + holdManager.formatKeys() + "]";
                    showToPlayer(client, clientMsg, true);
                }

            } else {
                releaseKeys(client);
                holdManager.clear();
                showToPlayer(client, "Unlatched", true);
            }
        }

        if (holdManager.isLatched()) {
            pressKeys(client);

            tickCounter++;
            if (tickCounter >= 30) {
                String clientMsg = "Latched: [" + holdManager.formatKeys() + "]";
                showToPlayer(client, clientMsg, false);
                tickCounter = 0;
            }
        } else {
            tickCounter = 0;
        }
    }

    private Set<String> capturePressedKeys(MinecraftClient client) {
        Set<String> keys = new HashSet<>();

        for (KeyBinding key : client.options.allKeys) {
            if (key.isPressed() && !key.equals(toggleKey)) {
                keys.add(key.getBoundKeyLocalizedText().getString());
            }
        }

        return keys;
    }

    private void releaseKeys(MinecraftClient client) {
    for (KeyBinding key : client.options.allKeys) {
        key.setPressed(false);
    }
}

    private void pressKeys(MinecraftClient client) {
        for (KeyBinding key : client.options.allKeys) {
            if (holdManager.getKeys().contains(key.getBoundKeyLocalizedText().getString())) {
                key.setPressed(true);
            }
        }
    }

    private static int executeWithOneArg(CommandContext<FabricClientCommandSource> context) {
        String propertyKey = StringArgumentType.getString(context, "propertyKey");
        String keyValue = configManager.getDisplayType();
        context.getSource().sendFeedback(
                Text.literal(propertyKey + " is set to " + keyValue)
        );
        return 1;
    }

    private static int executeWithTwoArgs(CommandContext<FabricClientCommandSource> context) {
        try {
            String propertyKey = StringArgumentType.getString(context, "propertyKey");
            String newPropertyValue = StringArgumentType.getString(context, "propertyValue");
            switch(propertyKey){
                case "displayType" -> {
                    if (Arrays.asList(supportedDisplayTypes).contains(newPropertyValue)) {
                        configManager.setDisplayType(newPropertyValue);
                        context.getSource().sendFeedback(
                                Text.literal(propertyKey + " has been set to " + newPropertyValue)
                        );
                        return 1;
                    }
                    else {
                        context.getSource().sendError(Text.of("unknown DisplayType " + newPropertyValue));
                        return 0;
                    }
                }
                default -> {
                    context.getSource().sendError(Text.of("unknown propertyKey " + propertyKey));
                    return 0;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to update config", e);
            context.getSource().sendError(Text.literal("Config save failed"));
            return 0;
        }
    }

    private void showToPlayer(MinecraftClient client, String msg, boolean stateChanged) {
        if (client.player == null) return;
        String displayType = configManager.getDisplayType();

        switch(displayType) {
            case "Chat" -> {
                if (stateChanged) {
                    client.player.sendMessage(Text.literal(msg), false);
                }
            }
            case "Action" -> client.inGameHud.setOverlayMessage(Text.literal(msg), false);
            case "Title" -> {
                client.inGameHud.setTitleTicks(0, 50, 10);
                client.inGameHud.setTitle(Text.literal(msg));
            }
            default -> {
                LOGGER.info("unknown display type: {}", displayType);
                if (stateChanged) {
                    client.player.sendMessage(Text.literal(msg), false);
                }
            }
        }
    }
}
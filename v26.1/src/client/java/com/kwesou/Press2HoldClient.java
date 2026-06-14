package com.kwesou;

import com.kwesou.common.ConfigManager;
import com.kwesou.common.HoldManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Press2HoldClient implements ClientModInitializer {
    private static KeyMapping toggleKey;
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

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(Commands.literal("press2hold")
                .then(Commands.argument("propertyKey", StringArgumentType.string())
                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                supportedPropertyKeys, builder))
                        .executes(Press2HoldClient::executeWithOneArg)
                        .then(Commands.argument("propertyValue", StringArgumentType.string())
                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        supportedDisplayTypes, builder))
                                .executes(Press2HoldClient::executeWithTwoArgs)))));


        // In 26.1, Categories are registered explicitly using KeyMapping.Category.register
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("press2hold", "press2hold")
        );

        // KeyBindingHelper is now KeyMappingHelper in the new keymapping.v1 package
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.press2hold.latch",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft client) {
        // wasPressed() is now consumeClick()???
        while (toggleKey.consumeClick()) {
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

    private Set<String> capturePressedKeys(Minecraft client) {
        Set<String> keys = new HashSet<>();

        String toggleKeyName = toggleKey.getTranslatedKeyMessage().getString();

        for (KeyMapping key : client.options.keyMappings) {
            String currentKeyName = key.getTranslatedKeyMessage().getString();

            if (key.isDown() && !currentKeyName.equals(toggleKeyName)) {
                keys.add(currentKeyName);
            }
        }

        return keys;
    }

    private void releaseKeys(Minecraft client) {
        for (KeyMapping key : client.options.keyMappings) {
            key.setDown(false);
        }
    }

    private void pressKeys(Minecraft client) {
        for (KeyMapping key : client.options.keyMappings) {
            if (holdManager.getKeys().contains(key.getTranslatedKeyMessage().getString())) {
                key.setDown(true);
            }
        }
    }

    private static int executeWithOneArg(CommandContext<CommandSourceStack> context) {
        String propertyKey = StringArgumentType.getString(context, "propertyKey");
        String keyValue = configManager.getDisplayType();
        context.getSource().sendSuccess(
                () -> Component.literal(propertyKey + " is set to " + keyValue),
                false
        );
        return 1;
    }

    private static int executeWithTwoArgs(CommandContext<CommandSourceStack> context) {
        try {
            String propertyKey = StringArgumentType.getString(context, "propertyKey");
            String newPropertyValue = StringArgumentType.getString(context, "propertyValue");
            switch(propertyKey){
                case "displayType" -> {
                    if (Arrays.asList(supportedDisplayTypes).contains(newPropertyValue)) {
                        configManager.setDisplayType(newPropertyValue);
                        context.getSource().sendSuccess(
                                () -> Component.literal(propertyKey + " has been set to " + newPropertyValue), false
                        );
                        return 1;
                    }
                    else
                        context.getSource().sendFailure(Component.nullToEmpty("unknown DisplayType " + newPropertyValue));
                    return 0;
                }
                default -> {
                    context.getSource().sendFailure(Component.nullToEmpty("unknown propertyKey " + propertyKey));
                    return 0;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to update config", e);
            context.getSource().sendFailure(Component.literal("Config save failed"));
            return 0;
        }
    }

    private void showToPlayer(Minecraft client, String msg, boolean stateChanged) {
        if (client.player == null) return;
        String displayType = configManager.getDisplayType();

        switch(displayType) {
            case "Chat" -> {
                if (stateChanged) {
                    client.player.sendSystemMessage(Component.literal(msg));
                }
            }
            case "Action" -> client.player.sendOverlayMessage(Component.nullToEmpty(msg));
            case "Title" -> {
                client.gui.setTimes(0, 50, 10);
                client.gui.setTitle(Component.literal(msg));
            }
            default -> {
                LOGGER.info("unknown display type: {}", displayType);
                if (stateChanged) {
                    client.player.sendSystemMessage(Component.literal(msg));
                }
            }
        }
    }
}
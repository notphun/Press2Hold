package com.kwesou;

import com.kwesou.common.HoldManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class Press2HoldClient implements ClientModInitializer {

    private static KeyMapping toggleKey;
    private static final HoldManager manager = new HoldManager();

    @Override
    public void onInitializeClient() {

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

            boolean latched = manager.toggle();

            if (latched) {
                Set<String> keys = capturePressedKeys(client);

                manager.setKeys(keys);

                if (manager.isEmpty()) {
                    manager.toggle();
                    sendMessage(client, "No valid inputs pressed");
                } else {
                    sendMessage(client, "Latched: [" + manager.formatKeys() + "]");
                }

            } else {
                releaseKeys(client);
                manager.clear();
                sendMessage(client, "Unlatched");
            }
        }

        if (manager.isLatched()) {
            pressKeys(client);
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
            if (manager.getKeys().contains(key.getTranslatedKeyMessage().getString())) {
                key.setDown(true);
            }
        }
    }

    private void sendMessage(Minecraft client, String msg) {
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(msg));
        }
    }
}
package com.kwesou;

import com.kwesou.common.HoldManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class Press2HoldClient implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static final HoldManager manager = new HoldManager();

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.press2hold.latch",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_G,
                KeyBinding.Category.create(Identifier.of("press2hold:press2hold"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {

        while (toggleKey.wasPressed()) {

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
            if (manager.getKeys().contains(key.getBoundKeyLocalizedText().getString())) {
                key.setPressed(true);
            }
        }
    }

    private void sendMessage(MinecraftClient client, String msg) {
        if (client.player != null) {
            client.player.sendMessage(Text.of(msg), false);
        }
    }
}
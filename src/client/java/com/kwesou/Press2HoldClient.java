package com.kwesou;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class Press2HoldClient implements ClientModInitializer {
	private static KeyMapping keyBinding;
	private static boolean isLatched = false;
	Set<Integer> pressedKeys = new HashSet<>();
	Set<Integer> pressedMouseButtons = new HashSet<>();
	Set<String> pressedKeyNames = new HashSet<>();

	@Override
	public void onInitializeClient() {
		keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.press2hold.latch",
				InputConstants.Type.KEYSYM,
				GLFW_KEY_G,
				KeyMapping.Category.register(Identifier.parse("press2hold:press2hold"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.consumeClick()) {
				isLatched = !isLatched;
				getCurrentlyPressedInputs();

				if (isLatched && (!pressedKeys.isEmpty() || !pressedMouseButtons.isEmpty())) {
					for (int key : pressedKeys) {
						InputConstants.Key keyObj = InputConstants.Type.KEYSYM.getOrCreate(key);
						KeyMapping.set(keyObj, true);
						pressedKeyNames.add(getKeyName(key));
					}
					for (int button : pressedMouseButtons) {
						KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(button), true);
						pressedKeyNames.add("MOUSE" + button);
					}
					if (client.player != null) {
						client.player.sendSystemMessage(Component.literal("Latching: " + pressedKeyNames.toString()));
					}
				} else if (pressedKeys.isEmpty() && pressedMouseButtons.isEmpty()) {
					if (client.player != null) {
						client.player.sendSystemMessage(Component.literal("Invalid inputs pressed"));
					}
					isLatched = false;
					pressedKeys.clear();
					pressedMouseButtons.clear();
					pressedKeyNames.clear();
				} else {
					for (int key : pressedKeys) {
						InputConstants.Key keyObj = InputConstants.Type.KEYSYM.getOrCreate(key);
						KeyMapping.set(keyObj, false);
					}
					for (int button : pressedMouseButtons) {
						KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(button), false);
					}
					pressedKeys.clear();
					pressedMouseButtons.clear();
					pressedKeyNames.clear();
					if (client.player != null) {
						client.player.sendSystemMessage(Component.literal("Unlatched"));
					}
				}
			}
			if (isLatched) {
				for (int key : pressedKeys) {
					InputConstants.Key keyObj = InputConstants.Type.KEYSYM.getOrCreate(key);
					KeyMapping.set(keyObj, true);
				}
				for (int button : pressedMouseButtons) {
					KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(button), true);
				}
			} else {
				for (int key : pressedKeys) {
					InputConstants.Key keyObj = InputConstants.Type.KEYSYM.getOrCreate(key);
					KeyMapping.set(keyObj, false);
				}
				for (int button : pressedMouseButtons) {
					KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(button), false);
				}
			}
		});
	}

	public static String getKeyName(int key) {
		String keyName = glfwGetKeyName(key, 0);

		if (keyName != null) {
			return keyName.toUpperCase();
		}

		return switch (key) {
			case GLFW_KEY_SPACE -> "SPACE";
			case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> "CTRL";
			case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> "SHIFT";
			case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> "ALT";
			case GLFW_KEY_ENTER -> "ENTER";
			case GLFW_KEY_BACKSPACE -> "BACKSPACE";
			case GLFW_KEY_ESCAPE -> "ESC";
			case GLFW_KEY_TAB -> "TAB";
			case GLFW_KEY_CAPS_LOCK -> "CAPS LOCK";
			default -> String.valueOf(key);
		};
	}

	public void getCurrentlyPressedInputs() {
		long windowHandle = Minecraft.getInstance().getWindow().handle();
		int keyCode = keyBinding.getDefaultKey().getValue();

		for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
			if (key != keyCode && glfwGetKey(windowHandle, key) == GLFW_PRESS) {
				pressedKeys.add(key);
			}
		}

		for (int button = GLFW_MOUSE_BUTTON_1; button <= GLFW_MOUSE_BUTTON_LAST; button++) {
			if (glfwGetMouseButton(windowHandle, button) == GLFW_PRESS) {
				pressedMouseButtons.add(button);
			}
		}
	}
}

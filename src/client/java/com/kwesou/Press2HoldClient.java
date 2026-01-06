package com.kwesou;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class Press2HoldClient implements ClientModInitializer {
	private static KeyBinding keyBinding;
	private static boolean isLatched = false;
	Set<Integer> pressedKeys = new HashSet<>();
	Set<Integer> pressedMouseButtons = new HashSet<>();
	Set<String> pressedKeyNames = new HashSet<>();

	@Override
	public void onInitializeClient() {
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Latch down keys",
				InputUtil.Type.KEYSYM,
				GLFW_KEY_G,
				KeyBinding.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.wasPressed()) {
				isLatched = !isLatched;
				getCurrentlyPressedInputs();

				if (isLatched && (!pressedKeys.isEmpty() || !pressedMouseButtons.isEmpty())) {
					for (int key : pressedKeys) {
						InputUtil.Key keyObj = InputUtil.Type.KEYSYM.createFromCode(key);
						KeyBinding.setKeyPressed(keyObj, true);
						pressedKeyNames.add(getKeyName(key));
					}
					for (int button : pressedMouseButtons) {
						KeyBinding.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(button), true);
						pressedKeyNames.add("MOUSE" + button);
					}
					assert client.player != null;
					client.player.sendMessage(Text.of("Latching: " + pressedKeyNames.toString()), false);
				} else if (pressedKeys.isEmpty() && pressedMouseButtons.isEmpty()) {
					assert client.player != null;
					client.player.sendMessage(Text.of("Invalid inputs pressed"), false);
					isLatched = false;
					pressedKeys.clear();
					pressedMouseButtons.clear();
					pressedKeyNames.clear();
				} else {
					for (int key : pressedKeys) {
						InputUtil.Key keyObj = InputUtil.Type.KEYSYM.createFromCode(key);
						KeyBinding.setKeyPressed(keyObj, false);
					}
					for (int button : pressedMouseButtons) {
						KeyBinding.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(button), false);
					}
					pressedKeys.clear();
					pressedMouseButtons.clear();
					pressedKeyNames.clear();
					assert client.player != null;
					client.player.sendMessage(Text.of("Unlatched"), false);
				}
			}
			if (isLatched) {
				for (int key : pressedKeys) {
					InputUtil.Key keyObj = InputUtil.Type.KEYSYM.createFromCode(key);
					KeyBinding.setKeyPressed(keyObj, true);
				}
				for (int button : pressedMouseButtons) {
					KeyBinding.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(button), true);
				}
			} else {
				for (int key : pressedKeys) {
					InputUtil.Key keyObj = InputUtil.Type.KEYSYM.createFromCode(key);
					KeyBinding.setKeyPressed(keyObj, false);
				}
				for (int button : pressedMouseButtons) {
					KeyBinding.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(button), false);
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
		long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
		int keyCode = keyBinding.getDefaultKey().getCode();

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

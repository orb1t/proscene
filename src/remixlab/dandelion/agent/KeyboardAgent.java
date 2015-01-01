
package remixlab.dandelion.agent;

import remixlab.bias.agent.ActionKeyboardAgent;
import remixlab.bias.agent.profile.KeyboardProfile;
import remixlab.bias.core.*;
import remixlab.bias.event.*;
import remixlab.dandelion.core.*;
import remixlab.dandelion.core.Constants.KeyboardAction;

public class KeyboardAgent extends InputAgent {
	protected AbstractScene	scene;
	protected ActionKeyboardAgent<KeyboardProfile<KeyboardAction>> keyBranch;

	public KeyboardAgent(AbstractScene scn, String n) {
		super(scn.inputHandler(), n);
		scene = scn;
		keyBranch = new ActionKeyboardAgent<KeyboardProfile<KeyboardAction>>(new KeyboardProfile<KeyboardAction>(), this, "scene_keyboard_agent");
		sceneBranch().disableTracking();
	  // new, mimics eye -> motionAgent -> scene -> keyAgent
		sceneBranch().addInPool(scene);
		sceneBranch().setDefaultGrabber(scene);
		setDefaultShortcuts();
	}
	
	public ActionKeyboardAgent<KeyboardProfile<KeyboardAction>> sceneBranch() {
		return keyBranch;
	}

	@Override
	public KeyboardEvent feed() {
		return null;
	}
	
	// -->
	
	@Override
	public void disableTracking() {
		super.disableTracking();
		sceneBranch().disableTracking();
	}
	
	@Override
	public void enableTracking() {
		super.enableTracking();
		sceneBranch().enableTracking();
	}
	
	// <--
	
	protected KeyboardProfile<KeyboardAction> keyboardProfile() {
		return sceneBranch().keyboardProfile();
	}

	/**
	 * Set the default keyboard shortcuts as follows:
	 * <p>
	 * {@code 'a' -> KeyboardAction.TOGGLE_AXIS_VISUAL_HINT}<br>
	 * {@code 'f' -> KeyboardAction.TOGGLE_FRAME_VISUAL_HINT}<br>
	 * {@code 'g' -> KeyboardAction.TOGGLE_GRID_VISUAL_HINT}<br>
	 * {@code 'm' -> KeyboardAction.TOGGLE_ANIMATION}<br>
	 * {@code 'e' -> KeyboardAction.TOGGLE_CAMERA_TYPE}<br>
	 * {@code 'h' -> KeyboardAction.DISPLAY_INFO}<br>
	 * {@code 'r' -> KeyboardAction.TOGGLE_PATHS_VISUAL_HINT}<br>
	 * {@code 's' -> KeyboardAction.INTERPOLATE_TO_FIT}<br>
	 * {@code 'S' -> KeyboardAction.SHOW_ALL}<br>
	 */
	public void setDefaultShortcuts() {
		keyboardProfile().removeAllBindings();
		keyboardProfile().setBinding('a', KeyboardAction.TOGGLE_AXES_VISUAL_HINT);
		keyboardProfile().setBinding('f', KeyboardAction.TOGGLE_PICKING_VISUAL_HINT);
		keyboardProfile().setBinding('g', KeyboardAction.TOGGLE_GRID_VISUAL_HINT);
		keyboardProfile().setBinding('m', KeyboardAction.TOGGLE_ANIMATION);

		keyboardProfile().setBinding('e', KeyboardAction.TOGGLE_CAMERA_TYPE);
		keyboardProfile().setBinding('h', KeyboardAction.DISPLAY_INFO);
		keyboardProfile().setBinding('r', KeyboardAction.TOGGLE_PATHS_VISUAL_HINT);

		keyboardProfile().setBinding('s', KeyboardAction.INTERPOLATE_TO_FIT);
		keyboardProfile().setBinding('S', KeyboardAction.SHOW_ALL);
	}

	/**
	 * Sets the default (virtual) key to play eye paths.
	 */
	public void setKeyCodeToPlayPath(int vkey, int path) {
		switch (path) {
		case 1:
			keyboardProfile().setBinding(BogusEvent.NOMODIFIER_MASK, vkey, KeyboardAction.PLAY_PATH_1);
			break;
		case 2:
			keyboardProfile().setBinding(BogusEvent.NOMODIFIER_MASK, vkey, KeyboardAction.PLAY_PATH_2);
			break;
		case 3:
			keyboardProfile().setBinding(BogusEvent.NOMODIFIER_MASK, vkey, KeyboardAction.PLAY_PATH_3);
			break;
		default:
			break;
		}
	}

	/**
	 * Binds the key shortcut to the (Keyboard) dandelion action.
	 */
	public void setShortcut(Character key, KeyboardAction action) {
		keyboardProfile().setBinding(key, action);
	}

	/**
	 * Binds the mask-vKey (virtual key) shortcut to the (Keyboard) dandelion action.
	 */
	public void setShortcut(int mask, int vKey, KeyboardAction action) {
		keyboardProfile().setBinding(mask, vKey, action);
	}

	/**
	 * Removes key shortcut binding (if present).
	 */
	public void removeShortcut(Character key) {
		keyboardProfile().removeBinding(key);
	}

	/**
	 * Removes mask-vKey (virtual key) shortcut binding (if present).
	 */
	public void removeShortcut(int mask, int vKey) {
		keyboardProfile().removeBinding(mask, vKey);
	}

	/**
	 * Returns {@code true} if the key shortcut is bound to a (Keyboard) dandelion action.
	 */
	public boolean hasShortcut(Character key) {
		return keyboardProfile().hasBinding(key);
	}

	/**
	 * Returns {@code true} if the mask-vKey (virtual key) shortcut is bound to a (Keyboard) dandelion action.
	 */
	public boolean hasShortcut(int mask, int vKey) {
		return keyboardProfile().hasBinding(mask, vKey);
	}

	/**
	 * Returns {@code true} if the keyboard action is bound.
	 */
	public boolean isActionBound(KeyboardAction action) {
		return keyboardProfile().isActionBound(action);
	}

	/**
	 * Returns the (Keyboard) dandelion action that is bound to the given key shortcut. Returns {@code null} if no
	 * action is bound to the given shortcut.
	 */
	public KeyboardAction action(Character key) {
		return (KeyboardAction) keyboardProfile().action(key);
	}

	/**
	 * Returns the (Keyboard) dandelion action that is bound to the given mask-vKey (virtual key) shortcut. Returns
	 * {@code null} if no action is bound to the given shortcut.
	 */
	public KeyboardAction action(int mask, int vKey) {
		return (KeyboardAction) keyboardProfile().action(mask, vKey);
	}
}

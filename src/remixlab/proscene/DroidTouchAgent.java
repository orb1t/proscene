/**************************************************************************************
 * ProScene (version 2.1.0)
 * Copyright (c) 2010-2014 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos http://otrolado.info/, Victor Manuel Forero
 * 
 * All rights reserved. Library that eases the creation of interactive scenes
 * in Processing, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 **************************************************************************************/

package remixlab.proscene;

import remixlab.bias.core.*;
import remixlab.bias.event.*;
import remixlab.dandelion.agent.*;
import remixlab.dandelion.core.*;
import remixlab.dandelion.core.Constants.*;

/**
 * Proscene {@link remixlab.dandelion.agent.MultiTouchAgent}.
 */
public class DroidTouchAgent extends MultiTouchAgent {
	Scene	scene;

	public DroidTouchAgent(Scene scn, String n) {
		super(scn, n);
		// inputHandler().unregisterAgent(this);
		scene = scn;
		setDefaultBinding();
	}

	public void setDefaultBinding() {
		eyeProfile().setBinding(MotionEvent.NO_MODIFIER_MASK, Gestures.DRAG_ONE_ID.id(), DOF6Action.ROTATE);
		frameProfile().setBinding(MotionEvent.NO_MODIFIER_MASK, Gestures.DRAG_ONE_ID.id(), DOF6Action.ROTATE);
		eyeProfile().setBinding(MotionEvent.NO_MODIFIER_MASK, Gestures.DRAG_TWO_ID.id(), DOF6Action.TRANSLATE);
		frameProfile().setBinding(MotionEvent.NO_MODIFIER_MASK, Gestures.DRAG_TWO_ID.id(), DOF6Action.TRANSLATE);
		eyeProfile().setBinding(MotionEvent.NO_MODIFIER_MASK, Gestures.PINCH_TWO_ID.id(),
				scene.is3D() ? DOF6Action.TRANSLATE_Z : DOF6Action.SCALE);
		frameProfile().setBinding(DOF6Event.NO_MODIFIER_MASK, Gestures.PINCH_TWO_ID.id(),
				scene.is3D() ? DOF6Action.TRANSLATE_Z : DOF6Action.SCALE);
		eyeProfile().setBinding(MotionEvent.NO_MODIFIER_MASK, Gestures.TURN_TWO_ID.id(), DOF6Action.ROTATE_Z);
		frameProfile().setBinding(MotionEvent.NO_MODIFIER_MASK, Gestures.TURN_TWO_ID.id(), DOF6Action.ROTATE_Z);
	}

	public void touchEvent(android.view.MotionEvent e) {
		int action = e.getAction();
		int turnOrientation;
		int code = action & android.view.MotionEvent.ACTION_MASK;
		int index = action >> android.view.MotionEvent.ACTION_POINTER_ID_SHIFT;

		float x = e.getX(index);
		float y = e.getY(index);
		int id = e.getPointerId(index);
		Gestures gesture;
		// PApplet.print("touch");
		// pass the events to the TouchProcessor
		if (code == android.view.MotionEvent.ACTION_DOWN || code == android.view.MotionEvent.ACTION_POINTER_DOWN) {
			// touch(new DOF6Event(x, y, 0, 0, 0, 0));
			// PApplet.print("down");
			touchProcessor.pointDown(x, y, id);
			touchProcessor.parse();
			event = new DOF6Event(null,
					touchProcessor.getCx(),
					touchProcessor.getCy(),
					0,
					0,
					0,
					0,
					MotionEvent.NO_MODIFIER_MASK,
					e.getPointerCount());
			if (e.getPointerCount() == 1)
				updateTrackedGrabber(event);
			prevEvent = event.get();
		}
		else if (code == android.view.MotionEvent.ACTION_UP || code == android.view.MotionEvent.ACTION_POINTER_UP) {
			// PApplet.print("up");
			touchProcessor.pointUp(id);
			if (e.getPointerCount() == 1) {
				gesture = touchProcessor.parseTap();
				if (gesture == Gestures.TAP_ID) {
					handle(new ClickEvent(e.getX() - scene.originCorner().x(), e.getY() - scene.originCorner().y(), gesture.id()));
				}
				this.disableTracking();
				this.enableTracking();
			}

		}
		else if (code == android.view.MotionEvent.ACTION_MOVE) {
			// PApplet.print("move");
			int numPointers = e.getPointerCount();
			for (int i = 0; i < numPointers; i++) {
				id = e.getPointerId(i);
				x = e.getX(i);
				y = e.getY(i);
				touchProcessor.pointMoved(x, y, id);
			}
			gesture = touchProcessor.parseGesture();
			if (gesture != null) {
				// PApplet.print(gesture.id());
				event = new DOF6Event(prevEvent, touchProcessor.getCx(), touchProcessor.getCy(), 0, 0, 0, 0,
						MotionEvent.NO_MODIFIER_MASK,
						gesture.id());

				Action<?> a = (inputGrabber() instanceof InteractiveFrame) ? eyeProfile().handle((BogusEvent) event)
						: frameProfile().handle((BogusEvent) event);
				if (a == null)
					return;
				MotionAction dA = (MotionAction) a.referenceAction();
				if (dA == MotionAction.TRANSLATE_XYZ) {

				} else if (dA == MotionAction.TRANSLATE_XYZ_ROTATE_XYZ) {

				} else {
					if (prevEvent.id() != gesture.id()) {
						prevEvent = null;
					}
					switch (gesture) {
					case DRAG_ONE_ID:
					case DRAG_TWO_ID:
					case DRAG_THREE_ID:// Drag
						event = new DOF6Event(prevEvent,
								touchProcessor.getCx(),
								touchProcessor.getCy(),
								0,
								0,
								0,
								0,
								MotionEvent.NO_MODIFIER_MASK,
								gesture.id()
								);
						break;
					case OPPOSABLE_THREE_ID:
						event = new DOF6Event(prevEvent,
								x,
								y,
								0,
								0,
								0,
								0,
								MotionEvent.NO_MODIFIER_MASK,
								gesture.id()
								);
						break;
					case PINCH_TWO_ID:
					case PINCH_THREE_ID: // Pinch
						event = new DOF6Event(prevEvent,
								0,
								touchProcessor.getZ(),
								0,
								0,
								0,
								0,
								MotionEvent.NO_MODIFIER_MASK,
								gesture.id()
								);
						break;
					case TURN_TWO_ID:
					case TURN_THREE_ID: // Rotate
						//TODO needs testing
						turnOrientation = -1;
						if(inputGrabber() instanceof InteractiveFrame)
							turnOrientation = ((InteractiveFrame)inputGrabber()).isEyeFrame() ? -1 : 1;
						event = new DOF6Event(prevEvent,
								touchProcessor.getR() * turnOrientation,
								0,
								0,
								0,
								0,
								0,
								MotionEvent.NO_MODIFIER_MASK,
								gesture.id()
								);
						break;
					default:
						break;

					}
				}
				if (gesture != null) {
					if (prevEvent != null)
						handle(event);
					prevEvent = event.get();
				}
			}
		}
	}
}
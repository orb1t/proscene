
package remixlab.bias.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import remixlab.bias.branch.*;
import remixlab.bias.event.MotionEvent;

public class Agent {
	class Tuple {
		Grabber				g;
		Branch<?, ?>	b;

		public <E extends Enum<E>> Tuple(ActionGrabber<E> _g, Branch<E, ?> _a) {
			g = _g;
			b = _a;
		}

		public Tuple(Grabber _g) {
			g = _g;
			b = null;
		}

		public Tuple() {
			g = null;
			b = null;
		}
	}

	protected String							nm;

	protected List<Branch<?, ?>>	brnchs;
	protected List<Tuple>					tuples;

	protected Tuple								trackedGrabber;
	protected Tuple								defaultGrabber;
	protected boolean							agentTrckn;

	protected InputHandler				handler;

	/**
	 * Constructs an Agent with the given name and registers is at the given inputHandler.
	 */
	public Agent(InputHandler inputHandler, String name) {
		nm = name;
		tuples = new ArrayList<Tuple>();
		trackedGrabber = new Tuple();
		defaultGrabber = new Tuple();
		setTracking(true);
		handler = inputHandler;
		handler.registerAgent(this);
		brnchs = new ArrayList<Branch<?, ?>>();
	}

	/**
	 * @return Agents name
	 */
	public String name() {
		return nm;
	}

	/**
	 * Removes the grabber from the {@link #grabbers()} list.
	 * <p>
	 * See {@link #addGrabber(Grabber)} for details. Removing a grabber that is not in {@link #grabbers()} has no effect.
	 */
	public boolean removeGrabber(Grabber grabber) {
		for (Iterator<Tuple> it = tuples.iterator(); it.hasNext();) {
			Tuple t = it.next();
			if (t.g == grabber) {
				if (defaultGrabber() == t.g)
					setDefaultGrabber(null);
				it.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Clears the {@link #grabbers()} list.
	 */
	public void removeGrabbers() {
		setDefaultGrabber(null);
		tuples.clear();
	}

	public List<Grabber> grabbers() {
		List<Grabber> pool = new ArrayList<Grabber>();
		for (Tuple t : tuples)
			pool.add(t.g);
		return pool;
	}

	/**
	 * Returns true if the grabber is currently in the agents {@link #grabbers()} list.
	 * <p>
	 * When set to false using {@link #removeGrabber(Grabber)}, the handler no longer
	 * {@link remixlab.bias.core.Grabber#checkIfGrabsInput(BogusEvent)} on this grabber. Use {@link #addGrabber(Grabber)}
	 * to insert it * back.
	 */
	public boolean hasGrabber(Grabber grabber) {
		if (grabber == null)
			return false;
		return grabbers().contains(grabber);
	}

	/**
	 * Adds the grabber in the {@link #grabbers()}.
	 * <p>
	 * Use {@link #removeGrabber(Grabber)} to remove the grabber from the pool, so that it is no longer tested with
	 * {@link remixlab.bias.core.Grabber#checkIfGrabsInput(BogusEvent)} by the handler, and hence can no longer grab the
	 * agent focus. Use {@link #hasGrabber(Grabber)} to know the current state of the grabber.
	 */
	public boolean addGrabber(Grabber grabber) {
		if (grabber == null)
			return false;
		if (grabber instanceof ActionGrabber) {
			System.out.println("use addGrabber(G grabber, K actionAgent) instead");
			return false;
		}
		if (hasGrabber(grabber))
			return false;
		tuples.add(new Tuple(grabber));
		return true;
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> List<ActionGrabber<E>> grabbers(Branch<E, ?> branch) {
		List<ActionGrabber<E>> list = new ArrayList<ActionGrabber<E>>();
		for (Tuple t : tuples)
			if (t.b == branch)
				list.add((ActionGrabber<E>) t.g);
		return list;
	}

	public <E extends Enum<E>, K extends Branch<E, ?/* extends Action<E> */>, G extends ActionGrabber<E>> boolean
			addGrabber(G grabber, K branch) {
		// Overkill but feels safer ;)
		if (grabber == null || this.hasGrabber(grabber) || branch == null)
			return false;
		if (!hasBranch(branch))
			this.appendBranch(branch);
		tuples.add(new Tuple(grabber, branch));
		return true;
	}

	public List<Branch<?, ?>> branches() {
		return brnchs;
	}

	// keep!
	/*
	 * public <E extends Enum<E>, M extends Action<E>, C extends Action<E>> MotionBranch<E, MotionProfile<M>,
	 * ClickProfile<C>> addBranch( MotionProfile<M> m, ClickProfile<C> c, String name) { return new MotionBranch<E,
	 * MotionProfile<M>, ClickProfile<C>>(m, c, this, name); }
	 */

	public boolean appendBranch(Branch<?, ?> branch) {
		if (branch == null)
			return false;
		if (!brnchs.contains(branch)) {
			// TODO: priority seems not needed
			// this.brnchs.add(0, actionAgent);//priority
			this.brnchs.add(branch);
			return true;
		}
		return false;
	}

	public boolean hasBranch(Branch<?, ?> branch) {
		return brnchs.contains(branch);
	}

	public <E extends Enum<E>> void resetBranch(Branch<E, ?> branch) {
		for (Iterator<Tuple> it = tuples.iterator(); it.hasNext();) {
			Tuple t = it.next();
			if (t.b == branch) {
				if (defaultGrabber() == t.g)
					setDefaultGrabber(null);
				it.remove();
			}
		}
	}

	public void resetBranches() {
		if (defaultGrabber() instanceof ActionGrabber<?>)
			setDefaultGrabber(null);
		for (Iterator<Tuple> it = tuples.iterator(); it.hasNext();) {
			Tuple t = it.next();
			if (t.g instanceof ActionGrabber<?>)
				it.remove();
		}
	}

	public boolean pruneBranch(Branch<?, ?> branch) {
		if (brnchs.contains(branch)) {
			this.resetBranch(branch);
			this.brnchs.remove(branch);
			return true;
		}
		return false;
	}

	public void pruneBranches() {
		for (Branch<?, ?> branch : branches())
			resetBranch(branch);
		branches().clear();
	}

	/**
	 * Returns a detailed description of this Agent as a String.
	 */
	public String info() {
		String description = new String();
		description += name();
		description += "\n";
		description += "ActionAgents' info\n";
		int index = 1;
		for (Branch<?, ?> branch : branches()) {
			description += index;
			description += ". ";
			description += branch.info();
			index++;
		}
		return description;
	}

	/**
	 * Callback (user-space) event reduction routine. Obtains data from the outside world and returns a BogusEvent i.e.,
	 * reduces external data into a BogusEvent. Automatically call by the main event loop (
	 * {@link remixlab.bias.core.InputHandler#handle()}). See ProScene's Space-Navigator example.
	 * 
	 * @see remixlab.bias.core.InputHandler#handle()
	 */
	public BogusEvent feed() {
		return null;
	}

	/**
	 * Returns the {@link remixlab.bias.core.InputHandler} this agent is registered to.
	 */
	public InputHandler inputHandler() {
		return handler;
	}

	/**
	 * If {@link #isTracking()} is enabled and the agent is registered at the {@link #inputHandler()} then queries each
	 * object in the {@link #grabbers()} to check if the {@link remixlab.bias.core.Grabber#checkIfGrabsInput(BogusEvent)})
	 * condition is met. The first object meeting the condition will be set as the {@link #inputGrabber()} and returned.
	 * Note that a null grabber means that no object in the {@link #grabbers()} met the condition. A
	 * {@link #inputGrabber()} may also be enforced simply with {@link #setDefaultGrabber(Grabber)}.
	 * 
	 * @param event
	 *          to query the {@link #grabbers()}
	 * @return the new grabber which may be null.
	 * 
	 * @see #setDefaultGrabber(Grabber)
	 * @see #isTracking()
	 */
	public Grabber updateTrackedGrabber(BogusEvent event) {
		if (event == null || !inputHandler().isAgentRegistered(this) || !isTracking())
			return trackedGrabber();

		Grabber g = trackedGrabber();

		// We first check if tracked grabber remains the same
		if (g != null)
			if (g.checkIfGrabsInput(event))
				return trackedGrabber();

		trackedGrabber = null;
		for (Tuple t : tuples) {
			// take whatever. Here the first one
			if (t.g.checkIfGrabsInput(event)) {
				trackedGrabber = t;
				return trackedGrabber();
			}
		}
		return trackedGrabber();
	}

	public float[] sensitivities(MotionEvent event) {
		return new float[] { 1f, 1f, 1f, 1f, 1f, 1f };
	}

	/**
	 * Main agent method. Parses the {@link #inputGrabber()} using the proper branch to determine the user-defined action
	 * the {@link #inputGrabber()} should perform. Calls
	 * {@code inputHandler().enqueueEventTuple(new EventGrabberTuple(event, grabber()))}.
	 * <p>
	 * <b>Note</b> that the agent must be registered at the {@link #inputHandler()} for this method to take effect.
	 * 
	 * @see #inputGrabber()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean handle(BogusEvent event) {
		if (event == null || !handler.isAgentRegistered(this) || inputHandler() == null)
			return false;
		//if (event.isNull())	return false;
		if (event instanceof MotionEvent)
			((MotionEvent) event).modulate(sensitivities((MotionEvent) event));
		if (inputGrabber() != null) {
			if (inputGrabber() instanceof ActionGrabber<?>) {
				Tuple t = trackedGrabber() != null ?  trackedGrabber : defaultGrabber;
				if (t.b.handle((ActionGrabber) inputGrabber(), event) == null)
					return false;
			}
			return inputHandler().enqueueEventTuple(new EventGrabberTuple(event, inputGrabber()));
		}
		return false;
	}

	/**
	 * If {@link #trackedGrabber()} is non null, returns it. Otherwise returns the {@link #defaultGrabber()}.
	 * 
	 * @see #trackedGrabber()
	 */
	public Grabber inputGrabber() {
		if (trackedGrabber() != null)
			return trackedGrabber();
		else
			return defaultGrabber();
	}

	/**
	 * Returns true if {@code g} is the agent's {@link #inputGrabber()} and false otherwise.
	 */
	public boolean isInputGrabber(Grabber g) {
		return inputGrabber() == g;
	}

	/**
	 * Returns {@code true} if this agent is tracking its grabbers.
	 * <p>
	 * You may need to {@link #enableTracking()} first.
	 */
	public boolean isTracking() {
		return agentTrckn;
	}

	/**
	 * Enables tracking so that the {@link #inputGrabber()} may be updated when calling
	 * {@link #updateTrackedGrabber(BogusEvent)}.
	 * 
	 * @see #disableTracking()
	 */
	public void enableTracking() {
		setTracking(true);
	}

	/**
	 * Disables tracking.
	 * 
	 * @see #enableTracking()
	 */
	public void disableTracking() {
		setTracking(false);
	}

	/**
	 * Sets the {@link #isTracking()} value.
	 */
	public void setTracking(boolean enable) {
		agentTrckn = enable;
		if (!isTracking())
			trackedGrabber = null;
	}

	/**
	 * Calls {@link #setTracking(boolean)} to toggle the {@link #isTracking()} value.
	 */
	public void toggleTracking() {
		setTracking(!isTracking());
	}

	/**
	 * Returns the grabber set after {@link #updateTrackedGrabber(BogusEvent)} is called. It may be null.
	 */
	public Grabber trackedGrabber() {
		return trackedGrabber == null ? null : trackedGrabber.g;
	}

	/**
	 * Default {@link #inputGrabber()} returned when {@link #trackedGrabber()} is null and set with
	 * {@link #setDefaultGrabber(Grabber)}.
	 * 
	 * @see #inputGrabber()
	 * @see #trackedGrabber()
	 */
	public Grabber defaultGrabber() {
		return defaultGrabber == null ? null : defaultGrabber.g;
	}

	/**
	 * Sets the {@link #defaultGrabber()}
	 * 
	 * {@link #inputGrabber()}
	 */
	public boolean setDefaultGrabber(Grabber grabber) {
		if (grabber == null) {
			this.defaultGrabber = null;
			return true;
		}
		for (Tuple t : tuples)
			if (t.g == grabber) {
				this.defaultGrabber = t;
				return true;
			}
		return false;
	}

	/**
	 * Resets the {@link #defaultGrabber()}. Convenience function that simply calls: {@code setDefaultGrabber(null)}.
	 * 
	 * @see #setDefaultGrabber(Grabber)
	 */
	public void resetDefaultGrabber() {
		setDefaultGrabber(null);
	}
}

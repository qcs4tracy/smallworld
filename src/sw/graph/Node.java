package sw.graph;

public class Node extends Object {
	protected InfectionState state;

	public Node() {
		state = InfectionState.SUSCEPTIBLE;
	}

	//hashCode might be override
	// public int hashCode() {}

	public InfectionState getInfectionState() { return state; }
	public void setInfectionState(InfectionState state) { this.state = state; }
}

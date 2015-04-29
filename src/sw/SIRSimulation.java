package sw;

import java.lang.Runnable;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.*;

import sw.graph.Graph;
import sw.graph.Node;
import sw.graph.InfectionState;
import sw.ui.Window;

public class SIRSimulation implements Runnable {

	protected Graph graph;
	protected boolean running;
	long timeInterval;
	double alpha;
	double gamma;
	int infectPeriod;
	Window window;

	//in the Window class, only one thread will run at any time, ok to put the array list here
	ArrayList<Node> infected = new ArrayList<Node>();
	ArrayList<Node> susceptibles = new ArrayList<Node>();

	/**
	 * Use a hash table to keep track of all infected people and, when it becomes empty, the epidemic is done.
	 * Initialize the hash table with bucket array of size # of nodes for performance reason (few collision)
	 */
	Hashtable<Node, Integer> infectDaysLeft;

	public SIRSimulation(Graph graph, long timeInterval, double alpha, double gamma, Window window) {
		this.graph = graph;
		this.timeInterval = timeInterval;
		this.alpha = alpha;
		this.gamma = gamma;
		this.window = window;
		infectDaysLeft = new Hashtable<Node, Integer>(graph.getNumberNodes());
		infectPeriod = (int)Math.round(1.0/gamma);
	}


	public void reset() {
		for(Node n : graph.getNodes()) {
			n.setInfectionState(InfectionState.SUSCEPTIBLE);
		}
		infected.clear();
		infectDaysLeft.clear();
		susceptibles.clear();
		susceptibles.addAll(graph.getNodes());
	}

	public void terminate() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	private void _elapseOneDay(Hashtable<Node, Integer> infDayLeft) {

		//it is only safe to modify collection instance when traversing using iterator
		for (Iterator<Map.Entry<Node, Integer>> itr = infDayLeft.entrySet().iterator(); itr.hasNext();) {
			Map.Entry<Node, Integer> ent = itr.next();
			ent.setValue(ent.getValue() - 1);
			//if the counter become 0 means the node has recovered
			if (ent.getValue() <= 0) {
				ent.getKey().setInfectionState(InfectionState.RECOVERED);
				//remove it the infected group
				infected.remove(ent.getKey());
				itr.remove();
			}
		}
	}

	//make # of infectious cases at beginning be configurable by the user
	public void addInfectedByN(int num) {
		//reset and randomly select n infected person
		reset();
		for (int i = 0; i < num; i++) {
			Node n = susceptibles.get(randInt(0,susceptibles.size()-1));
			susceptibles.remove(n);
			n.setInfectionState(InfectionState.INFECTED);
			//first person get infected, add it to the infecDaysLeft Hash Table
			infectDaysLeft.put(n, infectPeriod);
			infected.add(n);
		}
		//refresh UI
		window.onTimeStep();
	}

	//add one at a time
	public void addInfected(Node n) {
		n.setInfectionState(InfectionState.INFECTED);
		susceptibles.remove(n);
		infectDaysLeft.put(n, infectPeriod);
		infected.add(n);
	}


	private void _outputStat() {
		int s = susceptibles.size();
		int i = infected.size();
		int r = graph.getNumberNodes() - s - i;
		System.out.println("[susceptible: " + s + ", infected: " + i + ", recovered: " + r + "]");
	}

	public void run() {

		running = true;
		int day = 1;
		System.out.println("Running simulation...");
		System.out.println("Parameters:\n" + "\talpha: " + alpha + "\n" + "\tgamma:" + gamma + "\n" +
			"\tinitial infected cases:" + infected.size());

		while(running) {

			System.out.print("Day: " + day + " ");

			ArrayList<Node> newlyInfected = new ArrayList<Node>();

			//TODO:should we consider contact rate?
			for(Node n : infected) {
				for(Node neighbor : graph.getNeighbors(n)) {
					if(neighbor.getInfectionState() == InfectionState.SUSCEPTIBLE) {
						if(Math.random() < alpha) {
							neighbor.setInfectionState(InfectionState.INFECTED);
							//add it to hash table to keep track of its remaining infectious days
							//fix +1 cause the newly infected infectious period counter will be decremented immediately
							infectDaysLeft.put(neighbor, infectPeriod+1);
							newlyInfected.add(neighbor);
							susceptibles.remove(neighbor);
						}
					}
				}
			}

			//someone might get recovered
			_elapseOneDay(infectDaysLeft);
			infected.addAll(newlyInfected);

			//statistics
			_outputStat();
			//refresh the UI
			window.onTimeStep();

			//no one is infected now, simulation is done
			if (infected.isEmpty())
				break;

			try {
				Thread.sleep(timeInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			day++;
		}

		running = false;
		window.onSimulationEnd();
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
}

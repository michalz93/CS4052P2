package model;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * **/
public class State {
    private boolean init;
    private String name;
    private String [] label;
    private List<State> predecessors = new ArrayList<>();
	
    /**
     * Is state an initial state
     * @return boolean init 
     * */
    public boolean isInit() {
	return init;
    }
	
    /**
     * Returns the name of the state
     * @return String name 
     * */
    public String getName() {
	return name;
    }
	
    /**
     * Returns the labels of the state
     * @return Array of string labels
     * */
    public String[] getLabel() {
	return label;
    }

    public List<State> getPredecesors() {
        return predecessors;
    }

    public void addPredecessor(State s) {
        predecessors.add(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State other = (State) o;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
	
}

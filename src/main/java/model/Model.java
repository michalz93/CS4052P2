package model;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

/**
 * A model is consist of states and transitions
 **/
public class Model {
    State[] states;
    Transition[] transitions;

    public static Model parseModel(String filePath) throws IOException {
        Gson gson = new Gson();
        Model model = gson.fromJson(new FileReader(filePath), Model.class);
        for (Transition t : model.transitions) {
            State source = model.getState(t.getSource());
            State target = model.getState(t.getTarget());
            if (source != null && target != null) {
                target.addPredecessor(source);
                //add successor/transition;
            }
            System.out.println(t);
            ;
        }
        return model;
    }

    /**
     * Returns the list of the states
     * 
     * @return list of state for the given model
     */
    public State[] getStates() {
        return states;
    }

    public State[] getInitialStates() {
        return java.util.Arrays.stream(states)
                .filter(s -> s.isInit())
                .toArray(State[]::new);
    }

    public State getState(String id) {
        for (State state : states) {
            if (id.equals(state.getName())) {
                return state;
            }
        }
        return null;
    }

    /**
     * Returns the list of transitions
     * 
     * @return list of transition for the given model
     */
    public Transition[] getTransitions() {
        return transitions;
    }

    public Set<String> getAllActions() {
        Set<String> actions = new HashSet<>();

        if (transitions == null) {
            return actions;
        }

        for (Transition t : transitions) {
            if (t.getActions() != null) {
                for (String action : t.getActions()) {
                    actions.add(action);
                }
            }
        }

        return actions;
    }

}

package modelChecker;

import formula.stateFormula.*;
import formula.pathFormula.*;
import model.*;

import java.nio.file.Path;
import java.util.*;

public class SimpleModelChecker implements ModelChecker {
    private List<String> trace = new ArrayList<>();

    @Override
    public boolean check(Model model, StateFormula query) {
        System.out.println("Query: " + query);
        State[] initialStates = model.getInitialStates();
        

        for (State s : initialStates) {
            System.out.println(s.getName());
            trace.clear();
            if (!checkState(model, s , query)) {
                trace.add(s.getName());
                return false;
            }
            trace.add(s.getName());
        }
        
        
        return true;
    }

    public boolean checkState(Model model, State state ,StateFormula query) {
        if (query instanceof BoolProp) {
            BoolProp bp = (BoolProp) query;
            return bp.value;
        } else if (query instanceof Not) {
            Not not = (Not) query;
            StateFormula st = not.stateFormula;
            return !checkState(model, state, st);
        } else if (query instanceof And) {
            And and = (And) query;
            StateFormula left = and.left;
            StateFormula right = and.right;
            return (checkState(model, state, left) && checkState(model, state, right));
        } else if (query instanceof Or) {
            Or or = (Or) query;
            StateFormula left = or.left;
            StateFormula right = or.right;
            return (checkState(model, state, left) || checkState(model, state, right));
        } else if (query instanceof AtomicProp) {
            AtomicProp atom = (AtomicProp) query;
            String label = atom.label;
            String[] stateLabels = state.getLabel();

            for (String stateLabel : stateLabels) {
                if (label.equals(stateLabel)) {
                    return true;
                }
            }

            return false;
        } else if (query instanceof ForAll) {
            ForAll forAll = (ForAll) query;
            Transition[] transitions = model.getTransitions();

            for (Transition transition: transitions) {
                if (transition.getSource().equals(state.getName())) {
                    PathResult result = checkPathFormula(model, state, model.getState(transition.getTarget()), forAll.pathFormula);
                    if (!result.holds) {
                        trace.add(model.getState(transition.getTarget()).getName());
                        return false;
                    }
                }
            }

            return true;
        } else if (query instanceof ThereExists) {
            ThereExists thereExists = (ThereExists) query;
            Transition[] transitions = model.getTransitions();

            for (Transition transition: transitions) {
                if (transition.getSource().equals(state.getName())) {
                    PathResult result = checkPathFormula(model, state, model.getState(transition.getTarget()), thereExists.pathFormula);
                    if (result.holds) {
                        //trace.add(model.getState(transition.getTarget()).getName());
                        trace.addAll(result.pathTrace);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public PathResult checkPathFormula(Model model, State source, State target, PathFormula pathFormula) {
        if (pathFormula instanceof Next) {
            Next next = (Next) pathFormula;
            boolean holds = checkState(model, target, next.stateFormula);

            return new PathResult(holds, List.of(target.getName()));
        } else if (pathFormula instanceof Until) {
            Until until = (Until) pathFormula;

            if (until.getRightActions().isEmpty()) {
                boolean holds = checkState(model, source, until.right);
                return new PathResult(holds, null);
            }

            if (until.getLeftActions().isEmpty()) {
                boolean currentState = checkState(model, source, until.left);

                if (currentState) {
                    boolean holds = checkState(model, target, until.right);
                    return new PathResult(holds, null);
                } else {
                    return new PathResult(false, null);
                }
            }

            List<State> E = new ArrayList<>();
            Map<State, State> parents = new HashMap<>();
            for (State state : model.getStates()) {
                if (checkState(model, state, until.right)) {
                    E.add(state);
                    parents.put(state, null);
                }
            }
            if (E.size() == 0) return new PathResult(false, null);
 
            List<State> T = new ArrayList<>(E);
            while (E.size() > 0) {
                State sPrime = E.get(0);
                E.remove(0);
                for (State s : sPrime.getPredecesors()) {
                    if (checkState(model, s, until.left) && !(T.contains(s))) {
                        E.add(s);
                        T.add(s);

                        parents.put(s, sPrime);

                        if (T.contains(source)){ 
                            List<String> pathTrace = new ArrayList<>();

                            State cur = source;
                            while (cur != null) {
                                cur = parents.get(cur);
                                if (cur != null) pathTrace.add(cur.getName());
                            }

                            Collections.reverse(pathTrace);
                            return new PathResult(true, pathTrace);
                        }
                    }
                }
            }

            
            return new PathResult(false, null);
        }
        return new PathResult(false, null);
    }

    @Override
    public String[] getTrace() {
        if (trace == null) return null;
        return trace.toArray(new String[0]);
    }

    
    public String getBetterTrace() {
        if (trace == null || trace.isEmpty()) return null;

        List<String> copy = new ArrayList<>(trace);
        Collections.reverse(copy);

        return String.join("-", copy);
    }


}

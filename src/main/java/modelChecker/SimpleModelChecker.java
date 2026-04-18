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
            System.out.println("This is initial state");
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
                    System.out.println("transition");
                    PathResult result = checkPathFormula(model, state, model.getState(transition.getTarget()), thereExists.pathFormula);
                    if (result.holds) {
                        System.out.println("Hello");
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

            System.out.println("Until left side: " + until.getLeftActions());
            System.out.println("Until right side: " + until.getRightActions());

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

            System.out.println("None is empty!");

            List<State> E = new ArrayList<>();
            Map<State, State> parents = new HashMap<>();
            Map<State, String> parentAction = new HashMap<>();;
            for (State state : model.getStates()) {
                if (checkState(model, state, until.right)) {
                    E.add(state);
                    parents.put(state, null);
                    parentAction.put(state, null);
                }
            }
            if (E.size() == 0) return new PathResult(false, null);

            System.out.println("There are valid end states!");
            
            List<State> endStates = new ArrayList<>(E);
            List<State> T = new ArrayList<>(E);
            while (E.size() > 0) {
                State sPrime = E.get(0);
                E.remove(0);
                Set<String> allowedActions;
                if (endStates.contains(sPrime)) {
                    allowedActions = until.getRightActions();
                } else {
                    allowedActions = until.getLeftActions();
                }
                System.out.println("Allowed actions: " + allowedActions + " and target state is: " + sPrime.getName());

                for (Transition transition : model.getTransitions()) {
                    System.out.println("Transtition!");
                    State s = model.getState(transition.getSource());
                    String allowedAction = transition.getAllowedAction(allowedActions);
                    System.out.println(transition.getTarget().equals(sPrime.getName()));
                    System.out.println(!(T.contains(s)) );
                    System.out.println(checkState(model, s, until.left));
                    System.out.println((allowedAction != null));
                    if (transition.getTarget().equals(sPrime.getName()) 
                        && !(T.contains(s)) 
                        && checkState(model, s, until.left) 
                        && (allowedAction != null)
                    )  {
                        System.out.println("Doing something");
                        E.add(s);
                        T.add(s);

                        parents.put(s, sPrime);
                        parentAction.put(s, allowedAction);

                        if (T.contains(source)){ 
                            List<String> pathTrace = new ArrayList<>();

                            State cur = source;
                            while (parents.get(cur) != null) {
                                String action = parentAction.get(cur);
                                cur = parents.get(cur);
                                if (action != null) pathTrace.add(action);
                                if (cur != null) pathTrace.add(cur.getName());
                            }

                            Collections.reverse(pathTrace);
                            return new PathResult(true, pathTrace);
                        }
                    }
                }
                /*for (State s : sPrime.getActionPredecessors(allowedActions, model.getTransitions())) {
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
                }*/
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

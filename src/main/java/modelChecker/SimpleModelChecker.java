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
            PathResult result = checkState(model, s , query);
            if (!result.holds) {
                //trace.add(s.getName());
                trace.addAll(result.pathTrace);
                return false;
            }
            trace.addAll(result.pathTrace);
        }
        
        
        return true;
    }

    public PathResult checkState(Model model, State state ,StateFormula query) {
        List<String> checkStateTrace = new ArrayList<>();
        if (query instanceof BoolProp) {
            BoolProp bp = (BoolProp) query;
            checkStateTrace.add(state.getName());
            return new PathResult(bp.value, checkStateTrace);
        } else if (query instanceof Not) {
            Not not = (Not) query;
            StateFormula st = not.stateFormula;
            PathResult notNegated = checkState(model, state, st);
            notNegated.holds = !notNegated.holds;
            return notNegated;
        } else if (query instanceof And) {
            And and = (And) query;
            StateFormula left = and.left;
            StateFormula right = and.right;
            PathResult leftSide = checkState(model, state, left);
            PathResult rightSide = checkState(model, state, right);
            return (new PathResult((leftSide.holds && rightSide.holds), checkStateTrace));
        } else if (query instanceof Or) {
            Or or = (Or) query;
            StateFormula left = or.left;
            StateFormula right = or.right;
            PathResult leftSide = checkState(model, state, left);
            PathResult rightSide = checkState(model, state, right);
            return (new PathResult((leftSide.holds || rightSide.holds), checkStateTrace));
        } else if (query instanceof AtomicProp) {
            AtomicProp atom = (AtomicProp) query;
            String label = atom.label;
            String[] stateLabels = state.getLabel();
            checkStateTrace.add(state.getName());

            for (String stateLabel : stateLabels) {
                if (label.equals(stateLabel)) {
                    return new PathResult(true, checkStateTrace);
                }
            }

            return new PathResult(false, checkStateTrace);
        } else if (query instanceof ForAll) {
            ForAll forAll = (ForAll) query;
            Transition[] transitions = model.getTransitions();

            for (Transition transition: transitions) {
                if (transition.getSource().equals(state.getName())) {
                    System.out.println("transition");
                    PathResult result = checkPathFormula2(model, state, model.getState(transition.getTarget()), forAll.pathFormula, true);
                    if (!result.holds) {
                        System.out.println("Hello");
                        System.out.println(result.pathTrace);
                        checkStateTrace.addAll(result.pathTrace);
                        if (checkStateTrace.isEmpty() || 
                            !checkStateTrace.get(checkStateTrace.size() - 1).equals(state.getName())) {
                            checkStateTrace.add(state.getName());
                        }
                        System.out.println("CheckStackTrace: " + checkStateTrace);
                        return new PathResult(false, checkStateTrace);
                    }
                }
            }

            return new PathResult(true, checkStateTrace);
        } else if (query instanceof ThereExists) {
            ThereExists thereExists = (ThereExists) query;
            Transition[] transitions = model.getTransitions();

            for (Transition transition: transitions) {
                if (transition.getSource().equals(state.getName())) {
                    System.out.println("transition");
                    PathResult result = checkPathFormula2(model, state, model.getState(transition.getTarget()), thereExists.pathFormula, false);
                    if (result.holds) {
                        System.out.println("Hello");
                        System.out.println("CheckStackTrace: " + checkStateTrace);
                        System.out.println("result.pathTrace: " + result.pathTrace);
                        checkStateTrace.addAll(result.pathTrace);
                        System.out.println("CheckStackTrace: " + checkStateTrace);
                        if (checkStateTrace.isEmpty() || 
                            !checkStateTrace.get(checkStateTrace.size() - 1).equals(state.getName())) {
                            checkStateTrace.add(state.getName());
                        }
                        System.out.println("CheckStackTrace: " + checkStateTrace);
                        return new PathResult(true, checkStateTrace);
                    }
                }
            }
        }

        return new PathResult(false, checkStateTrace);
    }

    public PathResult checkPathFormula2(Model model, State source, State target, PathFormula pathFormula, boolean allCorrect) {
        List<String> checkStateTrace = new ArrayList<>();
        checkStateTrace.add(target.getName());
        
        if (pathFormula instanceof Next) {
            Next next = (Next) pathFormula;
            System.out.println("This should be printed first and last" + target.getName());
            PathResult pathResult = checkState(model, target, next.stateFormula);

            return pathResult;
        } else if (pathFormula instanceof Until) {
            Until until = (Until) pathFormula;

            System.out.println("Until left side: " + until.getLeftActions());
            System.out.println("Until right side: " + until.getRightActions());

            if (until.getRightActions().isEmpty()) {
                boolean holds = checkState(model, source, until.right).holds;
                return new PathResult(holds, null);
            }

            if (until.getLeftActions().isEmpty()) {
                boolean currentState = checkState(model, source, until.left).holds;

                if (currentState) {
                    boolean holds = checkState(model, target, until.right).holds;
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
                if (checkState(model, state, until.right).holds) {
                    E.add(state);
                    parents.put(state, null);
                    parentAction.put(state, null);
                }
            }
            if (E.size() == 0) return new PathResult(false, null);

            System.out.println("There are valid end states!");
            
            List<State> endStates = new ArrayList<>(E);
            List<State> T = new ArrayList<>(E);

            if (!allCorrect) {
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
                        System.out.println(checkState(model, s, until.left).holds);
                        System.out.println((allowedAction != null));
                        if (transition.getTarget().equals(sPrime.getName()) 
                            && !(T.contains(s)) 
                            && checkState(model, s, until.left).holds 
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
                                if (!allCorrect) return new PathResult(true, pathTrace);
                            }
                        }
                    }
                }

                return new PathResult(false, null);
            } else return dfsUntil(model, source, until, new HashSet<State>());
        } else if (pathFormula instanceof Eventually) {
            Eventually eventually = (Eventually) pathFormula;
            Until newFormula = new Until(new BoolProp(true), eventually.stateFormula, eventually.getLeftActions(), eventually.getRightActions());
            return checkPathFormula2(model, source, target, newFormula, allCorrect);
        } else if (pathFormula instanceof Always) {
            Always always = (Always) pathFormula;
            // This ensures that we don't waste time working on paths, if the always fails in the current state.
            PathResult doesItHoldInThisState = checkState(model, source, always.stateFormula);
            if (!doesItHoldInThisState.holds) {
                return doesItHoldInThisState;
            }
            Set<String> actions = always.getActions();
            if (actions.size() == 0) actions = model.getAllActions();

            Not not = new Not(always.stateFormula);
            Until newFormula = new Until(new BoolProp(true), not, actions, model.getAllActions());

            if (allCorrect) {
                ThereExists thereExists = new ThereExists(newFormula);
                Not negated = new Not(thereExists);
                System.out.println("Transformed: " + negated);
                return checkState(model, source, negated);
            } else {
                ForAll forAll = new ForAll(newFormula);
                Not negated = new Not(forAll);
                System.out.println("Query: " + negated);
                return checkState(model, source, negated);
            }
        }
        
        return new PathResult(false, null);
    }

    @Override
    public String[] getTrace() {
        if (trace == null) return null;
        return trace.toArray(new String[0]);
    }

    public PathResult dfsUntil(Model model, State current, Until until, Set<State> visited) {
        System.out.println(current.getName());
        if (visited.contains(current)) return new PathResult(true, null);
        visited.add(current);

        List<String> dfsTrace = new ArrayList<>();

        if (checkState(model, current, until.right).holds) return new PathResult(true, null);

        if (!checkState(model, current, until.left).holds) {
            dfsTrace.add(current.getName());
            System.out.println("It ends here");
            return new PathResult(false, dfsTrace);
        }

        boolean hasSuccessor = false;

        for (Transition t : model.getTransitions()) {
            if (t.getSource().equals(current.getName())) {
                hasSuccessor = true;

                State next = model.getState(t.getTarget());

                if (checkState(model, next, until.right).holds) {
                    boolean canTransit = t.hasAllowedAction(until.getRightActions());
                    if (canTransit) {
                        continue;
                    } else {
                        // If there is a state p we can reach, but not by action in B then it fails.
                        dfsTrace.add(t.getTarget());
                        dfsTrace.add(t.getActions()[0]);
                        dfsTrace.add(current.getName());
                        System.out.println("It ends here2");
                        return new PathResult(false, dfsTrace);
                    }
                } else {
                    String action = t.getAllowedAction(until.getLeftActions());
                    if (action == null) {
                        // can return any action i guess? or lack of actions?
                        System.out.println("It ends here4");
                        dfsTrace.add(t.getTarget());
                        dfsTrace.add(t.getActions()[0]);
                        dfsTrace.add(current.getName());
                        
                        return new PathResult(false, dfsTrace); 
                    }
                    dfsTrace.add(action);
                }

                PathResult result = dfsUntil(model, next, until, new HashSet<>(visited));

                if (!result.holds) {
                    System.out.println("It ends here3");
                    List<String> trace = new ArrayList<>(result.pathTrace);
                    trace.addAll(dfsTrace);
                    return new PathResult(false, trace);

                }
            }
        }

        dfsTrace.add(current.getName());
        if (!hasSuccessor) return new PathResult(false, dfsTrace);

        System.out.println("It ends here7");
        return new PathResult(true, null);
    }

    
    public String getBetterTrace() {
        if (trace == null || trace.isEmpty()) return null;

        List<String> copy = new ArrayList<>(trace);
        Collections.reverse(copy);

        return String.join("-", copy);
    }


}

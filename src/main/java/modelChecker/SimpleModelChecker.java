package modelChecker;

import formula.stateFormula.*;
import formula.pathFormula.*;
import model.*;

import java.nio.file.Path;
import java.util.*;

public class SimpleModelChecker implements ModelChecker {
    private List<List<String>> traces = new ArrayList<>();

    @Override
    public boolean check(Model model, StateFormula query) {
        System.out.println("Query: " + query);
        State[] initialStates = model.getInitialStates();

        traces.clear();
        

        for (State s : initialStates) {
            System.out.println("\n This is initial state: " + s.getName());
            PathResult result = checkState(model, s , query);
            System.out.println("\n This is trace: " + result.pathTrace);
            if (!result.holds) {
                traces.add(result.pathTrace);
                return false;
            }
            traces.add(result.pathTrace);
        }
        
        return true;
    }

    public PathResult checkState(Model model, State state ,StateFormula query) {
        //System.out.println("Check state for state: " + state.getName() + " and query: " + query);
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
            if (leftSide.holds) {
                return (new PathResult(true, checkStateTrace));
            }
            PathResult rightSide = checkState(model, state, right);
            return (new PathResult((leftSide.holds || rightSide.holds), checkStateTrace));
        } else if (query instanceof AtomicProp) {
            AtomicProp atom = (AtomicProp) query;
            String label = atom.label;
            String[] stateLabels = state.getLabel();
            if (checkStateTrace.isEmpty() || 
                    !checkStateTrace.get(checkStateTrace.size() - 1).equals(state.getName())) {
                    checkStateTrace.add(state.getName());
            }

            for (String stateLabel : stateLabels) {
                if (label.equals(stateLabel)) {
                    return new PathResult(true, checkStateTrace);
                }
            }

            return new PathResult(false, checkStateTrace);
        } else if (query instanceof ForAll) {
            ForAll forAll = (ForAll) query;
            PathResult result = checkPathFormula2(model, state, forAll.pathFormula, true);
            if (!result.holds) {
                /*System.out.println("for all found counterexample");
                System.out.println("for all adding " + result.pathTrace);*/
                checkStateTrace.addAll(result.pathTrace);
                if (checkStateTrace.isEmpty() || 
                    !checkStateTrace.get(checkStateTrace.size() - 1).equals(state.getName())) {
                    checkStateTrace.add(state.getName());
                }
                //System.out.println("CheckStackTrace: " + checkStateTrace);
                return new PathResult(false, checkStateTrace);
            } 

            return new PathResult(true, checkStateTrace); // Don't care about trace here
        } else if (query instanceof ThereExists) {
            ThereExists thereExists = (ThereExists) query;
            PathResult result = checkPathFormula2(model, state, thereExists.pathFormula, false);
            if (result.holds) {
                /*System.out.println("there exists found path");
                System.out.println("exist adding " + result.pathTrace);*/
                checkStateTrace.addAll(result.pathTrace);
                if (checkStateTrace.isEmpty() || 
                    !checkStateTrace.get(checkStateTrace.size() - 1).equals(state.getName())) {
                    checkStateTrace.add(state.getName());
                }
                //System.out.println("CheckStackTrace: " + checkStateTrace);
                return new PathResult(true, checkStateTrace);
            }
            return new PathResult(false, checkStateTrace); // We don't care about trace
        }

        return new PathResult(false, checkStateTrace);
    }

    // Check paths from given state now.
    public PathResult checkPathFormula2(Model model, State source, PathFormula pathFormula, boolean allCorrect) {
        //System.out.println("Check path formula: " +  pathFormula);
        List<String> checkStateTrace = new ArrayList<>();
        
        if (pathFormula instanceof Next) {
            Next next = (Next) pathFormula;
            Until newFormula = new Until(new BoolProp(true), next.stateFormula, new HashSet<String>(), next.getActions());
            return checkPathFormula2(model, source, newFormula, allCorrect);
        } else if (pathFormula instanceof Until) {
            Until until = (Until) pathFormula;

            /*System.out.println("Until left side: " + until.getLeftActions());
            System.out.println("Until right side: " + until.getRightActions());*/

            // Can't take final transition, so effectively it just checks Phi 2 for this state.
            if (until.getRightActions().isEmpty()) {
                //System.out.println("Can't make any transitions, formula must hold in this state");
                PathResult result = checkState(model, source, until.right);
                return result;
            }

            /*
            // Can't take any transitions except final, so Phi 1 must hold and next state has to hold Phi 2 and be achievable by legal action from B.
            // If can't make any left transitions than this is just next.
            // TODO For all / exist */
            if (until.getLeftActions().isEmpty()) {
                // This means that only B transition can happen once, 
                PathResult currentState = checkState(model, source, until.left);

                // If it doesnt hold in current state then until will fail. For next, until.left will simply be TRUE.
                if (!currentState.holds) {
                    return currentState;
                }

                boolean holdsAtLeastOnce = false;
                for (Transition transition : model.getTransitions()) {
                    if (transition.getSource().equals(source.getName())) {
                        //System.out.println("This should be printed first and last" + transition.getTarget());
                        PathResult pathResult = checkState(model, model.getState(transition.getTarget()), until.right);
                        String allowedAction = transition.getAllowedAction(until.getRightActions());
                        List<String> newTrace = new ArrayList<>(pathResult.pathTrace);
                        
                        if (pathResult.holds) holdsAtLeastOnce = true;

                        if (allCorrect) {
                            if (!pathResult.holds) { //TODO Add verification of transition action
                                //checkStateTrace.add(target.getName());
                                String anyAction = transition.getActions()[0];
                                if (anyAction != null) newTrace.add(anyAction);
                                //System.out.println("Check path formula: " +  pathFormula + "doesn't hold");
                                return new PathResult(false, newTrace);
                            } 
                            String illegalAction = transition.getIllegalAction(until.getRightActions());
                            if (illegalAction != null) {
                                newTrace.add(transition.getTarget());
                                newTrace.add(illegalAction);
                                //System.out.println("Check path formula: " +  pathFormula + "doesn't hold due to illegal action");
                                return new PathResult(false, newTrace);
                            }
                        } else {
                            if (pathResult.holds && allowedAction != null) {
                                newTrace.add(allowedAction);
                                //System.out.println("Check path formula: " +  pathFormula + "holds");
                                return new PathResult(pathResult.holds, newTrace);
                            }
                        }
                    }
                }

                // If at least one transition was valid will return that there exists at least 1 path.
                if (holdsAtLeastOnce) {
                    //System.out.println("Check path formula: " +  pathFormula + "holds at least once");
                    return new PathResult(true, checkStateTrace);
                }

                // If there is no valid transition then it returns false.
                //System.out.println("Check path formula: " +  pathFormula + "doesn't hold");
                return new PathResult(false, checkStateTrace);
            }

            Queue<State> E = new ArrayDeque<>();
            Map<State, State> parents = new HashMap<>();
            Map<State, String> parentAction = new HashMap<>();
            for (State state : model.getStates()) {
                if (checkState(model, state, until.right).holds) {
                    E.add(state);
                    parents.put(state, null);
                    parentAction.put(state, null);
                }
            }
            if (E.size() == 0) return new PathResult(false, checkStateTrace); // Empty trace, we don't need trace in exist, might be issue for forall
            

            if (!allCorrect) {
                Set<State> T = new HashSet<>();
                Set<State> almostEndStates = new HashSet<>();

                while (E.size() > 0) {
                    State sPrime = E.poll();
                    //System.out.println("Allowed actions: " + until.getRightActions() + " and target state is: " + sPrime.getName());

                    for (Transition transition : model.getTransitions()) {
                        //System.out.println("Transition from " + transition.getSource() + " to " + transition.getTarget());

                        if (!transition.getTarget().equals(sPrime.getName())) continue;

                        State s = model.getState(transition.getSource());

                        String allowedAction = transition.getAllowedAction(until.getRightActions());

                        if (allowedAction == null) continue;

                        // Transitions are faster to verify.
                        if (!checkState(model, s, until.left).holds) continue; 
                        //System.out.println("Added state: " + s + " to second target");
                        T.add(s);
                        almostEndStates.add(s);

                        parents.put(s, sPrime);
                        parentAction.put(s, allowedAction);
                    }
                }

                if (T.contains(source)) {
                    //System.out.println("returning here");
                    List<String> pathTrace = new ArrayList<>();
                    String action = parentAction.get(source);
                    State parent = parents.get(source);
                    
                    if (parent != null) pathTrace.add(parent.getName());
                    if (action != null) pathTrace.add(action);
                    return new PathResult(true, pathTrace);
                }

                
                E = new ArrayDeque<>(T);

                //System.out.println("Now regular search with: " + T);
                while (E.size() > 0) {
                    State sPrime = E.poll();
                    //System.out.println("Allowed actions: " + until.getLeftActions() + " and target state is: " + sPrime.getName());

                    for (Transition transition : model.getTransitions()) {
                        //System.out.println("Transition from " + transition.getSource() + " to " + transition.getTarget());

                        if (!transition.getTarget().equals(sPrime.getName())) continue;

                        State s = model.getState(transition.getSource());

                        if (T.contains(s)) continue;

                        String allowedAction = transition.getAllowedAction(until.getLeftActions());

                        if (allowedAction == null) continue;

                        if (!checkState(model, s, until.left).holds) continue; 

                        E.add(s);
                        T.add(s);

                        parents.put(s, sPrime);
                        parentAction.put(s, allowedAction);
                        
                        if (T.contains(source)){ 
                            List<String> pathTrace = new ArrayList<>();
                            Set<State> seen = new HashSet<>();

                            //System.out.println("parents: " + parents + " parentsActions: " + parentAction);

                            State cur = source;
                            while (cur != null && !seen.contains(cur)) {
                                seen.add(cur);
                                
                                String action = parentAction.get(cur);
                                State parent = parents.get(cur);
                                /*System.out.println(almostEndStates.contains(cur));
                                System.out.println("Cur and action" + cur + " " + action);
                                System.out.println("current thats added " + cur);*/
                                if (action != null) pathTrace.add(action);
                                if (parent != null) pathTrace.add(parent.getName());

                                cur = parent;
                            }
                            

                            Collections.reverse(pathTrace);
                            return new PathResult(true, pathTrace);
                        }
                    }
                }

                //System.out.println("Check path formula: " +  pathFormula + " returns failure for exist search");
                return new PathResult(false, checkStateTrace); //some trace should be here
            } else return dfsUntil(model, source, until, new HashSet<State>(), true, source, null);
        } else if (pathFormula instanceof Eventually) {
            Eventually eventually = (Eventually) pathFormula;
            Until newFormula = new Until(new BoolProp(true), eventually.stateFormula, eventually.getLeftActions(), eventually.getRightActions());
            return checkPathFormula2(model, source, newFormula, allCorrect);
        } else if (pathFormula instanceof Always) {
            Always always = (Always) pathFormula;
            // This ensures that we don't waste time working on paths, if the always fails in the current state.
            PathResult doesItHoldInThisState = checkState(model, source, always.stateFormula);
            if (!doesItHoldInThisState.holds) {
                return doesItHoldInThisState;
            }

            Not not = new Not(always.stateFormula);
            Until newFormula = new Until(new BoolProp(true), not, model.getAllActions(), model.getAllActions());

            if (allCorrect) {
                ThereExists thereExists = new ThereExists(newFormula);
                Not negated = new Not(thereExists);
                //System.out.println("Transformed: " + negated);
                return checkState(model, source, negated);
            } else {
                ForAll forAll = new ForAll(newFormula);
                Not negated = new Not(forAll);
                //System.out.println("Query: " + negated);
                return checkState(model, source, negated);
            }
        }
        
        return new PathResult(false, checkStateTrace); //TODO something
    }

    @Override
    public String[] getTrace() {
        if (traces == null) return null;
        return traces.toArray(new String[0]);
    }

    public PathResult dfsUntil(Model model, State current, Until until, Set<State> visited, boolean inital, State initialState, Transition transition) {
        //System.out.println("dfsSearch, current state:" + current.getName());
        visited.add(current);

        List<String> dfsTrace = new ArrayList<>();
        

        if (checkState(model, current, until.right).holds) {
            if (inital) {
                //return new PathResult(false, null); // Already holds at the start, unspecified for now
            } else {
                if (until.getRightActions().size() > 0) {
                    if (transition != null && transition.hasAllowedAction(until.getRightActions())) {
                    // Ensures you don't return true just because you are in correct state, you need to go through.
                    return new PathResult(true, dfsTrace); // This prevents from being met in fist state. //TODO sometrace?
                    }
                } else {
                    // Not sure how to act for U not having actions
                    return new PathResult(true, dfsTrace); // This prevents from being met in fist state. //TODO sometrace?
                }
                
                
            }
        }

        if (!checkState(model, current, until.left).holds) {
            dfsTrace.add(current.getName());
            //System.out.println("It ends here");
            return new PathResult(false, dfsTrace);
        }

        for (Transition t : model.getTransitions()) {
            if (t.getSource().equals(current.getName())) {

                State next = model.getState(t.getTarget());

                // If next state is the final state, and there are only allowed transitions there then we reached the end state.
                if (checkState(model, next, until.right).holds) {
                    boolean canTransit = t.hasAllowedAction(until.getRightActions());
                    boolean allAllowed = t.allActionsAllowed(until.getRightActions());
                    //boolean canReachIllegally = t.
                    //if (canTransit) {
                    if (canTransit && allAllowed) {
                        continue;
                    } else {
                        // If there is a state p we can reach, but not by action in B then it fails.
                        // Yeah it actually doesn't make sense to terminate, should just treat normally to allow to transfer through it.
                        /*dfsTrace.add(t.getTarget());
                        dfsTrace.add(t.getActions()[0]);
                        dfsTrace.add(current.getName());
                        System.out.println("It ends here2");
                        return new PathResult(false, dfsTrace);*/
                    }
                } 

                if (visited.contains(next) ) { //&& !next.getName().equals(initialState.getName())
                    dfsTrace.add(t.getTarget());
                    dfsTrace.add(t.getActions()[0]);
                    dfsTrace.add(current.getName());
                    //System.out.println("It ends here22");
                    return new PathResult(false, dfsTrace);
                }
                
                String action = t.getAllowedAction(until.getLeftActions());
                if (action == null) {
                    // can return any action i guess? or lack of actions?
                    //System.out.println("It ends here4");
                    dfsTrace.add(t.getTarget());
                    dfsTrace.add(t.getActions()[0]);
                    dfsTrace.add(current.getName());
                    
                    return new PathResult(false, dfsTrace); 
                }
                // We now move to that state so we keep track
                dfsTrace.add(action);
                dfsTrace.add(current.getName());

                // We move to the next state
                PathResult result = dfsUntil(model, next, until, new HashSet<>(visited), false, initialState, transition);

                if (!result.holds) {
                    //System.out.println("It ends here3");
                    List<String> trace = new ArrayList<>(result.pathTrace);
                    trace.addAll(dfsTrace);
                    return new PathResult(false, trace);

                }
            }
        }

        dfsTrace.add(current.getName());
        //if (!hasSuccessor) return new PathResult(false, dfsTrace);

        //System.out.println("It ends here7");
        return new PathResult(true, dfsTrace); // TODO Some trace here
    }

    
    public String getBetterTrace() {
        if (traces == null || traces.isEmpty()) return null;

        List<String> copy = new ArrayList<>(traces.get(0));
        Collections.reverse(copy);

        return String.join("-", copy);
    }

    public String getBetterTrace(int index) {
        if (traces == null || index >= traces.size()) return null;

        List<String> copy = new ArrayList<>(traces.get(index));
        Collections.reverse(copy);

        return String.join("-", copy);
    }


}

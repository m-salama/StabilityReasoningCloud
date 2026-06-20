package org.cloudbus.cloudsim.aware.arch.time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationRule;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;

public class QLearningAlgorithm {

	/** The learning rate or step size determines to what extent newly acquired information 
	 * overrides old information.
	 */
    private static final double GAMMA = 0.8;

    /** list of states (of stability goals)	*/
	private static List<QLearningState> lstStates;
    /** list of actions (adaptation actions)	*/
	private static List<QLearningAction> lstActions;
    /** set of states (of stability goals)	*/
    private static int stateSize;
    /** set of actions (possible adaptation tactics)	*/
    private static int actionSize;
	
    /** set of possible actions for each state  */                                            
	private Map<QLearningState,List<QLearningAction>> validActions;
	/** transition matrix */
	private int[][] transitionMatrix;

	//private static int Valid_Actions[][] = new int[stateSize][actionSize];

    /** learning matrix when executing an action in a specific state	*/
    private static double q[][] = new double[stateSize][actionSize];
    	
    /** current state	*/
    private static QLearningState currentState;
    
    /** 
     * default constructor
    */   
    public QLearningAlgorithm() {
    	lstStates = new ArrayList<QLearningState>();
    	lstActions = new ArrayList<QLearningAction>();
    	
    	stateSize = 0;
    	actionSize = 0;
    	
    	currentState = new QLearningState();

        for(int i = 0; i < stateSize; i++) {
            for(int j = 0; j < actionSize; j++) {
                q[i][j] = 0;
            } 
        } 
    }
   
    public AdaptationTactic getDecision() {
    	AdaptationTactic adaptationTactic = null;
    	
    	train();
    	
    	QLearningState newState = new QLearningState();
    	newState = maximum(currentState, true);
    	currentState = newState;
    	
    	
        // Perform tests, starting at all initial states.
        System.out.println("Shortest routes from initial states:");
        for(int i = 0; i < Q_SIZE; i++)
        {
            currentState = INITIAL_STATES[i];
            int newState = 0;
            do
            {
                newState = maximum(currentState, true);
                System.out.print(currentState + ", ");
                currentState = newState;
            } while(currentState < 5);
            System.out.print("5\n");
        }   	
		return adaptationTactic;
    }
    
    /** Perform training	*/
    private static void train() {
    	chooseAnAction();
    	printQMatirx();
    }
    
/*     private static void episode(final int initialState) {
        currentState = initialState;

        // Travel from state to state until goal state is reached.
        do
        {
            chooseAnAction();
        }while(currentState == 5);

        // When currentState = 5, Run through the set once more for convergence.
        for(int i = 0; i < Q_SIZE; i++)
        {
            chooseAnAction();
        }
        return;
    }*/
    
    /** Randomly choose a possible action connected to the current state.
    */
    private static void chooseAnAction() {
    	QLearningAction possibleAction = new QLearningAction();
        possibleAction = getRandomAction();

        if(Valid_Actions[currentState.getId()][possibleAction.getId()] >= 0) {
            q[currentState.getId()][possibleAction.getId()] = reward(possibleAction);
            currentState = possibleAction.ge;
        }
        return;
    }
    
    private static QLearningAction getRandomAction() {
        Action action = null;
        boolean choiceIsValid = false;

        // Randomly choose a possible action connected to the current state.
        while(!choiceIsValid) {
            // Get a random item from the list of actions.
        	action = lstActions.get(new Random().nextInt(actionSize));
        	choiceIsValid = (Valid_Actions[currentState.getId()][action.getId()] > -1);
        }
        return action;
    }
    
    private static int reward(final QLearningAction action) {
        return (int)(Valid_Actions[currentState.getId()][action.getId()] + (GAMMA * maximum(action, false)));
    }
    
    /** 
		If ReturnIndexOnly = True, the Q matrix index is returned.
       	If ReturnIndexOnly = False, the Q matrix value is returned.
    */
    private static int maximum(final QLearningState state, final boolean ReturnIndexOnly) {      
    	QLearningAction winnerAction = null;
        boolean foundNewWinner = false;
        boolean done = false;

        while(!done) {
            foundNewWinner = false;
            for(int i = 0; i < ACTION_SIZE; i++) {
                if(i != winnerAction) {             // avoid self-comparison.
                    if(q[State][i] > q[State][winnerAction]) {
                    	winnerAction = i;
                        foundNewWinner = true;
                    }
                }
            }
            if(!foundNewWinner) {
                done = true;
            }
        }

        if(ReturnIndexOnly) {
            return winnerAction;
        } else {
            return q[state.getId()][winnerAction.getId()];
        }
    }
    

    
    private static Double getReward(QLearningState s){
    	return 10D;
    }
    
    private static void updateQ(QLearningState previousState, QLearningAction previousAction){
    	//q[previousState][previousAction] 
    }
    
    
    private static void printQMatirx() {
    	System.out.println("Q Matrix values:");
    	for(int i = 0; i < stateSize; i++) {
    		for(int j = 0; j < actionSize; j++) {
    			System.out.print(q[i][j] + ",\t");
    		} 
    		System.out.print("\n");
    	} 
    	System.out.print("\n");
    }
    
   public List<QLearningState> getLstStates() {
		return lstStates;
	}

	public void setLstStates(List<QLearningState> states) {
		this.lstStates = states;
	}

	public List<QLearningAction> getLstActions() {
		return lstActions;
	}

	public void setLstActions(List<QLearningAction> actions) {
		this.lstActions = actions;
	}

	public int getStateSize() {
		return stateSize;
	}

	public void setStateSize(int size) {
		this.stateSize = size;
	}

	public int getActionSize() {
		return actionSize;
	}

	public void setActionSize(int size) {
		this.actionSize = size;
	}

	public QLearningState getCurrentState() {
		return currentState;
	}

	public void setCurrentState(QLearningState s) {
		this.currentState = s;
	}

	public Map<QLearningState,List<QLearningAction>> getValidActions() {
		return validActions;
	}

	public void setValidActions(Map<QLearningState,List<QLearningAction>> validActions) {
		this.validActions = validActions;
	}



 
}

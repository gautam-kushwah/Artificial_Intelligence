/*
 * Wumpus-Lite, version 0.18 alpha
 * A lightweight Java-based Wumpus World Simulator
 *
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 *
 * Thanks to everyone who provided feedback and
 * suggestions for improving this application,
 * especially the students from Professor
 * Gmytrasiewicz's Spring 2007 CS511 class.
 *
 * Last modified 3/5/07
 *
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 *
 */

import java.util.LinkedList;
import java.util.Stack;
@SuppressWarnings("unused")
public class AgentFunction
{

    // string to store the agent's name
    // do not remove this variable
    private String agentName = "Agent Frost";

    final int maxDimension = 4;

    // Cell Structure
    private class Cell
    {
        int timesVisited = 0;
        boolean breeze = false; //used to infer pit
        boolean stench = false; //used to infer wumpus
        double pitProb = 0.12;
        double wumpusProb = 0.06;///1.0/((double)maxDimension*(double)maxDimension);
        boolean queue = false; //visited
    }

    //each option represents an adjacent Cell
    private class Neighbors
    {
        int xOff = 0;
        int yOff = 0;
        boolean valid = false;
    }

    //Defining a state to keep track of the path taken
    private class State{

        int x;
        int y;
        int parentX;
        int parentY;

        State(int x, int y){

            this.x = x;
            this.y = y;
            this.parentX = x;
            this.parentY = y;
        }
    }

    State initialState;
    State nextGoal;
    State wumpus;
    Stack<State> DFSqueue;
    Stack<State> pathToOrigin;

    private enum AgentDirection {
        RIGHT, DOWN, LEFT, UP;
    }


    LinkedList<Integer> actionList;   //A FIFO list to keep track of actions to be performed to reach a certain state


    private boolean goldTaken;     				// True if gold was successfuly grabbed, used to handle the condition when wumpus and gold are in same cell
    private boolean goldImpossible; 			// gold is in a pit or surrounded by pits
    private boolean wumpusAlive;    			// Wumpus alive flag, set to true until a scream is observed
    private boolean wumpusCertain;				 // if the wumpus is not moving and the agent has zeroed in on the position
    private boolean killWumpus;    				// flag to decide if the agent should kill wumpus in order to get gold
    private boolean hasArrow;       			// True if the agent can shoot
    private AgentDirection      agentDir;       // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
    private boolean exploring;         			// agent is moving to the next goal
    private int     agentX;
    private int     agentY;
    private AgentDirection     lastAgentDir;
    private int     lastAgentX;
    private int     lastAgentY;
    private int lastAction; 					// The last action the agent made
    private Cell[][]    beliefBoard;         	 // The game board as percieved by the agent
    private Neighbors[] Neighbors;         		// each option represents an adjacent square
    private int     visitedCells;       		// number of Cells which have been visited
    private int actionCount;

    private boolean originalPath;


    private int visitedCellsLimit(){

        return (int)((colDimensionLogic*rowDimensionLogic - 1)*0.8);
    }


    private double safeProb(int x, int y){

        if(x >= colDimensionLogic || y >= rowDimensionLogic) return 0.0;
        return (1 - beliefBoard[x][y].wumpusProb)*(1 - beliefBoard[x][y].pitProb);
    }

    private void setDeducedPitProb(int x, int y, double p){

        if(beliefBoard[x][y].pitProb != 0){

            beliefBoard[x][y].pitProb = p;
        }
    }

    private void assignWumpusProb(int x, int y, double p){
    	beliefBoard[x][y].wumpusProb = p;
    }

    // percieved gameboard Variables
    private int colDimensionLogic;  // The number of columns the game beliefBoard has, inferred by agent
    private int rowDimensionLogic;  // The number of rows the game beliefBoard has, inferred by agent


    public AgentFunction ( ){
        //Constructor

        goldTaken   = false;
        goldImpossible = false;
        hasArrow     = true;
        wumpusAlive = true;
        wumpusCertain = false;
        killWumpus   = false;
        agentDir     = AgentDirection.UP; // The direction the agent is facing:Initially it's North
        exploring    = false;
        agentX       = 0;
        agentY       = 0;
        lastAgentDir = AgentDirection.UP;
        lastAgentX   = -1;
        lastAgentY   = -1;
        lastAction   =  Action.NO_OP;
        beliefBoard = new Cell[maxDimension][maxDimension];           // The game board
        for ( int r = 0; r < maxDimension; ++r )
            for ( int c = 0; c < maxDimension; ++c )
                beliefBoard[c][r] = new Cell();

        // Initialize Neighbors
        Neighbors = new Neighbors[4]; // Index represents relative direction, e.g. Neighbors[3] means upper square
        for ( int i = 0; i < 4; i++)
            Neighbors[i] = new Neighbors();
        Neighbors[0].xOff = 1;
        Neighbors[1].yOff = -1;
        Neighbors[2].xOff = -1;
        Neighbors[3].yOff = 1;

        colDimensionLogic = maxDimension;
        rowDimensionLogic = maxDimension;

        actionList = new LinkedList<>();
        visitedCells = 0;
        actionCount = 0;

        initialState = new State(0,0);
        nextGoal = new State(0,0);
        wumpus = new State(colDimensionLogic - 1, rowDimensionLogic - 1);
        DFSqueue = new Stack<>();
        pathToOrigin = new Stack<>();
        originalPath = false;
    }

    public String getAgentName() {
        return agentName;
    }

    public int process(TransferPercept tp){


        boolean stench = tp.getStench();
        boolean breeze = tp.getBreeze();
        boolean glitter = tp.getGlitter();
        boolean bump = tp.getBump();
        boolean scream = tp.getScream();


        actionCount++;

        if (!actionList.isEmpty()) {
//        	System.out.println("Executing a previous action without regards for the percept");
            lastAction = actionList.poll();
            return lastAction;
        }

        //belief state update

        if(bump && lastAction == Action.GO_FORWARD){

            switch(agentDir){

                case RIGHT:
                    agentX = agentX - 1; //need to revise back
                    colDimensionLogic = agentX + 1;
                    break;

                case DOWN:

                    break;

                case LEFT:

                    break;

                case UP:
                    agentY = agentY - 1; //need to revise back
                    rowDimensionLogic = agentY + 1;
                    break;
            }

            //out of bounds, turn around
            setNextGoalState();
            exploring = true;
            return moveToGoal();
        }

        if(lastAction == Action.SHOOT){

            if(scream){

                wumpusAlive = false;
                setWumpusProb(agentX, agentY);
            }
            else{
            	//no scream heard after shoot, mark all states in the agent direction with zero prob of wumpus
                wumpusAlive = true;

                switch(agentDir){

                    case RIGHT:
                        for(int i = agentX; i < colDimensionLogic; i++)
                            beliefBoard[i][agentY].wumpusProb = 0;
                        break;

                    case LEFT:
                        for(int i = agentX; i >=0; i--)
                            beliefBoard[i][agentY].wumpusProb = 0;
                        break;

                    case UP:
                        for(int i = agentY; i < rowDimensionLogic; i++)
                            beliefBoard[agentX][i].wumpusProb = 0;
                        break;

                    case DOWN:
                        for(int i = agentY; i >= 0; i--)
                            beliefBoard[agentX][i].wumpusProb = 0;
                        break;
                }

                updateWumpusProb(agentX, agentY); //try to infer the location of wumpus
            }
        }

        if(lastAction == Action.NO_OP || (lastAction == Action.GO_FORWARD && !bump)){

            //save origin path to revert back
            if(!originalPath){

                if(!pathToOrigin.isEmpty()){

                    State State_1 = pathToOrigin.peek();

                    if(agentX == State_1.parentX && agentY == State_1.parentY){

                        pathToOrigin.pop();
                    }
                    else{

                        State State = new State(agentX, agentY);
                        State.parentX = lastAgentX;
                        State.parentY = lastAgentY;
                        pathToOrigin.push(State);
                    }
                }
                else{

                    State State = new State(agentX, agentY);
                    State.parentX = lastAgentX;
                    State.parentY = lastAgentY;
                    pathToOrigin.push(State);
                }
            }


            if(beliefBoard[agentX][agentY].timesVisited == 0){

                visitedCells++;
            }



            beliefBoard[agentX][agentY].timesVisited++;
//            if(beliefBoard[agentX][agentY].timesVisited > 1) {
//            	System.out.println("Visiting a square again");
//            }
//

            //updating the state
            if(beliefBoard[agentX][agentY].timesVisited == 1 || beliefBoard[agentX][agentY].timesVisited > 1){


                beliefBoard[agentX][agentY].breeze = breeze; //used to infer pit
                beliefBoard[agentX][agentY].stench = stench; //used to infer wumpus

                //current state update
                beliefBoard[agentX][agentY].pitProb = 0;
                beliefBoard[agentX][agentY].wumpusProb = 0;

                generateNeighbors(agentX, agentY);

                if(!breeze){

                    for(int i = 0; i < 4; i++){

                        if(Neighbors[i].valid){

                            int x_1, y_1;
                            x_1 = agentX + Neighbors[i].xOff;
                            y_1 = agentY + Neighbors[i].yOff;

                            beliefBoard[x_1][y_1].pitProb = 0;
                        }
                    }
                }
                else{

                    for(int i = 0; i < 4; i++){

                        if(Neighbors[i].valid){

                            int x_1, y_1;
                            x_1 = agentX + Neighbors[i].xOff;
                            y_1 = agentY + Neighbors[i].yOff;
                            //assign a probability only if the option is valid and never visited before
                            if(beliefBoard[x_1][y_1].timesVisited == 0) {
                            	setDeducedPitProb(x_1, y_1, 1.0);
                            }

                        }
                    }
                }

                //if wumpus alive and we don't know the postion of the wumpus try deducing
                if(wumpusAlive && !wumpusCertain){

                    if(!stench){
                    	//no stench mark the neighbors as safe
                        for(int i = 0; i < 4; i++){

                            if(Neighbors[i].valid){

                                int x_1, y_1;
                                x_1 = agentX + Neighbors[i].xOff;
                                y_1 = agentY + Neighbors[i].yOff;

                                beliefBoard[x_1][y_1].wumpusProb = 0;
                            }
                        }
                    }
                }
            }
        }


        if(stench && wumpusAlive && !wumpusCertain) updateWumpusProb(agentX, agentY);

        //start exploring
        generateStates(agentX, agentY);


        if(glitter){

            //stop DFS search
            exploring = false;
            goldTaken   = true;
            goldImpossible = false;

            lastAction   = Action.GRAB;
            return Action.GRAB;
        }

        if(agentX == 0 && agentY == 0){

            //if initial State has breeze, Prob(pit forward) = 50%!
        	//tried a few iterations with taking a chance not worth it
            if(breeze || goldTaken || goldImpossible) {
//            	double chance = Math.random();
//            	if(chance < 0.5) {
//            		lastAction = Action.GO_FORWARD;
//                    return Action.GO_FORWARD; //take a chance
//            	}
                lastAction = Action.END_TRIAL;
                return Action.END_TRIAL; //returning end trial but there is no condition to handle it in the env
                						//(returning NO_OP can result in randomly exploring forward thus decreasing score)
            }
        }

        //if uncertain of wumpus location trying luck by shooting the arrow
        if(stench && wumpusAlive && !wumpusCertain && hasArrow && validShoot(agentDir.ordinal())) {

            hasArrow = false;
            lastAction = Action.SHOOT;
            return Action.SHOOT;
        }

        if(stench && wumpusAlive && killWumpus && hasArrow){

            generateNeighbors(agentX, agentY);

            int i;

            for(i = 0; i < 4; i++){

                if(Neighbors[i].valid){

                    int x_1, y_1;
                    x_1 = agentX + Neighbors[i].xOff;
                    y_1 = agentY + Neighbors[i].yOff;

                    //inferred wumpus location
                    if(x_1 == wumpus.x && y_1 == wumpus.y){

                        exploring = false;
                        break;
                    }
                }
            }

            if(i < 4){

                //kill the wumpus
                return startKillWumpus(agentDir, i);
            }
        }



        //give up DFS search if stuck in the same place too long
        if(!goldTaken && !goldImpossible && actionCount > 2*colDimensionLogic*rowDimensionLogic){

            exploring = false;
            goldImpossible = true;
        }

        if(exploring){

            if(agentX == nextGoal.x && agentY == nextGoal.y){ //have reached the goal state as calculated by the agent

                exploring = false;
            }
            else{

                return moveToGoal();
            }
        }

        //retrace back to origin if stuck and couldn't find gold
        if(goldTaken || goldImpossible){

            nextGoal = initialState;
        }
        else{

            //set the next goal
            setNextGoalState();
        }

        exploring = true;
        return moveToGoal();

    }



    //update probability based on new percepts
    private void updateWumpusProb(int x, int y){

        generateNeighbors(x, y);

        int w_count = 0;

        for(int i = 0; i < 4; i++){

            if(Neighbors[i].valid){

                int x_1, y_1;

                x_1 = x + Neighbors[i].xOff;
                y_1 = y + Neighbors[i].yOff;

                if(beliefBoard[x_1][y_1].wumpusProb != 0){

                    assignWumpusProb(x_1, y_1, 1.0);
                    wumpus.x = x_1;
                    wumpus.y = y_1;
                    w_count++;
                }
            }
        }

        if(w_count == 1){
        	 //the other three neighbors didn't have the wumpus so this has to be it
             setWumpusProb(wumpus.x, wumpus.y);
        }
    }

    //update wumpus probability when wumpus location is determined
    private void setWumpusProb(int x, int y){

        wumpusCertain = false;

        //because there is only one wumpus
        for(int i = 0; i < colDimensionLogic; i++){

            for(int j = 0; j < rowDimensionLogic; j++){

                beliefBoard[i][j].wumpusProb = 0;
            }
        }

        if(wumpusAlive){

            beliefBoard[x][y].wumpusProb = 1;
        }
    }

    //classification of agent's location
    private enum Location
    {
        LEFT_UP,
        UP,
        RIGHT_UP,
        LEFT,
        MIDDLE,
        RIGHT,
        LEFT_DOWN,
        DOWN,
        RIGHT_DOWN
    }

    //classify agent's location
    private Location locationCase(int x,int y){

        if(x == 0){

            if(y == 0) return Location.LEFT_DOWN;
            if(y == rowDimensionLogic - 1) return Location.LEFT_UP;
            return Location.LEFT;
        }

        if(x == colDimensionLogic - 1){

            if(y == 0) return Location.RIGHT_DOWN;
            if(y == rowDimensionLogic - 1) return Location.RIGHT_UP;
            return Location.RIGHT;
        }

        if(y == 0) return Location.DOWN;
        if(y == rowDimensionLogic - 1) return Location.UP;
        return Location.MIDDLE;
    }

    //generate Neighbors based on Agent Location
    private void generateNeighbors(int x,int y){

        for(int i = 0; i < 4; i++) Neighbors[i].valid = false;

        Location location = locationCase(x,y);

        switch(location){

            case LEFT_DOWN:
                Neighbors[0].valid = true;
                Neighbors[3].valid = true;
                break;

            case LEFT:
                Neighbors[0].valid = true;
                Neighbors[1].valid = true;
                Neighbors[3].valid = true;
                break;

            case LEFT_UP:
                Neighbors[0].valid = true;
                Neighbors[1].valid = true;
                break;

            case DOWN:
                Neighbors[0].valid = true;
                Neighbors[2].valid = true;
                Neighbors[3].valid = true;
                break;

            case MIDDLE:
                Neighbors[0].valid = true;
                Neighbors[1].valid = true;
                Neighbors[2].valid = true;
                Neighbors[3].valid = true;
                break;

            case UP:
                Neighbors[0].valid = true;
                Neighbors[1].valid = true;
                Neighbors[2].valid = true;
                break;

            case RIGHT_DOWN:
                Neighbors[2].valid = true;
                Neighbors[3].valid = true;
                break;

            case RIGHT:
                Neighbors[1].valid = true;
                Neighbors[2].valid = true;
                Neighbors[3].valid = true;
                break;

            case RIGHT_UP:
                Neighbors[1].valid = true;
                Neighbors[2].valid = true;
                break;
        }
    }

    //generate children and put them into the stack
    private void generateStates(int x, int y){

        generateNeighbors(x,y);

        //add back children first so that they will be expanded last
        int backChild;
        backChild = agentDir.ordinal() + 2;
        if(backChild >= 4) backChild = backChild - 4;

        addGeneratedStates(x, y, backChild);

        for(int i = 0; i < 4; i++){

            if(i != agentDir.ordinal() && i != backChild){

                addGeneratedStates(x, y, i);
            }
        }

        //add front children last so that they will be expanded first
        addGeneratedStates(x, y, agentDir.ordinal());
    }

    private void addGeneratedStates(int x, int y, int i){

        if(Neighbors[i].valid){

            int x_1, y_1;

            x_1 = x + Neighbors[i].xOff;
            y_1 = y + Neighbors[i].yOff;

            //only add 100% safe ; do not add the State if it has been in the queue
            if(safeProb(x_1, y_1) == 1  && !beliefBoard[x_1][y_1].queue){
//            if(safeProb(x_1, y_1) == 1 && beliefBoard[x_1][y_1].timesVisited == 0 && !beliefBoard[x_1][y_1].queue){

                State State = new State(x_1, y_1);
                State.parentX = x;
                State.parentY = y;

                if(DFSqueue.search(State) == -1){ //not in queue

                    DFSqueue.push(State);
                    beliefBoard[x_1][y_1].queue = true;
                }
            }
        }
    }


    //if wumpus is in a pit don't shoot to prevent action cost
    // works only if the wumpus is static
    private boolean validShoot(int dir){

        boolean valid = false;

        switch(dir){

            case 0:
                if(colDimensionLogic - 1 > agentX){

                    if(beliefBoard[agentX+1][agentY].pitProb == 0) valid = true;
                }
                break;

            case 1:
                if(0 < agentY){

                    if(beliefBoard[agentX][agentY-1].pitProb == 0) valid = true;
                }
                break;

            case 2:
                if(0 < agentX){

                    if(beliefBoard[agentX-1][agentY].pitProb == 0) valid = true;
                }
                break;

            case 3:
                if(rowDimensionLogic - 1 > agentY){

                    if(beliefBoard[agentX][agentY+1].pitProb == 0) valid = true;
                }
                break;
        }

        return valid;
    }

    //set the top of the stack to be the next goal
    private void setNextGoalState(){

        do{
            //empty queue equlas no more safe states

            if(DFSqueue.empty()){

                //maybe wumpus and gold in the same cell therefore kill wumpus!
                if(!killWumpus && wumpusAlive && wumpusCertain && hasArrow && beliefBoard[wumpus.x][wumpus.y].pitProb == 0){

                    killWumpus = true;
                    nextGoal = wumpus;
                }
                else{

                    goldImpossible = true;
                    // go back to start
                    nextGoal = initialState;
                }
            }
            else{

                nextGoal = DFSqueue.pop();
            }

        }while(!(nextGoal.x < colDimensionLogic && nextGoal.y < rowDimensionLogic));
        //checking bounds before adding state
    }

    //moves required to get to the next goal state
    private int moveToGoal(){

        generateNeighbors(agentX, agentY);

        if(originalPath){

            return followOriginalPath();
        }

        int validCount = 0;  // a count of valid neighbor states
        LinkedList<Integer> moveMethods = new LinkedList<>();
        LinkedList<Integer> moveMethods_1 = new LinkedList<>();

        for(int i = 0; i < 4; i++){

            if(Neighbors[i].valid) {

                validCount++;
                moveMethods.addLast(i);
                moveMethods_1.addLast(i);
            }
        }

        double randomSelector;
        int selector, method, counter;

        counter = validCount;

        //first, select the possibly shortest path to goal
        while(!moveMethods.isEmpty() && counter!=0){

            randomSelector = Math.random(); //between 0 and 1
            selector = (int)(randomSelector/(1.0/counter));

            method = moveMethods.get(selector);
            moveMethods.remove(selector);
            counter--;

            switch(method){

                case 0:
                    if(nextGoal.x > agentX && safeProb(agentX+1, agentY) == 1)
                        return addToActionList(agentDir, AgentDirection.RIGHT);
                    break;

                case 1:
                    if(nextGoal.y < agentY && safeProb(agentX, agentY-1) == 1)
                        return addToActionList(agentDir, AgentDirection.DOWN);
                    break;

                case 2:
                    if(nextGoal.x < agentX && safeProb(agentX-1, agentY) == 1)
                        return addToActionList(agentDir, AgentDirection.LEFT);
                    break;

                case 3:
                    if(nextGoal.y > agentY && safeProb(agentX, agentY+1) == 1)
                        return addToActionList(agentDir, AgentDirection.UP);
                    break;
            }
        }

        //if loop
        if((goldTaken || goldImpossible) && !originalPath){

            originalPath = true;
            pathToOrigin.pop(); //pop the current location, move to the previous location
            return followOriginalPath();
        }

        //otherwise, randomly select a safe path to goal
        counter = validCount;

        while(!moveMethods_1.isEmpty() && counter!=0){

            randomSelector = Math.random(); //between 0 and 1
            selector = (int)(randomSelector/(1.0/counter));

            method = moveMethods_1.get(selector);
            moveMethods_1.remove(selector);
            counter--;

            switch(method){

                case 0:
                    if(safeProb(agentX+1, agentY) == 1)
                        return addToActionList(agentDir, AgentDirection.RIGHT);
                    break;

                case 1:
                    if(safeProb(agentX, agentY-1) == 1)
                        return addToActionList(agentDir, AgentDirection.DOWN);
                    break;

                case 2:
                    if(safeProb(agentX-1, agentY) == 1)
                        return addToActionList(agentDir, AgentDirection.LEFT);
                    break;

                case 3:
                    if(safeProb(agentX, agentY+1) == 1)
                        return addToActionList(agentDir, AgentDirection.UP);
                    break;
            }
        }

        //No_OP would introduce random behavior
        // hence Grab
        lastAction = Action.GRAB;
        return Action.GRAB;
    }

   // method to head back to the starting state
    private int followOriginalPath(){

        State State = pathToOrigin.pop();

        for(int i = 0; i < 4; i++){

            if(Neighbors[i].valid){

                int x_1, y_1;

                x_1 = agentX + Neighbors[i].xOff;
                y_1 = agentY + Neighbors[i].yOff;

                if(x_1 == State.x && y_1 == State.y){

                    switch(i) {

                        case 0:
                            return addToActionList(agentDir, AgentDirection.RIGHT);

                        case 1:
                            return addToActionList(agentDir, AgentDirection.DOWN);

                        case 2:
                            return addToActionList(agentDir, AgentDirection.LEFT);

                        case 3:
                            return addToActionList(agentDir, AgentDirection.UP);
                    }
                }
            }
        }


        lastAction = Action.GRAB;
        return Action.GRAB;
    }

    // Based on current direction and the desired direction, add action sequence to the queue
    private int addToActionList(AgentDirection currentDirection, AgentDirection desiredDirection) {

        //back up old location
        lastAgentX = agentX;
        lastAgentY = agentY;
        lastAgentDir = agentDir;

        //update location
        agentX = agentX + Neighbors[desiredDirection.ordinal()].xOff;
        agentY = agentY + Neighbors[desiredDirection.ordinal()].yOff;
        agentDir = desiredDirection;

        lastAction = Action.GO_FORWARD;

        switch(currentDirection) {

            case RIGHT:
                switch(desiredDirection) {
                    case RIGHT: // Facing R to go R
                        return Action.GO_FORWARD;

                    case DOWN: // Facing R to go D
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;

                    case LEFT: // Facing R to go L
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case UP: // Facing R to go U
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;
                }

            case DOWN:
                switch(desiredDirection) {
                    case RIGHT: // Facing D to go R
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case DOWN: // Facing D to go D
                        return Action.GO_FORWARD;

                    case LEFT: // Facing D to go L
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;

                    case UP: // Facing D to go U
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;
                }

            case LEFT:
                switch(desiredDirection) {
                    case RIGHT: // Facing L to go R
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case DOWN: // Facing L to go D
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case LEFT: // Facing L to go L
                        return Action.GO_FORWARD;

                    case UP: // Facing L to go U
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;
                }

            case UP:
                switch(desiredDirection) {
                    case RIGHT: // Facing U to go R
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;

                    case DOWN: // Facing U to go D
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case LEFT: // Facing U to go L
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case UP: // Facing U to go U
                        return Action.GO_FORWARD;
                }
        }


        agentX = lastAgentX;
        agentY = lastAgentY;
        agentDir = lastAgentDir;

        lastAction = Action.GRAB;
        return Action.GRAB;
    }

    private int startKillWumpus(AgentDirection currentDirection, int desiredDirection) {

        //back up old location
        lastAgentX = agentX;
        lastAgentY = agentY;
        lastAgentDir = agentDir;

        //update location
        agentX = agentX + Neighbors[desiredDirection].xOff;
        agentY = agentY + Neighbors[desiredDirection].yOff;

        switch(desiredDirection) {

            case 0:
                agentDir = AgentDirection.RIGHT;
                break;

            case 1:
                agentDir = AgentDirection.DOWN;
                break;

            case 2:
                agentDir = AgentDirection.LEFT;
                break;

            case 3:
                agentDir = AgentDirection.UP;
                break;
        }

        hasArrow = false;
        wumpusAlive = false;
        setWumpusProb(agentX, agentY); //when wumpus dies, all squares have Prob(wumpus) = 0
        lastAction = Action.GO_FORWARD;

        switch(currentDirection) {

            case RIGHT:
                switch(agentDir) {
                    case RIGHT:
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.SHOOT;

                    case DOWN:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;

                    case LEFT:
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case UP:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;
                }

            case DOWN:
                switch(agentDir) {
                    case RIGHT:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case DOWN:
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.SHOOT;

                    case LEFT:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;

                    case UP:
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;
                }

            case LEFT:
                switch(agentDir) {
                    case RIGHT:
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case DOWN:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case LEFT:
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.SHOOT;

                    case UP:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;
                }

            case UP:
                switch(agentDir) {
                    case RIGHT:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_RIGHT;

                    case DOWN:
                        actionList.addLast(Action.TURN_LEFT);
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case LEFT:
                        actionList.addLast(Action.SHOOT);
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.TURN_LEFT;

                    case UP:
                        actionList.addLast(Action.GO_FORWARD);
                        return Action.SHOOT;
                }
        }

        agentX = lastAgentX;
        agentY = lastAgentY;
        agentDir = lastAgentDir;

        lastAction = Action.SHOOT;
        return Action.SHOOT;
    }

}
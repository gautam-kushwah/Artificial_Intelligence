/* 
* Class that defines the agent function.
 * 
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 * 
 * Last modified 2/19/07 
 * 
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 * 
 */

import java.util.Random;

class AgentFunction {
	
	// string to store the agent's name
	// do not remove this variable
	private String agentName = "Agent Frost";
	
	// all of these variables are created and used
	// for illustration purposes; you may delete them
	// when implementing your own intelligent agent
	private int[] actionTable;
	private boolean bump;
	private boolean glitter;
	private boolean breeze;
	private boolean stench;
	private boolean scream;
	private Random rand;

	public AgentFunction()
	{
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your 
		// own intelligent agent

		// this integer array will store the agent actions
		actionTable = new int[12];
				  
		actionTable[0] = Action.GO_FORWARD;
		actionTable[1] = Action.GO_FORWARD;
		actionTable[2] = Action.GO_FORWARD;
		actionTable[3] = Action.GO_FORWARD;
		actionTable[4] = Action.TURN_RIGHT;
		actionTable[5] = Action.TURN_LEFT;
		actionTable[6] = Action.NO_OP;
		actionTable[7] = Action.NO_OP;
		actionTable[8] = Action.NO_OP;
		actionTable[9] = Action.NO_OP;
		actionTable[10] = Action.SHOOT;
		actionTable[11] = Action.GRAB;;
		
		
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
	}

	public int process(TransferPercept tp)
	{
		// To build your own intelligent agent, replace
		// all code below this comment block. You have
		// access to all percepts through the object
		// 'tp' as illustrated here:
		
		// read in the current percepts
		bump = tp.getBump();
		glitter = tp.getGlitter();
		breeze = tp.getBreeze();
		stench = tp.getStench();
		scream = tp.getScream();
		
		
		
		//condition action rules based on percept sequence
		//whenever there is glitter we simply return grab
		//other times we use a random integer and choose an appropriate action from action table
		//the random variable helps in accounting for probability while returning an action
		
		if (bump == false && glitter == false && breeze == false && stench == false && scream == false) {
			// 1
			return actionTable[rand.nextInt(0, 6)];
		}
		if (bump == false && glitter == false && breeze == false && stench == false && scream == true) {
			// 2
			return actionTable[rand.nextInt(0, 6)];
		}
		if (bump == false && glitter == false && breeze == false && stench == true && scream == false) {
			// 3
			return actionTable[rand.nextInt(4, 11)];
		}
		if (bump == false && glitter == false && breeze == false && stench == true && scream == true) {
			return actionTable[0];
		}
		if (bump == false && glitter == false && breeze == true && stench == false && scream == false) {
			// 5
			return actionTable[rand.nextInt(4, 10)];
			
		}
		if (bump == false && glitter == false && breeze == true && stench == false && scream == true) {
			// 6
			return actionTable[rand.nextInt(4, 10)];
		}
		if (bump == false && glitter == false && breeze == true && stench == true && scream == false) {
			// 7
			return actionTable[rand.nextInt(4, 11)];
		}
		if (bump == false && glitter == false && breeze == true && stench == true && scream == true) {
			// 8
			return actionTable[rand.nextInt(4, 11)];
		}
		if (bump == false && glitter == true && breeze == false && stench == false && scream == false) {
			// 9
			return actionTable[11];
		}
		if (bump == false && glitter == true && breeze == false && stench == false && scream == true) {
			// 10
			return actionTable[11];
		}
		if (bump == false && glitter == true && breeze == false && stench == true && scream == false) {
			// 11
			return actionTable[11];
		}
		if (bump == false && glitter == true && breeze == false && stench == true && scream == true) {
			// 12
			return actionTable[11];
		}
		if (bump == false && glitter == true && breeze == true && stench == false && scream == false) {
			// 13
			return actionTable[11];
		}
		if (bump == false && glitter == true && breeze == true && stench == false && scream == true) {
			// 14
			return actionTable[11];
		}
		if (bump == false && glitter == true && breeze == true && stench == true && scream == false) {
			// 15
			return actionTable[11];
		}
		if (bump == false && glitter == true && breeze == true && stench == true && scream == true) {
			// 16
			return actionTable[11];
		}
		if (bump == true) {
			return actionTable[rand.nextInt(3, 10)];
		}
		
		
		if(glitter ==true) {
			return actionTable[6];
		}
		
		//code never reaches here
		
		// return action to be performed
	    return actionTable[rand.nextInt(8)];	    
	}
	
	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
}
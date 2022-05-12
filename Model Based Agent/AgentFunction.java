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

import java.util.HashMap;

class AgentFunction {
    
    // string to store the agent's name
    // do not remove this variable
    private String agentName = "Agent Frost";
    
    private Model model;

    public AgentFunction() {
        model = new Model();
    }

    /*
    Percepts tuple - <glitter, stench, scream, bump, breeze>
    
    read the percepts and return the most suitable action
    */
    public int process(TransferPercept tp) {
        // 
        // if it's glitter then grab right away
        if(tp.getGlitter()){
            return Action.GRAB;
        }

        //updating the model based on last action
        //initial case 0,0 no update based on action
        model.updateModelOnAction();
        
        
        //updating the model based on current percepts
        //gets executed at 0,0 to update model of 3 adjacent cells
        model.updateModelOnPercept(tp.getBreeze(), tp.getStench(), tp.getBump(), tp.getScream(), tp.getGlitter());

        //Condition - Action Rules
        if (tp.getBump()) {
            //save the current action
            model.setPrevAction(Action.TURN_LEFT);
            return Action.TURN_LEFT;
        }

        //check and update any conflicting markings of pits and wumpus
        updateCellStatus(tp);
        //Get the location of the next safe cell
        int[] safe_loc = model.nextOkCell();

        //If no safe cell is found, then do NO_OP
        if (safe_loc[0] == -1) {
            
            model.setPrevAction(Action.NO_OP);
            return Action.NO_OP;
        }

        //Check if forward action takes the agent to the desired safe cell
        if (canMoveTo(safe_loc)) {
            model.resetNextLoc();
            model.setPrevAction(Action.GO_FORWARD);
            return Action.GO_FORWARD;
        }
        else {
        	//take a turn in that direction
        	int direction = getTurnDirection(safe_loc);
        	
        	
        	model.setPrevAction(direction);
            return direction; 	
        }


    }
 
    
  private int getTurnDirection (int[] loc) {
  	int[] cur_loc =model.getAgentLoc();
  	int x_diff,  y_diff;
  	x_diff = loc[0] - cur_loc[0];
  	y_diff = loc[1] - cur_loc[1];
  	String dir = model.getAgentDirection();
  	int turn_action = 0;
  	
  	
  	if(x_diff > 0 || y_diff > 0){
  	  if(dir.equals("S")) {
  		turn_action = 3;
  	  }
  	  turn_action = 2;
  	}
  	
  	
  	
  	if(x_diff < 0 && y_diff > 0){
  	  if(dir.equals("S")) {
    		turn_action = 2;
    	  }	
  	  turn_action = 3;
  	}
  	
  	
  	
  	if(x_diff > 0 && y_diff < 0){
    	  if(dir.equals("S")) {
      		turn_action = 3;
      	  }	
    	  turn_action = 2;
  	}
  	
  	
  	
  	if(x_diff < 0 || y_diff < 0){
    	  if(dir.equals("E")) {
      		turn_action = 2;
      	  }	
    	  turn_action = 3;
  	}
  	
  	
  	
  	
  	return turn_action;
  }

    private boolean canMoveTo(int[] loc) {
        /* Based on the direction the agent is currently facing, check if the forward action takes the
            agent to the loc cell
        */
        switch (model.getAgentDirection()){
            case "E":
                return model.getAgentLoc()[0] == loc[0] && model.getAgentLoc()[1] + 1 == loc[1];
            case "W":
                return model.getAgentLoc()[0] == loc[0] && model.getAgentLoc()[1] - 1 == loc[1];
            case "N":
                return model.getAgentLoc()[0] - 1 == loc[0] && model.getAgentLoc()[1] == loc[1];
            case "S":
                return model.getAgentLoc()[0] + 1 == loc[0] && model.getAgentLoc()[1] == loc[1];
        }
        return false;
    }

    private void updateCellStatus(TransferPercept tp) {
        int[] aLoc = model.getAgentLoc();

        //Pass the location of all adjacent cells to the current agent location..
        if(aLoc[1] + 1 < model.getWorld()[0].length){
            updateHelper(aLoc[0], aLoc[1] + 1, tp);
        }

        if(aLoc[1] - 1 >= 0){
            updateHelper(aLoc[0], aLoc[1] - 1, tp);
        }

        if(aLoc[0] - 1 >= 0){
            updateHelper(aLoc[0] - 1, aLoc[1], tp);
        }

        if(aLoc[0] + 1 < model.getWorld().length){
            updateHelper(aLoc[0] + 1, aLoc[1], tp);
        }
    }

    private void updateHelper(int r, int c, TransferPercept tp) {
    	//If ok then do nothing..
        if(model.getWorld()[r][c].isCellOK())
            return;
    	
    	//Get the pit and the wumpus flags set for the cell at (r, c) position
        HashMap<String, String> flags = model.getWorld()[r][c].getFlags();

        

        /*
        Three conditions to eliminate a pit/wumpus possibility
        - pit and wumpus but no breeze and stench
        - pit but no breeze
        - wumpus but no stench
        */
        boolean breeze = tp.getBreeze();
        boolean stench = tp.getStench();
        if(flags.get(Utils.pit).equals("?") &&
                flags.get(Utils.wumpus).equals("?")) {
            if (!(breeze && stench)) {
                model.getWorld()[r][c].setCellOK(true);
                flags.put(Utils.pit, "");
                flags.put(Utils.wumpus, "");
            }
        }

        if(flags.get(Utils.pit).equals("?") && !flags.get(Utils.wumpus).equals("?")){
            if(!breeze){
                model.getWorld()[r][c].setCellOK(true);
                flags.put(Utils.pit, "");
            }
        }

        if(!flags.get(Utils.pit).equals("?") && flags.get(Utils.wumpus).equals("?")){
            if(!stench){
                model.getWorld()[r][c].setCellOK(true);
                flags.put(Utils.wumpus, "");
            }
        }
    }

    // public method to return the agent's name
    // do not remove this method
    public String getAgentName() {
        return agentName;
    }
}
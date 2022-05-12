import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;

public class Search {
    private List<Utility> utilities = new ArrayList<>();
    private List<String> goals = new ArrayList<>();
    private int destX;
    private int destY;
    private WumpusWorld wumpusWorld;
    private int flag = 0;

    public List<Integer> fwdSearch(List<WumpusCellClass> possibleExploredCells, List<WumpusCellClass> possibleUnexploredCells, int agentPosX, int agentPosY, char direction, WumpusWorld wumpus_World) {
        this.wumpusWorld = wumpus_World;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
            	//if a cell is safe but not visited and doesn't have the possibility of wumpus or a pit
            	//add to goals 
                if ((wumpusWorld.cells[i][j].safe == 1 && wumpusWorld.cells[i][j].visited != 1) || (wumpusWorld.cells[i][j].safe == 1 && wumpusWorld.cells[i][j].visited != 1 && wumpusWorld.cells[i][j].wumpus == 0 && wumpusWorld.cells[i][j].pit == 0)) {

                        goals.add(String.valueOf(i) + "-" + String.valueOf(j));
                }
            }
        }
        
        // adding cells to utilities list with their indexes 
        if(possibleUnexploredCells.size() != 0){
            for (WumpusCellClass cell : possibleUnexploredCells) {
                utilities.add(new Utility(cell.x, cell.y));
            }
        } else {
            for (WumpusCellClass cell : possibleExploredCells) {
                utilities.add(new Utility(cell.x, cell.y));
            }
        }
        
        int counter = 0;
        if(possibleUnexploredCells.size() != 0) {
            for (WumpusCellClass cell : possibleUnexploredCells) {
            	//if the cell is in the vicinity of the agent add it's index to the destination index
                if (direction == 'N' && cell.y == agentPosY + 1 && cell.x == agentPosX) {
                    destX = cell.x;
                    destY = cell.y;
                    counter += 1;
                    break;
                } else if (direction == 'E' && cell.y == agentPosY && cell.x == agentPosX + 1) {
                    destX = cell.x;
                    destY = cell.y;
                    counter += 1;
                    break;
                } else if (direction == 'S' && cell.y == agentPosY - 1 && cell.x == agentPosX) {
                    destX = cell.x;
                    destY = cell.y;
                    counter += 1;
                    break;
                } else if (direction == 'W' && cell.y == agentPosY && cell.x == agentPosX - 1) {
                    destX = cell.x;
                    destY = cell.y;
                    counter += 1;
                    break;
                }
            }

            if (counter == 0) {
                Utility finalCell = getUtilityValue();
                destX = finalCell.x;
                destY = finalCell.y;
            }
            
            
        } else {
                Utility finalCell = getUtilityValue();
                destX = finalCell.x;
                destY = finalCell.y;
        }

        List<Integer> result = new ArrayList<>();
        
        
        //if Utility of destination is not 0
        if(flag == 0) {
            result.add(destX);
            result.add(destY);
        } else {
        	// return the agent's position 
            result.add(agentPosX);
            result.add(agentPosY);
        }

        return result;
    }

    private Utility getUtilityValue() {
        List<Goal> possibleGoalCells = new ArrayList<>();

        for(int i = 0; i < 4; i++)
            for(int j = 0; j < 4; j++)
                possibleGoalCells.add(new Goal(i,j));

        for (Utility utilityCell : utilities) {
            List<String>  reachableCell = new ArrayList<>();

            if(wumpusWorld.cells[utilityCell.x][utilityCell.y].visited == 0) {
            	utilityCell.utility += 1;
            }

            for(Goal goals : possibleGoalCells) {
                goals.dist = abs(goals.x - utilityCell.x) +  abs(goals.y - utilityCell.y);
            }

            Collections.sort(possibleGoalCells, new GoalCellComparator());

            for(Goal goalCell : possibleGoalCells){

                if(goalCell.dist == 0) {
                    reachableCell.add(String.valueOf(goalCell.x)+"-"+String.valueOf(goalCell.y));
                } else {
                    int x1 = goalCell.x;
                    int y1 = goalCell.y;
                    
                    // if reachable cell is adjacent
                    if(reachableCell.contains(String.valueOf(x1-1)+"-"+String.valueOf(y1)) || reachableCell.contains(String.valueOf(x1+1)+"-"+String.valueOf(y1)) || reachableCell.contains(String.valueOf(x1)+"-"+String.valueOf(y1-1)) || reachableCell.contains(String.valueOf(x1)+"-"+String.valueOf(y1+1))) {
                        if (this.goals.contains(String.valueOf(x1)+"-"+String.valueOf(y1))) {
                            reachableCell.add(String.valueOf(goalCell.x)+"-"+String.valueOf(goalCell.y));
                            utilityCell.utility += 1 / (double)goalCell.dist ;  //update utility
                        } else {
                        	//if the cell doesn't have wumpus or pit add it to reachable list
                            if((wumpusWorld.cells[x1][y1].pit == 0 && wumpusWorld.cells[x1][y1].wumpus == 0 && (wumpusWorld.cells[x1][y1].safe != -1 || wumpusWorld.cells[x1][y1].wumpusExists != 1 )) || (wumpusWorld.cells[x1][y1].safe == 1)) {
                                reachableCell.add(String.valueOf(goalCell.x)+"-"+String.valueOf(goalCell.y));
                            }
                        }
                    }
                }
            }
            
            
        }

        Comparator<Utility> customCompare = Comparator.comparing(Utility::getUtility);
        
        // return cell with the max utility
        Utility finalCell = Collections.max(utilities,customCompare);
        if(finalCell.utility == 0) {
            flag = 1;
        }
        return finalCell;
    }

}

class Utility {
    int x,y;
    double utility = 0.0;
    Utility(int x, int y){
        this.x = x;
        this.y = y;
    }
    double getUtility() {
        return this.utility;
    }
}

class Goal {
    int x,y;
    int dist=0;
    Goal(int x, int y){
        this.x = x;
        this.y = y;
    }
    int getDist() {
        return this.dist;
    }
}

class GoalCellComparator implements Comparator<Goal> {
    public int compare(Goal goal1, Goal goal2) {
        return goal1.getDist() - goal2.getDist();
    }
}
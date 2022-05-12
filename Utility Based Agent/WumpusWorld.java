import java.util.Stack;


// Class to calculate and update wumpus and pit probabilities
public class WumpusWorld {
    public int wumpusFound = 0;
    public int pitFound = 0;
    public int pitX = 0;
    public int pitY = 0;
    public WumpusCell[][] cells = new WumpusCell[4][4];

    
    //initialising the wumpus world model by creating 4x4 matrix with new 'WumpusCell'
    public WumpusWorld() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cells[i][j] = new WumpusCell();
            }
        }
        
        //marking the cell (0,0) as both safe and visited
        cells[0][0].safe = 1;
        cells[0][0].visited = 1;
    }


    public void resolveWumpus() {
    	
        if (wumpusFound == 0) {
            int maxpitprobability = 1;
            Stack<String> wumpusstack = new Stack<String>();

            for (int i = 0; i <= 3; i++) {
                for (int j = 0; j <= 3; j++) {
                    if (cells[i][j].wumpus > maxpitprobability && cells[i][j].safe != 1) {
                        wumpusstack.clear();
                        wumpusstack.push(Integer.toString(i) + Integer.toString(j));
                        maxpitprobability = (int) cells[i][j].wumpus;
                    } else if (cells[i][j].wumpus == maxpitprobability && cells[i][j].safe != 1) {
                        wumpusstack.push(Integer.toString(i) + Integer.toString(j));
                    }
                }
            }

            if (wumpusstack.size() == 1) {
                char position[] = wumpusstack.peek().toCharArray();
                int wumpus_x = Character.getNumericValue(position[0]);
                int wumpus_y = Character.getNumericValue(position[1]);
                
                cells[wumpus_x][wumpus_y].safe = -1;
                cells[wumpus_x][wumpus_y].wumpusExists = 1;
                wumpusFound = 1;
            }
        }

        if (wumpusFound == 1) {
            for (int i = 0; i <= 3; i++) {
                for (int j = 0; j <= 3; j++) {
                    cells[i][j].wumpus = 0;
                }
            }
//            try {
//            	cells[w_x][w_y].wumpus = 1;
//            }finally {
//            	
//            }
            
        }

    }

    // if no stench is observed clear the nearby cells for pit
    public void clearPit(int leftx, int rightx, int positionX, int topy, int bottomy, int positionY) {
        cells[leftx][positionY].pitExists = 0;
        cells[rightx][positionY].pitExists = 0;
        cells[positionX][topy].pitExists = 0;
        cells[positionX][bottomy].pitExists = 0;
        cells[leftx][positionY].pit = 0;
        cells[rightx][positionY].pit = 0;
        cells[positionX][topy].pit = 0;
        cells[positionX][bottomy].pit = 0;
    }

    // function to resolve the position of the pit if the agent perceives a breeze
    public void resolvePit(int leftx, int rightx, int positionX, int topy, int bottomy, int positionY) {
        if (pitFound < 2) {
            Stack<String> pitstack = new Stack<String>();
            // converting the positions as a string and combining them to store it onto the stack
            
            String left_x = Integer.toString(leftx);
            String position_y = Integer.toString(positionY);
            String right_x = Integer.toString(rightx);
            String position_x = Integer.toString(positionX);
            String top_y = Integer.toString(topy);
            String bottom_y = Integer.toString(bottomy);
            
            if (cells[leftx][positionY].pitExists == 1 && cells[leftx][positionY].safe != 1) {
                pitstack.push(left_x + position_y);
            }
            if (cells[rightx][positionY].pitExists == 1 && cells[rightx][positionY].safe != 1) {
                pitstack.push(right_x + position_y);
            }
            if (cells[positionX][topy].pitExists == 1 && cells[positionX][topy].safe != 1) {
                pitstack.push(position_y + top_y);
            }
            if (cells[positionX][bottomy].pitExists == 1 && cells[positionX][bottomy].safe != 1) {
                pitstack.push(position_x + bottom_y);
            }
            if (pitstack.size() == 1) {
                char position[] = pitstack.peek().toCharArray();
                int pos_x = Character.getNumericValue(position[0]);
                int pos_y = Character.getNumericValue(position[1]);
                cells[ pos_x ][ pos_y ].safe = -1;
                
                
                //if already found the x and y co-ordinates of the pit 
                if (pitX == pos_x && pitY == pos_y) {
                	// do nothing
                } else {
                	
                	//update
                    pitFound += 1;
                    pitX = pos_x;
                    pitY = pos_y;
                }
            }
        }
        // if pit found, update the probability and presence of pit variable for all the cells
        if (pitFound == 2) {
            for (int i = 0; i <= 3; i++) {
                for (int j = 0; j <= 3; j++) {
                    cells[i][j].pitExists = 0;
                    cells[i][j].pit = 0;
                }
            }
        }
    }

    // Update the wumpus world model based on the current percepts
    public void updateWumpusWorld(int positionX, int positionY, int option) {
        // bounds for the wumpus world
    	int leftx = 0;
        int rightx = 3;
        int bottomy = 0;
        int topy = 3;
        switch (option) {
            // For case 0 - No breeze or stench, Hence adjacent cells can be tagged as safe
            case 0:
                leftx = positionX - 1 < leftx ? leftx : positionX - 1;
                bottomy = positionY - 1 < bottomy ? bottomy : positionY - 1;
                rightx = positionX + 1 > rightx ? rightx : positionX + 1;
                topy = positionY + 1 > topy ? topy : positionY + 1;
                cells[leftx][positionY].safe = 1;
                cells[rightx][positionY].safe = 1;
                cells[positionX][topy].safe = 1;
                cells[positionX][bottomy].safe = 1;
                break;
                
                
            // For case 1 - Both breeze and stench observed, Update wumpus and pit probabilities by adding 1
            // Then based on Wumpus and pit Probabilities try to finalize the position of wumpus and pit positions
            // By calling resolveWumpus and resolvePit functions
            case 1:
                leftx = positionX - 1 < leftx ? leftx : positionX - 1;
                bottomy = positionY - 1 < bottomy ? bottomy : positionY - 1;
                rightx = positionX + 1 > rightx ? rightx : positionX + 1;
                topy = positionY + 1 > topy ? topy : positionY + 1;
                if (cells[leftx][positionY].pitExists == 1)
                    cells[leftx][positionY].pit += 1;
                if (cells[rightx][positionY].pitExists == 1)
                    cells[rightx][positionY].pit += 1;
                if (cells[positionX][topy].pitExists == 1)
                    cells[positionX][topy].pit += 1;
                if (cells[positionX][bottomy].pitExists == 1)
                    cells[positionX][bottomy].pit += 1;
                cells[leftx][positionY].wumpus += 1;
                cells[rightx][positionY].wumpus += 1;
                cells[positionX][topy].wumpus += 1;
                cells[positionX][bottomy].wumpus += 1;
                resolveWumpus();
                resolvePit(leftx, rightx, positionX, topy, bottomy, positionY);
                break;
                
                
            // For case 2 - Only stench observed, Update wumpus probabilities by adding 1
            // Then based on Wumpus probability try to finalize the position of wumpus positions
            // By calling resolveWumpus function
            // breeze not observed - eliminate possibility of breeze in neighboring cells
            // By  calling clearPit function
            case 2:
                leftx = positionX - 1 < leftx ? leftx : positionX - 1;
                bottomy = positionY - 1 < bottomy ? bottomy : positionY - 1;
                rightx = positionX + 1 > rightx ? rightx : positionX + 1;
                topy = positionY + 1 > topy ? topy : positionY + 1;
                cells[leftx][positionY].wumpus += 1;
                cells[rightx][positionY].wumpus += 1;
                cells[positionX][topy].wumpus += 1;
                cells[positionX][bottomy].wumpus += 1;
                resolveWumpus();
                clearPit(leftx, rightx, positionX, topy, bottomy, positionY);
                break;
                
                
            // For case 3 - Only breeze observed, Update pit probabilities
            // Then based on Pit Probabilities try to finalize the position of pit
            // By calling resolvePit functions
            case 3:
                leftx = positionX - 1 < leftx ? leftx : positionX - 1;
                bottomy = positionY - 1 < bottomy ? bottomy : positionY - 1;
                rightx = positionX + 1 > rightx ? rightx : positionX + 1;
                topy = positionY + 1 > topy ? topy : positionY + 1;
                if (cells[leftx][positionY].pitExists == 1)
                    cells[leftx][positionY].pit += 1;
                if (cells[rightx][positionY].pitExists == 1)
                    cells[rightx][positionY].pit += 1;
                if (cells[positionX][topy].pitExists == 1)
                    cells[positionX][topy].pit += 1;
                if (cells[positionX][bottomy].pitExists == 1)
                    cells[positionX][bottomy].pit += 1;
                resolvePit(leftx, rightx, positionX, topy, bottomy, positionY);
                break;
                
              
            // Case - 4: Scream Observed along with Breeze
            // Since wumpus is dead, mark all cells as wumpus free 
            // Then since breeze is observed, update pit probabilities
            // and then try finalizing pit position
            // by calling resolvePit
            case 4:
                for (int i = 0; i <= 3; i++) {
                    for (int j = 0; j <= 3; j++) {
                        if (cells[i][j].wumpusExists == 1) {
                            cells[i][j].safe = 0;
                            cells[i][j].wumpusExists = 0;
                        }
                        cells[i][j].wumpus = 0;
                        cells[i][j].wumpusExists = 0;
                    }
                }
                leftx = positionX - 1 < leftx ? leftx : positionX - 1;
                bottomy = positionY - 1 < bottomy ? bottomy : positionY - 1;
                rightx = positionX + 1 > rightx ? rightx : positionX + 1;
                topy = positionY + 1 > topy ? topy : positionY + 1;
                if (cells[leftx][positionY].pitExists == 1)
                    cells[leftx][positionY].pit += 1;
                if (cells[rightx][positionY].pitExists == 1)
                    cells[rightx][positionY].pit += 1;
                if (cells[positionX][topy].pitExists == 1)
                    cells[positionX][topy].pit += 1;
                if (cells[positionX][bottomy].pitExists == 1)
                    cells[positionX][bottomy].pit += 1;
                resolvePit(leftx, rightx, positionX, topy, bottomy, positionY);
                break;
                
                
            // Case - 5: Only scream observed
            // Mark all cells as Wumpus free, since it is dead by making wumpus probability to 0
            // Since breeze is also not observed, mark neighboring cells as safe
            case 5:
                for (int i = 0; i <= 3; i++) {
                    for (int j = 0; j <= 3; j++) {
                        if (cells[i][j].wumpusExists == 1) {
                            cells[i][j].safe = 0;
                            cells[i][j].wumpusExists = 0;
                        }
                        cells[i][j].wumpusExists = 0;
                        cells[i][j].wumpus = 0;
                    }
                }
                leftx = positionX - 1 < leftx ? leftx : positionX - 1;
                bottomy = positionY - 1 < bottomy ? bottomy : positionY - 1;
                rightx = positionX + 1 > rightx ? rightx : positionX + 1;
                topy = positionY + 1 > topy ? topy : positionY + 1;
                clearPit(leftx, rightx, positionX, topy, bottomy, positionY);
                cells[leftx][positionY].safe = 1;
                cells[rightx][positionY].safe = 1;
                cells[positionX][topy].safe = 1;
                cells[positionX][bottomy].safe = 1;
                break;
        }
    }
}
// An individual cell of the wumpus world with the following properties
//	1. safe - cell safe or not
//  2. pit - Probability of pit 
//  3. wumpus - Probability of wumpus 
//  4. visited - 1 if the cell is visitied, 0 otherwise
//  5. pitExists - to indicate the presence of oit
//  6. wumpusExists - To indicate presence of a wumpus
public class WumpusCell {
    public int safe = 0;
    public float pit = 0;
    public float wumpus = 0;
    public int visited = 0;
    public int pitExists = 1;
    public int wumpusExists = 0;
}

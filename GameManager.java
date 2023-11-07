import java.util.*;


public class GameManager {
    private Ship ship;
    private int shipSize;
    private int k;
    private Cell botPosition;
    private Cell leakPosition;
    private List<Cell> coveredGrid;
    private int botStrategy;
    private Bot bot;
    private Set<Cell> visitedNodes;
    private double[][] knowledgeGrid;
    private Random random = new Random();
    private List<Cell> Probability_list;
    private int numOpenCells;
    private double alpha; // New variable for alpha
    private double[][] probabilityGrid;
    
    public static void main(String[] args) {
        int shipSize = 50; // Set your ship size here
        int botStrategy = 1; // Set your bot strategy here
        int k = 5; // Set your 'k' value here
        double alpha = 1; // Set your alpha value here

        GameManager gameManager = new GameManager(shipSize, botStrategy, k, alpha);
    }
   
    public GameManager(int shipSize, int botStrategy, int k, double alpha) {
        this.ship = new Ship(shipSize);
        this.shipSize = shipSize;
        this.k = k;
        this.botPosition = getRandomOpenCell(ship.getOpenCells());
        this.leakPosition = getRandomOpenCell(ship.getOpenCells());
        this.coveredGrid = new ArrayList<>();
        this.botStrategy = botStrategy;
        //this.bot = new Bot(ship, botStrategy, botPosition, k);
        this.visitedNodes = new HashSet<>();
        this.knowledgeGrid = new double[shipSize][shipSize]; // Changed from String to double
        
        this.alpha = alpha; // Initialize alpha
        this.probabilityGrid = new double[shipSize][shipSize];
        initializeProbabilityGrid(); // Initialize probability grid
        this.numOpenCells = ship.getOpenCells().size();
        int totalActions = 0;
        totalActions = Game();
        System.out.println(totalActions);
        

        
        

    }

    private int Game(){
        int actions = 0;
        boolean beep = false;
        while(botPosition!=leakPosition){
            updateBotProbabilities();
            beep = sense();
            actions+=1;
            if(beep){
                updateBeepProbabilities();
            }
            else{
                updateNotBeepProbabities();
            }

            List<Cell> path = pathFinder();
            for (Cell cell : path) {
                botPosition = cell;
                if(botPosition==leakPosition){
                    return actions;
                }
                else{
                    updateBotProbabilities();
                }
                actions+=1;
            }

        }
        return actions;
    }

    private void updateNotBeepProbabities() {
        double totalProbability = 0.0;

        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                if (isOpen(new Cell(i, j))) {
                    double beepProbability = calculateBeepProbability(new Cell(i, j), botPosition);
                    probabilityGrid[i][j] = probabilityGrid[i][j] / (1.0 - beepProbability);
                } 

                totalProbability += probabilityGrid[i][j];
            }
        }

        // Normalize probabilities
        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                probabilityGrid[i][j] /= totalProbability;
            }
        }
    }

    private void updateBeepProbabilities() {
        double totalProbability = 0.0;

        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                if (isOpen(new Cell(i, j))) {
                    double beepProbability = calculateBeepProbability(new Cell(i, j), botPosition);
                    probabilityGrid[i][j] = probabilityGrid[i][j] * beepProbability;
                } 

                totalProbability += probabilityGrid[i][j];
            }
        }

        // Normalize probabilities
        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                probabilityGrid[i][j] /= totalProbability;
            }
        }
    }

    private void initializeKnowledgeGrid() {
        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                knowledgeGrid[i][j] = 1.0 / numOpenCells;
            }
        }
    }

    private void initializeProbabilityGrid() {
        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                if (!botPosition.equals(new Cell(i, j)) && !leakPosition.equals(new Cell(i, j))) {
                    probabilityGrid[i][j] = 1.0 / numOpenCells;
                }
            }
        }
    }


    private Cell getRandomOpenCell(List<Cell> openCells) {
        return openCells.get(random.nextInt(openCells.size()));
    }

    
    private double calculateBeepProbability(Cell cell, Cell cell2) {
        int d = getDistance(cell, cell2);
        return Math.exp(-alpha * d - 1);
    }

    private void updateBotProbabilities() {
        double totalProbability = 0.0;

        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                if (isOpen(new Cell(i, j))) {
                    double beepProbability = calculateBeepProbability(new Cell(i, j), botPosition);
                    probabilityGrid[i][j] = probabilityGrid[i][j] * (1.0 - beepProbability);
                } 

                totalProbability += probabilityGrid[i][j];
            }
        }

        // Normalize probabilities
        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                probabilityGrid[i][j] /= totalProbability;
            }
        }
    }

    private int getDistance(Cell start, Cell target) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}; // Assuming movement in 4 directions

        int rows = probabilityGrid.length;
        int cols = probabilityGrid[0].length;
        boolean[][] visited = new boolean[rows][cols];

        Queue<Cell> queue = new LinkedList<>();
        queue.add(start);
        visited[start.getX()][start.getY()] = true;

        int distance = 0;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            for (int i = 0; i < levelSize; i++) {
                Cell current = queue.poll();

                if (current.equals(target)) {
                    return distance; // Shortest distance found.
                }

                for (int[] direction : directions) {
                    int newRow = current.getX() + direction[0];
                    int newCol = current.getY() + direction[1];

                    if (isValid(newRow, newCol, rows, cols) && probabilityGrid[newRow][newCol] == 0 && !visited[newRow][newCol]) {
                        Cell nextCell = new Cell(newRow, newCol);
                        queue.add(nextCell);
                        visited[newRow][newCol] = true;
                    }
                }
            }

            distance++; // Move to the next level (increasing distance).
        }

        // If no path is found, return -1 (or any suitable value to indicate no path).
        return -1;
    }
    
    public boolean isOpen(Cell cell) {
        // Implement logic to check if a cell is open.
        return ship.getOpenCells().contains(cell);
    }

    private boolean sense() {
        double beepProbability = calculateBeepProbability(botPosition,leakPosition);
        double num = random.nextDouble();

        if (num > beepProbability) {
            return true;
        } else {
            return false;
        }
    }

    private List<Cell> pathFinder() {
        double maximumProbability = 0.0;
        List<Cell> bestCells = new ArrayList<>();

        for (int i = 0; i < shipSize; i++) {
            for (int j = 0; j < shipSize; j++) {
                if (probabilityGrid[i][j] > maximumProbability) {
                    maximumProbability = probabilityGrid[i][j];
                    bestCells.clear();
                    bestCells.add(new Cell(i, j));
                } else if (probabilityGrid[i][j] == maximumProbability) {
                    bestCells.add(new Cell(i, j));
                }
            }
        }

        if (bestCells.size() > 1) {
            bestCells.sort((cell1, cell2) -> Integer.compare(getDistance(botPosition, cell1), getDistance(botPosition, cell2)));
        }

        Cell chosenCell;

        if (bestCells.isEmpty()) {
            chosenCell = botPosition;
        } else {
            chosenCell = bestCells.get(0);
        }

        // Use BFS to move the bot to the chosen cell (implement BFS logic)
        // Check if the bot is on the leak
        if (chosenCell.equals(leakPosition)) {
            putOutLeak();
        }

        return findPathToChosenCell(chosenCell, probabilityGrid);
    }

    private List<Cell> findPathToChosenCell(Cell chosenCell, double[][] grid) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}; // Assuming movement in 4 directions
    
        int rows = grid.length;
        int cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Cell[][] parent = new Cell[rows][cols];
    
        Queue<Cell> queue = new LinkedList<>();
        queue.add(botPosition);
        visited[botPosition.getX()][botPosition.getY()] = true;
    
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
    
            if (current.equals(chosenCell)) {
                // Path found, backtrack to get the full path.
                return getPath(botPosition, chosenCell, parent);
            }
    
            for (int[] direction : directions) {
                int newRow = current.getX() + direction[0];
                int newCol = current.getY() + direction[1];
    
                if (isValid(newRow, newCol, rows, cols) && grid[newRow][newCol] == 0 && !visited[newRow][newCol]) {
                    Cell nextCell = new Cell(newRow, newCol);
                    queue.add(nextCell);
                    visited[newRow][newCol] = true;
                    parent[newRow][newCol] = current;
                }
            }
        }
    
        // No path found, return an empty list.
        return new ArrayList<>();
    }

    private boolean isValid(int x, int y, int rows, int cols) {
        return x >= 0 && x < rows && y >= 0 && y < cols;
    }
    
    private List<Cell> getPath(Cell start, Cell end, Cell[][] parent) {
        List<Cell> path = new ArrayList<>();
        Cell current = end;
    
        while (!current.equals(start)) {
            path.add(current);
            current = parent[current.getX()][current.getY()];
        }
    
        Collections.reverse(path);
        return path;
    }

    private void putOutLeak() {
        // Implement logic to put out the leak
    }

    // Implement additional methods such as game loop, updates, etc.

    // Cell class may be a part of Ship or its own file depending on the structure of your project
}

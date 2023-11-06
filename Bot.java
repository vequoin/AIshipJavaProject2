import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Bot {
    private Ship ship;
    private Cell position;
    private int strategy;
    private boolean isAlive;
    private int k;

    public Bot(Ship ship, int botNumber, Cell position, int k) {
        this.ship = ship;
        this.position = position;
        this.strategy = botNumber;
        this.isAlive = true;
        this.k = k;
    }

    public void move(Cell newPosition) {
        this.position = newPosition;
    }

    public List<Cell> getPossibleMoves() {
        return this.ship.getOpenNeighbors(this.position);
    }

    public void sense() {
        List<Cell> detectionGrid = generateSenseGrid();
        // Perform sensing logic here using the detectionGrid
    }

    private List<Cell> generateSenseGrid() {
        int x = this.position.x;
        int y = this.position.y;

        // Determine the detection square bounds
        int leftBound = x - this.k;
        int rightBound = x + this.k;
        int upperBound = y - this.k;
        int lowerBound = y + this.k;

        // Get all open cells from the ship
        Set<Cell> allOpenCells = this.ship.getOpenCells().stream().collect(Collectors.toSet());

        // Collect cells that are inside the detection square
        List<Cell> detectionGrid = new ArrayList<>();
        for (int i = leftBound; i <= rightBound; i++) {
            for (int j = upperBound; j <= lowerBound; j++) {
                Cell cell = new Cell(i, j);
                if (allOpenCells.contains(cell)) {
                    detectionGrid.add(cell);
                }
            }
        }
        return detectionGrid;
    }

    // Additional methods such as getters, setters, and toString may be required

    // Cell class for representing position. Assuming this is a static nested class inside Ship
    public static class Cell {
        int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell)) return false;
            Cell cell = (Cell) o;
            return x == cell.x && y == cell.y;
        }

        @Override
        public int hashCode() {
            // Use a prime number like 31 for hash calculation
            int result = Integer.hashCode(x);
            result = 31 * result + Integer.hashCode(y);
            return result;
        }
    }
}

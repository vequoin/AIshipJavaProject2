import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Ship {
    private String name;
    private int D;
    private int[][] ship;
    private List<Cell> leaks;
    private List<Cell> openCells;

    private static final Random random = new Random();

    private class Cell {
        int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public Ship(int D) {
        this.name = "Archaeopteryx";
        this.D = D;
        this.ship = generateShip();
        this.leaks = new ArrayList<>();
        this.openCells = getOpenCells();
    }

    private int[][] generateShip() {
        int[][] ship = new int[this.D][this.D];

        Cell startCell = new Cell(random.nextInt(this.D), random.nextInt(this.D));
        ship[startCell.x][startCell.y] = 0;

        Set<Cell> fringeCells = new HashSet<>(getNeighbors(startCell));

        while (!fringeCells.isEmpty()) {
            List<Cell> validCells = new ArrayList<>();
            for (Cell cell : fringeCells) {
                if (ship[cell.x][cell.y] == 1) {
                    int openCount = 0;
                    for (Cell n : getNeighbors(cell)) {
                        if (ship[n.x][n.y] == 0) openCount++;
                    }
                    if (openCount == 1) validCells.add(cell);
                }
            }

            if (validCells.isEmpty()) break;

            Cell chosenCell = validCells.get(random.nextInt(validCells.size()));
            ship[chosenCell.x][chosenCell.y] = 0;

            fringeCells.remove(chosenCell);
            fringeCells.addAll(getNeighbors(chosenCell));
        }

        eliminateDeadEnds(ship);
        return ship;
    }

    private void eliminateDeadEnds(int[][] ship) {
        List<Cell> deadEnds;
        do {
            deadEnds = new ArrayList<>();
            for (int row = 0; row < this.D; row++) {
                for (int col = 0; col < this.D; col++) {
                    if (ship[row][col] == 0) {
                        int openCount = 0;
                        for (Cell n : getNeighbors(new Cell(row, col))) {
                            if (ship[n.x][n.y] == 0) openCount++;
                        }
                        if (openCount == 1) deadEnds.add(new Cell(row, col));
                    }
                }
            }

            int requiredLength = deadEnds.size() / 2;

            while (requiredLength < deadEnds.size()) {
                Cell randomDeadEnd = deadEnds.get(random.nextInt(deadEnds.size()));
                List<Cell> neighbors = getNeighbors(randomDeadEnd);
                List<Cell> closedNeighbors = new ArrayList<>();
                for (Cell n : neighbors) {
                    if (ship[n.x][n.y] == 1) closedNeighbors.add(n);
                }

                if (closedNeighbors.isEmpty()) return;
                
                Cell cellToOpen = closedNeighbors.get(random.nextInt(closedNeighbors.size()));
                ship[cellToOpen.x][cellToOpen.y] = 0;
            }
        } while (requiredLength < deadEnds.size());
    }

    private List<Cell> getOpenCells() {
        List<Cell> openCells = new ArrayList<>();
        for (int i = 0; i < this.D; i++) {
            for (int j = 0; j < this.D; j++) {
                if (this.ship[i][j] == 0) {
                    openCells.add(new Cell(i, j));
                }
            }
        }
        return openCells;
    }

    private List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int[] directions = new int[]{-1, 0, 1, 0, -1}; // used for finding neighbors in a grid
        for (int i = 0; i < 4; i++) {
            int xi = cell.x + directions[i];
            int yi = cell.y + directions[i + 1];
            if (0 <= xi && xi < this.D && 0 <= yi && yi < this.D) {
                neighbors.add(new Cell(xi, yi));
            }
        }
        return neighbors;
    }

    public int getLength() {
        return this.D;
    }

    @Override
    public String toString() {
        StringBuilder gridStr = new StringBuilder();
        for (int[] row : this.ship) {
            for (int cell : row) {
                gridStr.append(cell == 0 ? "0 " : "1 ");
            }
            gridStr.append("\n");
        }
        return gridStr.toString();

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;

import org.junit.Before;
import org.junit.Test;

public class TestGameOfLife {
	private static RunFrame rf;
	private GameCell[][] cells;
	private int stopMiddle;
	private int stopEnd;
	private int rows;
	private int columns;

	@Before
	public void initCameCellArray() {
		this.rows = 10;
		this.columns = 10;
		if(rf == null){
			rf = new RunFrame(rows,columns);
		}
			
		cells = rf.getGameCellArray();
		stopMiddle = 1000;
		stopEnd = 2000;
	}

	@Test
	public void testInitGameFrame() {
		assertNotNull(rf);
		assertEquals(rf.getRows(), rows);
		assertEquals(rf.getColumns(), columns);
	}
	
	@Test
	public void testInitGamePanel() {
		assertNotNull(cells);
		assertEquals(rf.getRows(), cells.length);
		assertEquals(rf.getColumns(), cells[0].length);

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				assertEquals(i, cells[i][j].getRow());
				assertEquals(j, cells[i][j].getCol());
				assertEquals(CellStatus.Dead, cells[i][j].getStatus());
			}
		}
	}

	@Test
	public void testInitGameCell() {
		GameCell cell = cells[0][0];
		assertNotNull(cell);
		assertEquals(0, cell.getRow());
		assertEquals(0, cell.getCol());
		assertTrue("The initial status of a cell should be dead: ", cell
				.getStatus().equals(CellStatus.Dead));
	}

	@Test
	public void testClearCell() {
		rf.clear();

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				assertTrue("The status should be dead", cells[i][j].getStatus()
						.equals(CellStatus.Dead));
			}
		}
	}

	@Test
	public void testChangeStatus() {
		rf.clear();

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j].changeStatus();
				assertEquals(CellStatus.Alive, cells[i][j].getStatus());
			}
		}

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j].changeStatus();
				assertEquals(CellStatus.Dead, cells[i][j].getStatus());
			}
		}
	}

	@Test
	public void testNeighbours() {
		//up-left corner
		GameCell cell = cells[0][0];
		
		GameCell[] expectedNeighbours = rf.getNeighbours(cell);
		GameCell[] currentNeighbours = new GameCell[] { new GameCell(9,9),
				new GameCell(9, 0), new GameCell(9, 1), new GameCell(0, 1),
				new GameCell(1, 1), new GameCell(1, 0), new GameCell(1, 9),
				new GameCell(0, 9) };
		assertEquals(8, expectedNeighbours.length);
		assertArrayEquals(expectedNeighbours, currentNeighbours);
		
		//up-right corner
		cell = cells[0][9];

		expectedNeighbours = rf.getNeighbours(cell);
		currentNeighbours = new GameCell[] { new GameCell(9, 8),
				new GameCell(9,9), new GameCell(9, 0), new GameCell(0, 0),
				new GameCell(1, 0), new GameCell(1, 9), new GameCell(1, 8),
				new GameCell(0, 8) };
		assertEquals(8, expectedNeighbours.length);
		assertArrayEquals(expectedNeighbours, currentNeighbours);
		
		//normal cell
		cell = cells[5][5];

		expectedNeighbours = rf.getNeighbours(cell);
		currentNeighbours = new GameCell[] { cells[4][4], cells[4][5],
				cells[4][6], cells[5][6], cells[6][6], cells[6][5],
				cells[6][4], cells[5][4] };
		assertEquals(8, expectedNeighbours.length);
		assertArrayEquals(expectedNeighbours, currentNeighbours);
		
		//bottom-left corner
		cell = cells[9][0];

		expectedNeighbours = rf.getNeighbours(cell);
		currentNeighbours = new GameCell[] { cells[8][9], cells[8][0],
				cells[8][1], cells[9][1], cells[0][1], cells[0][0],
				cells[0][9], cells[9][9] };
		assertEquals(8, expectedNeighbours.length);
		assertArrayEquals(expectedNeighbours, currentNeighbours);

		//bottom-right corner
		cell = cells[9][9];
		expectedNeighbours = rf.getNeighbours(cell);
		currentNeighbours = new GameCell[] { cells[8][8], cells[8][9],
				cells[8][0], cells[9][0], cells[0][0], cells[0][9],
				cells[0][8], cells[9][8] };
		assertEquals(8, expectedNeighbours.length);
		assertArrayEquals(expectedNeighbours, currentNeighbours);
	}

	/*
	 * Any live cell with fewer than two live neighbours dies, as if caused by
	 * under-population.
	 */
	@Test
	public void testNextStatusRule_01_01() {
		GameCell cell = cells[1][1];
		assertNotNull(cell);

		for (int i = 0; i < 2; i++) {
			rf.clear();
			cell.setStatus(CellStatus.Alive);
			assertTrue("The status of cell should be alive", cell.getStatus()
					.equals(CellStatus.Alive));
			
			setAliveNeighbours(cell, i);
			assertEquals(i,
					cell.getNumberOfAliveNeighbours(rf.getNeighbours(cell)));
			
			CellStatus status = cell.getNextStatus(rf.getNeighbours(cell));
			assertTrue("It should be dead",	status.equals(CellStatus.Dead));
		}
	}
	
	/*
	 * Any live cell with fewer than two live neighbours dies, as if caused by
	 * under-population.
	 * 
	 *  before:
	 *  	 - - -
	 *  	 -[o]-
	 *  	 - - -
	 *  after:
	 *  	 - - -
	 *  	 -[-]-
	 *  	 - - -
	 */
	@Test
	public void testNextStatusRule_01_02() {
		try {
			rf.clear();
						
			//main cell without any alive neighbour
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();

			// assert
			// main cell
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			// neighbors
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Dead, cells[4][6].getStatus());
			assertEquals(CellStatus.Dead, cells[5][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Dead, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Any live cell with fewer than two live neighbours dies, as if caused by
	 * under-population.
	 * 
	 *  before:
	 *  	 o - -
	 *  	 -[o]-
	 *  	 - - -
	 *  after:
	 *  	 - - -
	 *  	 -[-]-
	 *  	 - - -
	 */
	@Test
	public void testNextStatusRule_01_03() {
		try{
			rf.clear();
			
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//only one neighbour is alive
			cells[4][4].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();

			// assert
			// main cell
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			// neighbors
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Dead, cells[4][6].getStatus());
			assertEquals(CellStatus.Dead, cells[5][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Dead, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Any live cell with two or three live neighbours lives on to the next
	 * generation.
	 */
	@Test
	public void testNextStatusRule_02_01() {
		GameCell cell = cells[1][1];
		assertNotNull(cell);

		for (int i = 1; i <= 8; i++) {
			rf.clear();
			cell.setStatus(CellStatus.Alive);
			assertTrue("The status of cell should be alive", cell.getStatus()
					.equals(CellStatus.Alive));
			
			setAliveNeighbours(cell, i);
			GameCell[] neighbours = rf.getNeighbours(cell);
			assertEquals(i,
					cell.getNumberOfAliveNeighbours(neighbours));

			CellStatus status = cell.getNextStatus(neighbours);
			if (i == 2 || i == 3) {
				assertTrue("It should be alive",
						status.equals(CellStatus.Alive));
			} else {
				assertTrue("It should be dead", status.equals(CellStatus.Dead));
			}
		}
	}
	
	/*
	 * Any live cell with two or three live neighbours lives on to the next
	 * generation.
	 * 
	 *  before:
	 *  	 o - -
	 *  	 -[o]-
	 *  	 - - o
	 *  after:
	 *  	 - - -
	 *  	 -[o]-
	 *  	 - - -
	 */
	@Test
	public void testNextStatusRule_02_02() {
		try {
			rf.clear();
			
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//two neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Alive, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Dead, cells[4][6].getStatus());
			assertEquals(CellStatus.Dead, cells[5][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Dead, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
			
	/*
	 * Any live cell with two or three live neighbours lives on to the next
	 * generation.
	 * 
	 *  before:
	 *  	 o - -
	 *  	 -[o]o
	 *  	 - - o
	 *  after:
	 *  	 - o -
	 *  	 -[o]o
	 *  	 - o o
	 */
	@Test
	public void testNextStatusRule_02_03() {
		try {			
			rf.clear();
			Thread.sleep(stopMiddle);
			
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//three neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			cells[5][6].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Alive, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Alive, cells[4][5].getStatus());
			assertEquals(CellStatus.Dead, cells[4][6].getStatus());
			assertEquals(CellStatus.Alive, cells[5][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][5].getStatus());
			assertEquals(CellStatus.Dead, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Any live cell with more than three live neighbours dies, as if by
	 * overcrowding.
	 */
	@Test
	public void testNextStatusRule_03_01() {
		GameCell cell = cells[1][1];
		assertNotNull(cell);

		cell.setStatus(CellStatus.Alive);
		assertTrue("The status of cell should be alive", cell.getStatus()
				.equals(CellStatus.Alive));

		for (int i = 4; i <= 8; i++) {
			rf.clear();
			setAliveNeighbours(cell, i);
			assertEquals(i,
					cell.getNumberOfAliveNeighbours(rf.getNeighbours(cell)));
			CellStatus status = cell.getNextStatus(rf.getNeighbours(cell));
			assertTrue("It should be dead", status.equals(CellStatus.Dead));
		}
	}
		
	/*
	 * Any live cell with more than three live neighbours dies, as if by
	 * overcrowding.
	
	 *  before:
	 *  	 o - o
	 *  	 -[o]o
	 *  	 - - o
	 *  after:
	 *  	 - - o -
	 *  	 -[-]o o
	 *  	 - o o -
	 */
	@Test
	public void testNextStatusRule_03_02() {
		try {
			rf.clear();
		
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//four neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			cells[5][6].setStatus(CellStatus.Alive);
			cells[4][6].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Dead, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Alive, cells[4][6].getStatus());
			assertEquals(CellStatus.Alive, cells[5][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][5].getStatus());
			assertEquals(CellStatus.Dead, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Any live cell with more than three live neighbours dies, as if by
	 * overcrowding.
	
	 *  before:
	 *  	 o - o
	 *  	 -[o]o
	 *  	 o - o
	 *  after:
	 *  	 - - o -
	 *  	 o[-]o o
	 *  	 - - o -
	 */
	@Test
	public void testNextStatusRule_03_03() {
		try {
			rf.clear();
		
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//five neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			cells[5][6].setStatus(CellStatus.Alive);
			cells[4][6].setStatus(CellStatus.Alive);
			cells[6][4].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Dead, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Alive, cells[4][6].getStatus());
			assertEquals(CellStatus.Alive, cells[5][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Dead, cells[6][4].getStatus());
			assertEquals(CellStatus.Alive, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Any live cell with more than three live neighbours dies, as if by
	 * overcrowding.
	
	 *  before:
	 *  	 o - o
	 *  	 -[o]o
	 *  	 o o o
	 *  after:
	 *  	 - - o -
	 *  	 -[-]- o
	 *  	 o - o -
	 *       - o - -
	 */
	@Test
	public void testNextStatusRule_03_04() {
		try {
			rf.clear();
		
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//six neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			cells[5][6].setStatus(CellStatus.Alive);
			cells[4][6].setStatus(CellStatus.Alive);
			cells[6][4].setStatus(CellStatus.Alive);
			cells[6][5].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Dead, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Alive, cells[4][6].getStatus());
			assertEquals(CellStatus.Dead, cells[5][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Alive, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Any live cell with more than three live neighbours dies, as if by
	 * overcrowding.
	
	 *  before:
	 *  	 o - o
	 *  	 0[o]o
	 *  	 o o o
	 *  after:
	 *  	 - o - o -
	 *  	 o -[-]- o
	 *  	 - o - o -
	 *       - - o - -
	 */
	@Test
	public void testNextStatusRule_03_05() {
		try {
			rf.clear();
		
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//seven neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			cells[5][6].setStatus(CellStatus.Alive);
			cells[4][6].setStatus(CellStatus.Alive);
			cells[6][4].setStatus(CellStatus.Alive);
			cells[6][5].setStatus(CellStatus.Alive);
			cells[5][4].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Dead, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Alive, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Alive, cells[4][6].getStatus());
			assertEquals(CellStatus.Dead, cells[5][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Alive, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Any live cell with more than three live neighbours dies, as if by
	 * overcrowding.
	
	 *  before:
	 *  	 o o o
	 *  	 0[o]o
	 *  	 o o o
	 *  after:
	 *       - - o - - 
	 *  	 - o - o -
	 *  	 o -[-]- o
	 *  	 - o - o -
	 *       - - o - -
	 */
	@Test
	public void testNextStatusRule_03_06() {
		try {
			rf.clear();
		
			//mian cell
			cells[5][5].setStatus(CellStatus.Alive);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//eight neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			cells[5][6].setStatus(CellStatus.Alive);
			cells[4][6].setStatus(CellStatus.Alive);
			cells[6][4].setStatus(CellStatus.Alive);
			cells[6][5].setStatus(CellStatus.Alive);
			cells[5][4].setStatus(CellStatus.Alive);
			cells[4][5].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Dead, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Alive, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Alive, cells[4][6].getStatus());
			assertEquals(CellStatus.Dead, cells[5][6].getStatus());
			assertEquals(CellStatus.Alive, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Alive, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());
			
			Thread.sleep(stopEnd);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Any dead cell with exactly three live neighbours becomes a live cell, as
	 * if by reproduction.
	 */
	@Test
	public void testNextStatusRule_04_01() {
		GameCell cell = cells[1][1];
		assertNotNull(cell);

		cell.setStatus(CellStatus.Dead);
		assertTrue("The status of cell should be dead", cell.getStatus()
				.equals(CellStatus.Dead));

		for (int i = 1; i <= 8; i++) {
			rf.clear();
			setAliveNeighbours(cell, i);
			assertEquals(i,
					cell.getNumberOfAliveNeighbours(rf.getNeighbours(cell)));
			CellStatus status = cell.getNextStatus(rf.getNeighbours(cell));
			if (i == 3) {
				assertTrue("It should be alive",
						status.equals(CellStatus.Alive));
			} else {
				assertTrue("It should be dead", status.equals(CellStatus.Dead));
			}
		}
	}

	/*
	 * Any live cell with more than three live neighbours dies, as if by
	 * overcrowding.
	
	 *  before:
	 *  	 o - -
	 *  	 -[-]o
	 *  	 - - o
	 *  after:
	 *       - - -
	 *       -[o]- 
	 *  	 - - -
	 */
	@Test
	public void testNextStatusRule_04_02() {
		try {
			rf.clear();
		
			//mian cell
			cells[5][5].setStatus(CellStatus.Dead);
			cells[5][5].setBorder(BorderFactory.createLineBorder(Color.red));
			//three neighbours are alive
			cells[4][4].setStatus(CellStatus.Alive);
			cells[6][6].setStatus(CellStatus.Alive);
			cells[5][6].setStatus(CellStatus.Alive);
			
			rf.repaint();
			Thread.sleep(stopMiddle);
			
			getNextStatus(5,5); 
	
			rf.repaint();
			
			// main cell 
			assertEquals(CellStatus.Alive, cells[5][5].getStatus());
			// neighbours
			assertEquals(CellStatus.Dead, cells[4][4].getStatus());
			assertEquals(CellStatus.Dead, cells[4][5].getStatus());
			assertEquals(CellStatus.Dead, cells[4][6].getStatus());
			assertEquals(CellStatus.Dead, cells[5][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][6].getStatus());
			assertEquals(CellStatus.Dead, cells[6][5].getStatus());
			assertEquals(CellStatus.Dead, cells[6][4].getStatus());
			assertEquals(CellStatus.Dead, cells[5][4].getStatus());

			Thread.sleep(stopEnd);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setAliveNeighbours(GameCell cell, int numberOfAliveNeighbours) {
		Random r = new Random();
		GameCell[] neighbours = rf.getNeighbours(cell);

		Set<Integer> set = new HashSet<Integer>();

		while (set.size() < numberOfAliveNeighbours) {
			set.add(r.nextInt(8));
		}

		for (Integer i : set) {
			cells[neighbours[i].getRow()][neighbours[i].getCol()]
					.setStatus(CellStatus.Alive);
		}
	}
	
	private void getNextStatus(int row, int col) {
		CellStatus[][] status = new CellStatus[rows][columns];
		
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				status[i][j] = cells[i][j].getNextStatus(rf.getNeighbours(cells[i][j]));
			}
		}
		

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				cells[i][j].setStatus(status[i][j]);
			}
		}
	}
}

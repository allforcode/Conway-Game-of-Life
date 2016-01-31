import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.util.Random;

public class RunFrame extends JFrame implements Runnable {
	private static final long serialVersionUID = 5300492314595916580L;
	
	private RunFrame rf;
	
	private int width;
	private int height;
	private int rows;
	private int columns;
	private int count;
	private int threadSpeed;
	private int speedStep;
	public int cellWidth;
	public int cellHeight;
	
	private String countLabelName;
	
	private String[] patterns;
	private GameCell[][] cells;
	private CellStatus[][] status;
	
	private JPanel topPanel, mainPanel;
	private JButton jbtn1, jbtn2, jbtn3, jbtn6, jbtn7;
	private JComboBox<String> jcb;
	private JLabel jlb, countLabel;
	
	private Thread t;
	
	private boolean stop = false;
	private boolean waitFlag = false;
	private boolean stepWaitFlag = false;
	private boolean stepSwitchOn = false;
	

	public static void main(String[] args) {
		new RunFrame(70, 120);
//		new RunFrame(40, 40);
//		new RunFrame(6, 6);
	}

	public RunFrame(int rows, int columns) {
		rf = this;
		
		if (rows < 5 || rows > 70) {
			this.rows = 70;
		}else{
			this.rows = rows;
		}
		
		if (columns < 5 || columns > 120) {
			this.columns = 120;
		}else{
			this.columns = columns;
		}
		
		this.count = 0;
		this.threadSpeed = 20;
		this.speedStep = 10;
		
		setTitle("Conway's Game of Life");
		setSize(1000, 800);
		setLocation(GameUtil.getScreenMiddleLocationX(this.getWidth()),
				GameUtil.getScreenMiddleLocationY(this.getHeight()));
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		width = this.getWidth() - 16;
		height = this.getHeight() - 36 - 35;

		topPanel = new JPanel();
		topPanel.setVisible(true);
		jbtn1 = new JButton("Start");
		jbtn1.addActionListener(new BtnActionListener());
		jbtn1.setSize(200, 36);
		jbtn2 = new JButton("Step");
		jbtn2.addActionListener(new BtnActionListener());
		jbtn3 = new JButton("Reset");
		jbtn3.addActionListener(new BtnActionListener());

		patterns = new String[] { "Patterns", "Gosper Glider Gun", "Acorn",
				"Random" };
		jcb = new JComboBox<String>(patterns);
		jcb.setSelectedIndex(0);
		jcb.setBackground(Color.white);
		jcb.addActionListener(new BtnActionListener());

		jbtn6 = new JButton("Slow Down");
		jbtn6.addActionListener(new BtnActionListener());
		jbtn7 = new JButton("Speed Up");
		jbtn7.addActionListener(new BtnActionListener());
		jlb = new JLabel("Speed: " + getThreadSpeed() + " ms");

		countLabelName = "Paint time: ";
		countLabel = new JLabel(countLabelName + getCount());

		topPanel.add(jbtn1);
		topPanel.add(jbtn2);
		topPanel.add(jbtn3);

		topPanel.add(jbtn6);
		topPanel.add(jbtn7);
		topPanel.add(jlb);
		topPanel.add(jcb);
		topPanel.add(countLabel);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.white);
		mainPanel.setLayout(new GridLayout(this.rows, this.columns, 0, 0));

		cells = new GameCell[this.rows][this.columns];
		status = new CellStatus[this.rows][this.columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				cells[i][j] = new GameCell(i, j, width, height, rows, columns);
				status[i][j] = CellStatus.Dead;
				mainPanel.add(cells[i][j]);
			}
		}
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.setVisible(true);

	}

	@Override
	public void run() {
//		System.out.println("thread run");
		while (!stop) {
			count++;
			countLabel.setText(countLabelName + getCount());

			try {
				synchronized (this) {
					
//					showStatusArrayToConsole("run");


					for (int i = 0; i < rows; i++) {
						for (int j = 0; j < columns; j++) {
							status[i][j] = cells[i][j].getNextStatus(this
									.getNeighbours(cells[i][j]));
						}
					}
					

					for (int i = 0; i < rows; i++) {
						for (int j = 0; j < columns; j++) {
							cells[i][j].setStatus(status[i][j]);
						}
					}
					
					repaint();
					pause();
					
					while (waitFlag || isAllDead()) {
						wait();
					}

					while (stepWaitFlag) {
						wait();
					}
					if (stepSwitchOn) {
						stepWaitFlag = true;
					}
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
//		showStatusArrayToConsole("paint");
	}
	
	public GameCell[] getNeighbours(GameCell cell) {
		GameCell[] temp = new GameCell[8];
		int row = cell.getRow();
		int col = cell.getCol();

		// top-left row-1,col-1
		temp[0] = cells[getTopRow(row - 1)][getLeftCol(col - 1)];
		
		// left row,col-1
		temp[7] = cells[row][getLeftCol(col - 1)];
		
		// bottom left row+1,col-1
		temp[6] = cells[getBottomRow(row + 1)][getLeftCol(col - 1)];
		
		// bottom row+1,col
		temp[5] = cells[getBottomRow(row + 1)][col];

		// bottom right row+1,col+1
		temp[4] = cells[getBottomRow(row + 1)][getRightCol(col + 1)];

		// right row,col+1
		temp[3] = cells[row][getRightCol(col + 1)];

		// top-right row-1,col+1
		temp[2] = cells[getTopRow(row - 1)][getRightCol(col + 1)];

		// top row-1, col
		temp[1] = cells[getTopRow(row - 1)][col];

		return temp;
	}
	
	private int getLeftCol(int neighbourCol) {
		return neighbourCol < 0 ? (columns-1) : neighbourCol;
	}

	private int getRightCol(int neighbourCol) {
		return neighbourCol > (columns-1) ? 0 : neighbourCol;
	}

	private int getTopRow(int neighbourRow) {
		return neighbourRow < 0 ? (rows - 1) : neighbourRow;
	}

	private int getBottomRow(int neighbourRow) {
		return neighbourRow > (rows - 1) ? 0 : neighbourRow;
	}
	
	// set the status of current thread to wait
	public void waitThread() {
		waitFlag = true;
	}

	// resume the waiting thread
	public synchronized void resume() {
		waitFlag = false;
		notifyAll();
	}

	// clear the panel
	public void clear() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				cells[i][j].setStatus(CellStatus.Dead);
				cells[i][j].setResume(false);
			}
		}
		repaint();
	}

	// set a random pattern
	public void random() {
		this.clear();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				CellStatus c = getRandomStatus();
				cells[i][j].setStatus(c);
			}
		}
		repaint();
	}

	// set a pattern of Gosper Glider Gun
	public void gosperGliderGun() {
		this.clear();

		// the first row of this pattern in the window
		int offsetRow = 10;

		// the first col of this pattern in the window
		int offsetCol = 10;

		boolean[][] array = new boolean[9][36];

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				array[i][j] = false;
			}
		}
		array[0][24] = true;
		array[1][22] = true;
		array[1][24] = true;
		array[2][12] = true;
		array[2][13] = true;
		array[2][20] = true;
		array[2][21] = true;
		array[2][34] = true;
		array[2][35] = true;
		array[3][11] = true;
		array[3][15] = true;
		array[3][20] = true;
		array[3][21] = true;
		array[3][34] = true;
		array[3][35] = true;
		array[4][0] = true;
		array[4][1] = true;
		array[4][10] = true;
		array[4][16] = true;
		array[4][20] = true;
		array[4][21] = true;
		array[5][0] = true;
		array[5][1] = true;
		array[5][10] = true;
		array[5][14] = true;
		array[5][16] = true;
		array[5][17] = true;
		array[5][22] = true;
		array[5][24] = true;
		array[6][10] = true;
		array[6][16] = true;
		array[6][24] = true;
		array[7][11] = true;
		array[7][15] = true;
		array[8][12] = true;
		array[8][13] = true;

		for (int i = offsetRow; i < array.length + offsetRow; i++) {
			for (int j = offsetCol; j < array[0].length + offsetCol; j++) {
				if (array[i - offsetRow][j - offsetCol]) {
					cells[i][j].setStatus(CellStatus.Alive);
				} else {
					continue;
				}
			}
		}
		repaint();
	}

	// set a pattern of Acorn
	public void acorn() {
		this.clear();

		// the first row of this pattern in the window
		int offsetRow = 30;

		// the first col of this pattern in the window
		int offsetCol = 60;

		boolean[][] array = new boolean[3][7];

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				array[i][j] = false;
			}
		}

		array[0][1] = true;
		array[1][3] = true;
		array[2][0] = true;
		array[2][1] = true;
		array[2][4] = true;
		array[2][5] = true;
		array[2][6] = true;

		for (int i = offsetRow; i < array.length + offsetRow; i++) {
			for (int j = offsetCol; j < array[0].length + offsetCol; j++) {
				if (array[i - offsetRow][j - offsetCol]) {
					cells[i][j].setStatus(CellStatus.Alive);
				} else {
					continue;
				}
			}
		}
		repaint();
	}

	private CellStatus getRandomStatus() {
		Random r = new Random();
		if (r.nextInt(100) % 5 == 0) {
			return CellStatus.Alive;
		}
		return CellStatus.Dead;
	}

	// slow down the thread
	public void slowDown() {
		this.threadSpeed += speedStep;
	}

	// speed up the thread
	public void speedUp() {
		if (this.threadSpeed >= speedStep) {
			this.threadSpeed -= speedStep;
		} else {
			this.threadSpeed = 0;
		}
	}

	public void pause() throws InterruptedException {
		Thread.sleep(this.threadSpeed);
	}

	public boolean isAllDead() {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if ((cells[i][j].getStatus()).equals(CellStatus.Alive)) {
					return false;
				}
			}
		}
		return true;
	}

	public int getThreadSpeed() {
		return threadSpeed;
	}

	public void setThreadSpeed(int threadSpeed) {
		this.threadSpeed = threadSpeed;
	}

	class BtnActionListener implements ActionListener {
		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == jbtn1) {
				if (jbtn1.getText().equalsIgnoreCase("start")
						|| jbtn1.getText().equalsIgnoreCase("resume")) {

					if (!isAllDead()) {

						jbtn1.setText("Pause");
						jbtn2.setEnabled(false);

						if (t != null && t.isAlive()) {

							stepWaitFlag = false;
							stepSwitchOn = false;

							resume();

						} else {
							t = new Thread(rf);
							t.start();
						}
					} else {
						JOptionPane.showMessageDialog(rf,
								"Please choose a pattern!");
					}
				} else if (jbtn1.getText().equalsIgnoreCase("pause")) {
					jbtn1.setText("Resume");
					jbtn2.setEnabled(true);
					waitThread();
				}
			} else if (e.getSource() == jbtn2) {
				if (!isAllDead()) {
					//customized cells need press "Step" twice if there is no resume();
					resume();
					jbtn1.setText("Start");
					if (t == null || !t.isAlive()) {
						t = new Thread(rf);
						t.start();
					}
					stepRun();
				} else {
					JOptionPane.showMessageDialog(rf,
							"Please choose a pattern!");
				}
			} else if (e.getSource() == jbtn3) {
				reset();
				clear();
			} else if (e.getSource() == jbtn6) {
				slowDown();
				jlb.setText("Speed: " + getThreadSpeed() + " ms");
			} else if (e.getSource() == jbtn7) {
				speedUp();
				jlb.setText("Speed: " + getThreadSpeed() + " ms");
			} else if (e.getSource() == jcb) {
				jcb = (JComboBox<String>) e.getSource();
				if (!jcb.getSelectedItem().toString().equals(patterns[0])) {
					newPattern();
					if (jcb.getSelectedItem().toString().equals(patterns[1])) {
						gosperGliderGun();
					} else if (jcb.getSelectedItem().toString()
							.equals(patterns[2])) {
						acorn();
					} else if (jcb.getSelectedItem().toString()
							.equals(patterns[3])) {
						random();
					} 
				}else{
					reset();
					clear();
					setCount(0);
				}
			}
		}
	}

	private void stepRun() {
		stepSwitchOn = true;
		if (!stepWaitFlag) {
			stepWaitFlag = true;
		} else {
			stepWaitFlag = false;
		}
		resume();
//		System.out.println("steprun: " + this.waitFlag + ", " + this.stepWaitFlag);
	}

	private void reset() {
		if (jbtn1.getText().equalsIgnoreCase("pause")) {
			waitThread();
		}
		resetSpeed();
		jcb.setSelectedIndex(0);
		resetButtons();
		clear();
	}
	
	private void resetSpeed(){
		setThreadSpeed(20);
		jlb.setText("Speed: " + getThreadSpeed() + " ms");
	}
	
	private void newPattern() {
		clear();
		if (jbtn1.getText().equalsIgnoreCase("pause")) {
			waitThread();
		}
		resetButtons();
	}
	
	private void resetButtons (){
		setCount(0);
		countLabel.setText(countLabelName + getCount());
		stepWaitFlag = false;
		stepSwitchOn = false;
		jbtn2.setEnabled(true);
		jbtn1.setText("Start");
	}

	// cannot trigger the event when select a same item, so change to
	// addActionListener
	// class PatternItemListener implements ItemListener {
	//
	// @Override
	// public void itemStateChanged(ItemEvent e) {
	// System.out.println("select");
	// if (e.getSource() == jcb) {
	// if (e.getItem().equals(patterns[1])) {
	// newPattern();
	// gosperGliderGun();
	// } else if (e.getItem().equals(patterns[2])) {
	// newPattern();
	// acorn();
	// } else if (e.getItem().equals(patterns[3])) {
	// newPattern();
	// random();
	// }
	// }
	// }
	// }

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public GameCell[][] getGameCellArray() {
		return cells;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
//	private void showStatusArrayToConsole(String method){
//		System.out.println("************** " + method + " **************");
//		for (int i = 0; i < cells.length; i++) {
//			System.out.print(method + ": ");
//			for (int j = 0; j < cells[i].length; j++) {
//				System.out.print((((cells[i][j].getStatus()).equals(CellStatus.Alive))?"*":"-") + " ");
//			}
//			System.out.println();
//		}
//		System.out.println("************** end **************");
//	}
}
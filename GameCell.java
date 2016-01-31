import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class GameCell extends JLabel {
	private int row;
	private int col;
	private CellStatus status;
	private GameCell gc;
	private int cellWidth;
	private int cellHeight;
	private boolean isResume = false;
	private static int count = 0;
	private int rows;
	private int columns;
	
	//for testing
	public GameCell(int row, int col) {
		gc = this;
		this.row = row;
		this.col = col;
	}

	public GameCell(int row, int col, int panelWidth, int panelHeight, int rows, int columns) {
//		System.out.println("GameCell_Constructor()");
		gc = this;
		this.row = row;
		this.col = col;
		this.rows = rows;
		this.columns = columns;
		this.cellWidth = panelWidth / columns;
		this.cellHeight = panelHeight / rows;
		this.status = getInitStatus();
		this.setSize(this.cellHeight,this.cellHeight);
		this.addMouseListener(new CellClickListener());
	}

	@Override
	public void paintComponent(Graphics g) {
//		System.out.println("GameCell_paintComponent()" + count++);
		super.paintComponent(g);

		if (this.getStatus().equals(CellStatus.Dead)) {
			if (isResume) {
				//pink
				g.setColor(new Color(252,204,241));
				g.fillRect(0, 0, cellWidth, cellHeight);
			}
			g.setColor(Color.lightGray);
			if( this.getRow() == (rows - 1) && this.getCol() == (columns - 1)){
				g.drawRect(0, 0, cellWidth - 1, cellHeight - 1);
			}else if( this.getRow() != (rows - 1) && this.getCol() == (columns - 1)){
				g.drawRect(0, 0, cellWidth - 1, cellHeight);
			}else if( this.getRow() == (rows - 1) && this.getCol() != (columns - 1)){
				g.drawRect(0, 0, cellWidth, cellHeight - 1);
			}else{
				g.drawRect(0, 0, cellWidth, cellHeight);
			}
		} else {
			g.setColor(Color.gray);
			g.fillRect(0, 0,  cellWidth, cellHeight);
		}
	}
	
	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		GameCell.count = count;
	}

	public boolean isResume() {
		return isResume;
	}

	public void setResume(boolean isResume) {
		this.isResume = isResume;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public CellStatus getStatus() {
		return status;
	}

	public void setStatus(CellStatus status) {
		this.status = status;
	}
	
	private CellStatus getInitStatus() {
		return CellStatus.Dead;
	}

	public int getNumberOfAliveNeighbours(GameCell[] neighbours) {
		int number = 0;

		for (GameCell c:neighbours) {
			if (c.getStatus().equals(CellStatus.Alive)) {
				number++;
			}
		}
		
		return number;
	}

	public CellStatus getNextStatus(GameCell[] neighbours) {
		int aliveNeighbours = getNumberOfAliveNeighbours(neighbours);
		CellStatus status = this.getStatus();
		CellStatus c = null;
		if (aliveNeighbours == 3) {
			c = CellStatus.Alive;
//			this.isResume = true;
		} else if (aliveNeighbours == 2) {
			c = this.getStatus();
		} else {
			c = CellStatus.Dead;
		}
		if(status.equals(CellStatus.Alive) && c.equals(CellStatus.Dead)){
			this.isResume = true;
		}
		return c;
	}
	
	public void changeStatus() {
		this.setStatus((this.getStatus().equals(CellStatus.Dead)) ? CellStatus.Alive
				: CellStatus.Dead);
	}

	class CellClickListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
//			System.out.println(gc.getRow() + ", " + gc.getCol());
			if (e.getSource() == gc) {
				gc.changeStatus();
//				System.out.println((gc.getRow() == (rows - 1)) + ", " + (gc.getCol() == (columns -1)));
			}
			gc.repaint();
		}
	}

	@Override
	public String toString() {
		return "[" + row + ", " + col + ", " + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameCell other = (GameCell) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
}
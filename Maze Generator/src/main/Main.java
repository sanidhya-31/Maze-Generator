package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

public class Main implements Runnable{
	
	public static Random rand = new Random();
	
	private JFrame frame;
	private Canvas canvas;
	private Thread thread;
	
	public static int WIDTH = 600,HEIGHT=600;
	public static int cols=20,rows=40;
	public static int w = WIDTH/cols , h = HEIGHT/rows;
	private BufferStrategy bs;
	private Graphics g;
	
	//REAL STUFF
	
	private Cell[] grid ;
	private Cell current=null;
	
	private ArrayList<Cell> stack;
	
	public Main(){
		frame=new JFrame("Maze Generator ~Designed by Sanidhya");
		frame.setSize(WIDTH,HEIGHT);
		frame.setVisible(true);;
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		canvas.setMaximumSize(new Dimension(WIDTH,HEIGHT));
		canvas.setMinimumSize(new Dimension(WIDTH,HEIGHT));
		
		frame.add(canvas);
		frame.pack();
		
		init();
		
	}
	
	public void init(){
		grid = new Cell[rows*cols];
		
		for(int y=0;y<rows;y++){
			for(int x=0;x<cols;x++){
				grid[y*cols+x] = new Cell(x,y);
			}
		}
		
		current = grid[0];
		stack = new ArrayList<Cell>();
	}
	
	public synchronized void start(){
		thread = new Thread(this);
		thread.start();
	}
	
	public void tick(){
		
	}
	
	public void render(){
		bs=canvas.getBufferStrategy();
		if(bs==null){
			canvas.createBufferStrategy(3);
			return;
		}
		g=bs.getDrawGraphics();
		
		//background
		g.clearRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, WIDTH,HEIGHT);
		//start draw
		
		for(int i=0;i<grid.length;i++){
			grid[i].render(g);
		}
		
		current.visited = true;
		current.highlight();
		//STEP 1
		Cell next = current.getRandomNeighbor();
		if(!next.undefined){
			next.visited=true;
			//STEP 2
			stack.add(current);
			//STEP 3
			removeWalls(current,next);
			//STEP 4
			current=next;
		}else if (stack.size()>0){//WHEN NO MORE NEIGHBORS LEFT, DO RECURSIVE BACKTRACKING		
			Cell cell = stack.remove(stack.size()-1);
			current=cell;
		}
		//end draw
		bs.show();
		g.dispose();
	}
	
	@Override
	public void run(){
		while(true){
			tick();
			render();
//			try {
//				thread.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	public void removeWalls(Cell a,Cell b){
		int x = a.x-b.x;
		if(x==1){
			a.walls[3]=false;
			b.walls[1]=false;
		}else if(x==-1){
			a.walls[1]=false;
			b.walls[3]=false;
		}
		int y = a.y-b.y;
		if(y==1){
			a.walls[0]=false;
			b.walls[2]=false;
		}else if(y==-1){
			a.walls[2]=false;
			b.walls[0]=false;
		}
		
	}
	
	public synchronized void stop(){
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Main main = new Main();
		main.start();
	}
	
	class Cell{
		public int x,y;
		public boolean[] walls;
		public boolean visited;
		public boolean undefined = false;
		Cell(int x,int y){
			this.x = x;
			this.y=y;
			walls= new boolean[]{true,true,true,true};//top right bottom left
			visited = false;
		}
		
		public void render(Graphics g){
			g.setColor(Color.WHITE);
			int x = this.x*Main.w;
			int y = this.y*Main.h;
			if(walls[0]) g.drawLine(x, y, x+Main.w, y);
			if(walls[1]) g.drawLine(x+Main.w, y, x+Main.w, y+Main.h);
			if(walls[2]) g.drawLine(x+Main.w, y+Main.h, x, y+Main.h);
			if(walls[3]) g.drawLine(x, y+Main.h, x,y);
			
			if(this.visited){
				g.setColor(new Color(34, 236, 233, 200));
				g.fillRect(this.x*Main.w, this.y*Main.h, Main.w, Main.h);
			}
		}
		
		public Cell getRandomNeighbor(){
			ArrayList<Cell> neighbors = new ArrayList<Cell>();
			Cell top=new Cell(0,0),right=new Cell(0,0),bottom=new Cell(0,0),left=new Cell(0,0);
			top.visited=true;
			right.visited=true;
			bottom.visited=true;
			left.visited=true;
			if(y>0) 			top = grid[index(x,y-1)];
			if(x<Main.cols-1)	right = grid[index(x+1,y)];
			if(y<Main.rows-1) 	bottom= grid[index(x,y+1)];
			if(x>0)				left= grid[index(x-1,y)];
			
			if(!top.visited) neighbors.add(top);
			if(!right.visited) neighbors.add(right);
			if(!bottom.visited) neighbors.add(bottom);
			if(!left.visited) neighbors.add(left);
			
			if(neighbors.size()>0){
				int r = Main.rand.nextInt(neighbors.size());
				return neighbors.get(r);
			}
			else{
				Cell cell = new Cell(0,0);
				cell.undefined=true;
				return cell;
			}
		}
		
		public int index(int x,int y){
			if(x<0||y<0||x>Main.cols-1||y>Main.rows-1)
				return -1;
			return x+y*cols;
		}
		public void highlight(){
			g.setColor(new Color(0,255,0,200));
			g.fillOval(this.x*Main.w+Main.w/4, this.y*Main.h+Main.h/4, Main.w/2, Main.h/2);
			g.setColor(new Color(0,155,0,200));
			g.drawOval(this.x*Main.w+Main.w/4, this.y*Main.h+Main.h/4, Main.w/2, Main.h/2);

		}
	}
}

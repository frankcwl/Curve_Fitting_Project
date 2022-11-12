package project1;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

public class MyPanel extends JPanel implements ActionListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;

	private static final int LAGRANGE = 1;
	private static final int SPLINE = 2;
	private static final int FITTING = 3;

	private UI ui;
	private double dx = 300;
	private double dy = 300;
	private double times = 1;
	private File file;
	private double mouse_x = 0;
	private double mouse_y = 0;
	private ArrayList<double[]> data = new ArrayList<>();
	private boolean isNew = true;
	private int drawMode = 0;
	private int fitTimes = 0;
	
	private void init() {
		// 使数据点位于屏幕中央
		double min_x = data.get(0)[0];
		double max_x = data.get(0)[0];
		double min_y = data.get(0)[1];
		double max_y = data.get(0)[1];
		for (double[] p : data) {
			min_x = Math.min(min_x, p[0]);
			max_x = Math.max(max_x, p[0]);
			min_y = Math.min(min_y, p[1]);
			max_y = Math.max(max_y, p[1]);
		}
		times = Math.max(max_x, max_y) / 600 * 1.5;
		double center_x = (min_x + max_x) / 2;
		double center_y = (min_y + max_y) / 2;
		dx = 300 - center_x / times;
		dy = 300 + center_y / times;
		
		drawMode = 0;
		isNew = true;
	}

	public MyPanel(UI ui) {
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.ui = ui;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		drawCoordinate(g2);

		// 绘制鼠标位置坐标
		g2.setColor(Color.BLUE);
		g2.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		g2.drawString(String.format("(%.0f, %.0f)", getReal_X(mouse_x), getReal_Y(mouse_y)),
				(float) mouse_x + 20, (float) mouse_y + 20);

		// 绘制点和点的坐标
		for (double[] point : data) {
			double x = point[0];
			double y = point[1];
			drawPoint(g2, getVirtual_X(x), getVirtual_Y(y));
			g2.drawString("(" + x + ", " + y + ")", (float) getVirtual_X(x) + 10, (float) getVirtual_Y(y) + 10);
		}
		
		// 绘制函数图像
		if (data.size() != 0) {
			Line2D line;
			g2.setColor(Color.BLUE);
			switch (drawMode) {
			case LAGRANGE:
				if (isNew) {
					Algorithm.lagrangeSet(data);
					isNew = false;
				}
				for (double i = 0; i < 600; i += 0.01) {
					line = new Line2D.Double(i, getVirtual_Y(Algorithm.calculate(getReal_X(i))),
							i + 1, getVirtual_Y(Algorithm.calculate(getReal_X(i + 1))));
					g2.draw(line);
				}
				showFunction(g2);
				break;
			case SPLINE:
				if (isNew) {
					Algorithm.splineSet(data);
					isNew = false;
				}
				for (double i = 0; i < 600; i += 0.01) {
					line = new Line2D.Double(i, getVirtual_Y(Algorithm.spline(getReal_X(i))),
							i + 1, getVirtual_Y(Algorithm.spline(getReal_X(i + 1))));
					g2.draw(line);
				}
				break;
			case FITTING:
				if (isNew) {
					Algorithm.fittingSet(fitTimes, data);
					isNew = false;
				}
				for (double i = 0; i < 600; i += 0.01) {
					line = new Line2D.Double(i, getVirtual_Y(Algorithm.calculate(getReal_X(i))),
							i + 1, getVirtual_Y(Algorithm.calculate(getReal_X(i + 1))));
					g2.draw(line);
				}
				showFunction(g2);
			}
			
		}
	}

	private double getReal_X(double x) {
		return (x - dx) * times;
	}
	
	private double getVirtual_X(double x) {
		return dx + x / times;
	}
	
	private double getReal_Y(double y) {
		return (dy - y) * times;
	}
	
	private double getVirtual_Y(double y) {
		return dy - y / times;
	}
	
	//绘制函数表达式
	private void showFunction(Graphics2D g2) {
		g2.setFont(new Font("News701 BT Italic", Font.BOLD, 20));
		g2.setColor(Color.BLUE.darker());
		double[] p = Algorithm.getP();
		StringBuffer sb = new StringBuffer("f (x) = ");
		for (int i = p.length - 1; i >= 0; i--) {
			if (p[i] > 0 && i != p.length - 1) sb.append(" + ");
			if (p[i] < 0) sb.append(" - ");
			if (p[i] == 0) continue;
			sb.append(String.format("%.2f", Math.abs(p[i])));
			if (i == 0) continue;
			sb.append("x");
			if (i == 1) continue;
			sb.append("^");
			sb.append(i);
		}
		g2.drawString(sb.toString(), 40, 100);
	}

	// 绘制坐标系
	private void drawCoordinate(Graphics2D g2) {
		int len = 40;
		Line2D line;
		double x_shift = dx;
		while ((x_shift += len) < 0);
		while ((x_shift -= len) > 0);
		double y_shift = dy;
		while ((y_shift += len) < 0);
		while ((y_shift -= len) > 0);
		for (int i = 0; i <= 600 / len; i++) {
			if (i * len + x_shift == dx) {
				g2.setColor(Color.BLACK);
			} else {
				g2.setColor(Color.GRAY.brighter());
			}
			line = new Line2D.Double(i * len + x_shift, 0, i * len + x_shift, 600);
			g2.draw(line);
			if (i * len + y_shift == dy) {
				g2.setColor(Color.BLACK);
			} else {
				g2.setColor(Color.GRAY.brighter());
			}
			line = new Line2D.Double(0, i * len + y_shift, 600, i * len + y_shift);
			g2.draw(line);
		}
		g2.setColor(Color.RED);
		Ellipse2D circle = new Ellipse2D.Double(dx - 3, dy - 3, 6, 6);
		g2.fill(circle);
	}

	// 绘制点
	private void drawPoint(Graphics2D g2, double x, double y) {
		g2.setColor(Color.BLACK);
		Ellipse2D circle = new Ellipse2D.Double(x - 3, y - 3, 6, 6);
		g2.fill(circle);
	}

	// 从文件中读取数据
	private boolean readData(Scanner sc) {
		data.removeAll(data);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.indexOf(',') == -1 || line.indexOf(',') != line.lastIndexOf(',')) {
				JOptionPane.showMessageDialog(this, "数据格式不正确");
				data.removeAll(data);
				sc.close();
				return false;
			}
			String[] nums = line.split(",");
			try {
				double[] point = { Double.parseDouble(nums[0]), Double.parseDouble(nums[1]) };
				data.add(point);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "数据格式不正确");
				data.removeAll(data);
				sc.close();
				return false;
			}
		}
		sc.close();
		return true;
	}

	// 检查是否有输入数据或选择文件
	private boolean checkDraw() {
		if (ui.rb_manualInput.isSelected() == true) {
			String str = ui.tf.getText();
			if (str == null || str.length() == 0) {
				JOptionPane.showMessageDialog(this, "请输入数据！");
				return false;
			}
			str = str.replace(' ', '\n');
			if (!readData(new Scanner(str))) return false;
		} else {
			if (file == null) {
				JOptionPane.showMessageDialog(this, "请选择文件！");
				return false;
			}
		}
		init();
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ui.rb_manualInput) {
			ui.tf.setEnabled(true);
			ui.bn_select.setEnabled(false);
		}
		if (e.getSource() == ui.rb_fileInput) {
			ui.tf.setEnabled(false);
			ui.bn_select.setEnabled(true);
		}
		if (e.getSource() == ui.bn_select) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			// 只允许选择txt或csv文件
			fc.addChoosableFileFilter(new FileFilter() {
				
				@Override
				public String getDescription() {
					return "*.txt,*.csv";
				}
				
				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) {
				        return true;
				    }
				    String ext = null;
			        String s = f.getName();
			        int i = s.lastIndexOf('.');
			        if (i > 0 && i < s.length() - 1) {
			            ext = s.substring(i+1).toLowerCase();
			        }
			        String extension = ext;
				    if (extension != null) {
				        if (extension.equals("txt") || extension.equals("csv")) {
				                return true;
				        } else {
				            return false;
				        }
				    }
				    return false;
				}
			});
			fc.setAcceptAllFileFilterUsed(false);
			fc.showOpenDialog(null);
			if (fc.getSelectedFile() == null) return;
			if (fc.getSelectedFile().exists()) {
				file = fc.getSelectedFile();
				ui.fileName.setText("已选择" + file.getName());
				try {
					if (!readData(new Scanner(file))) return;
					init();
				} catch (FileNotFoundException e1) {
					// test
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(this, "文件不存在！");
			}

		}

		if (e.getSource() == ui.bn_lagr) {
			if (!checkDraw()) return;
			drawMode = LAGRANGE;
		}
		if (e.getSource() == ui.bn_spline) {
			if (!checkDraw()) return;
			drawMode = SPLINE;
		}
		if (e.getSource() == ui.bn_fit) {
			if (!checkDraw()) return;
			String s = JOptionPane.showInputDialog(this,"请输入阶数：");
			if (s == null) return;
			try {
				fitTimes = Integer.parseInt(s);
				if (fitTimes < 0 || fitTimes >= data.size()) {
					fitTimes = 0;
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(this, "输入的阶数不正确");
				return;
			}
			drawMode = FITTING;
		}
		
		if (e.getSource() == ui.bn_exit) {
			System.exit(0);
		}

		repaint();
	}
	
	@Override
	// 鼠标拖动时移动坐标系
	public void mouseDragged(MouseEvent e) {
		Point pt = e.getPoint();
		dx += pt.x - mouse_x;
		dy += pt.y - mouse_y;
		mouse_x = pt.x;
		mouse_y = pt.y;
		repaint();
	}

	@Override
	// 保存鼠标当前位置
	public void mouseMoved(MouseEvent e) {
		Point pt = e.getPoint();
		mouse_x = pt.x;
		mouse_y = pt.y;
		repaint();
	}

	@Override
	// 鼠标滚轮滚动时缩放坐标系
	public void mouseWheelMoved(MouseWheelEvent e) {
		double x0 = getReal_X(mouse_x);
		double y0 = getReal_Y(mouse_y);
		if (e.getWheelRotation() > 0) {
			times *= 2;
		} else {
			times /= 2;
		}
		dx = mouse_x - x0 / times;
		dy = mouse_y + y0 / times;
		repaint();
	}
}

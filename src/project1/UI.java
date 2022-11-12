package project1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class UI extends JFrame {
	private static final long serialVersionUID = 1L;

	private MyPanel mp;
	private JPanel pl = new JPanel();
	public JRadioButton rb_manualInput, rb_fileInput;
	public ButtonGroup bg = new ButtonGroup();
	public JTextField tf = new JTextField(16);
	public JLabel fileName = new JLabel("未选择文件");
	public JButton bn_exit = new JButton("退出"), bn_select = new JButton("选择文件");
	public JButton bn_lagr = new JButton("拉格朗日内插"), bn_spline = new JButton("三次样条插值"),
			bn_fit = new JButton("多项式拟合");

	public UI() {
		// 绘制UI
		mp = new MyPanel(this);

		pl.setLayout(new GridLayout(20, 1));
		rb_manualInput = new JRadioButton("输入数据");
		rb_fileInput = new JRadioButton("选择文件");
		rb_fileInput.setSelected(true);
		tf.setEnabled(false);
		bn_select.setEnabled(true);
		bg.add(rb_manualInput);
		bg.add(rb_fileInput);
		rb_manualInput.addActionListener(mp);
		rb_fileInput.addActionListener(mp);

		pl.add(rb_manualInput);
		pl.add(tf);
		pl.add(rb_fileInput);
		addButton(bn_select);
		pl.add(fileName);
		pl.add(new JLabel());
		addButton(bn_lagr);
		addButton(bn_spline);
		addButton(bn_fit);
		pl.add(new JLabel());
		addButton(bn_exit);

		rb_manualInput.setBackground(Color.WHITE);
		rb_fileInput.setBackground(Color.WHITE);
		pl.setBackground(Color.WHITE);
		mp.setBackground(Color.WHITE);

		this.setLayout(new BorderLayout());
		this.add(mp, BorderLayout.CENTER);
		this.add(pl, BorderLayout.EAST);
		this.setTitle("Project1");
		this.setSize(797, 637);
		Dimension dn = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((dn.width - 797) / 2, (dn.height - 637) / 2);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void addButton(JButton bn) {
		pl.add(bn);
		bn.addActionListener(mp);
	}

	public static void main(String[] args) {
		new UI();
	}
}

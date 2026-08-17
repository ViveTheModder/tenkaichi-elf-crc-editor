package gui;
//Tenkaichi ELF CRC Editor by ViveTheJoestar
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import cmd.Main;

public class App {
	private static File currElfPath = null;
	private static File getElfPathFromChooser() {
		File elfPath = null;
		JFileChooser chooser = new JFileChooser();
		if (currElfPath != null) chooser.setCurrentDirectory(elfPath);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Choose ELF...");
		while (elfPath == null) {
			int result = chooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				elfPath = chooser.getSelectedFile();
			}
			else if (result == JFileChooser.CANCEL_OPTION) break;
		}
		return elfPath;
	}
	private static void errorBeep(Toolkit defToolkit) {
		Runnable runWinErrorSnd = (Runnable) defToolkit.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
	}
	@SuppressWarnings("unchecked")
	public static void setApp(String title) throws Exception {
		String ver = "v2.2";
		String[] labelText = {"Budokai Tenkaichi Version", "Game Region"};
		String[] btVers = {"BT1 / Sparking!", "BT2 / Sparking! NEO", "BT3 / Sparking! METEOR"};
		String[] regions = {"NTSC-J / Japan", "NTSC-U / USA", "PAL / EU"};
		Toolkit defToolkit = Toolkit.getDefaultToolkit();
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		//initialize components
		Font boldFont = new Font("Tahoma", Font.BOLD, 20);
		Font normalFont = new Font("Tahoma", Font.PLAIN, 15);
		Font titleFont = new Font("Tahoma", Font.BOLD, 30);
		GridBagConstraints gbc = new GridBagConstraints();
		JButton fixBtn = new JButton("Fix ELF");
		JComboBox<String>[] argComboBoxes = new JComboBox[2];
		JLabel[] argLabels = new JLabel[2];
		JLabel titleLabel = new JLabel(title);
		JMenu fileMenu = new JMenu("File"), helpMenu = new JMenu("Help");
		JMenuBar menuBar = new JMenuBar();
		JMenuItem about = new JMenuItem("About");
		JMenuItem choose = new JMenuItem("Choose ELF...");
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(new GridBagLayout());
		for (int i = 0; i < 2; i++) {
			if (i == 0) argComboBoxes[i] = new JComboBox<String>(btVers);
			else argComboBoxes[i] = new JComboBox<String>(regions);
			argComboBoxes[i].setFont(normalFont);
			((JLabel) argComboBoxes[i].getRenderer()).setHorizontalAlignment(JLabel.CENTER);
			argLabels[i] = new JLabel(labelText[i]);
			argLabels[i].setForeground(new Color(254, 112, 1));
			argLabels[i].setFont(boldFont);
		}
		//set component properties
		fixBtn.setBackground(new Color(254, 176, 1));
		fixBtn.setForeground(Color.WHITE);
		fixBtn.setFont(boldFont);
		//properly display background color on Windows 10 or higher
		fixBtn.setContentAreaFilled(false);
		fixBtn.setOpaque(true);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		titleLabel.setForeground(new Color(224, 4, 28));
		titleLabel.setFont(titleFont);
		//add action listeners
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Box box = Box.createHorizontalBox();
				JLabel label = new JLabel("Made by: ");
				JLabel author = new JLabel("ViveTheJoestar");
				label.setFont(normalFont);
				author.setFont(normalFont);
				author.setForeground(Color.BLUE);
				author.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							URI uri = new URI("https://github.com/ViveTheModder");
							Desktop.getDesktop().browse(uri);
						} 
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				box.add(label);
				box.add(author);
				JOptionPane.showMessageDialog(null, box, title + " " + ver, JOptionPane.INFORMATION_MESSAGE);
			}
		});
		choose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currElfPath = getElfPathFromChooser();
				if (currElfPath != null) {
					frame.setTitle(title + " " + ver + " - " + currElfPath.getName());
					int[] elfParams = Main.getElfParams(currElfPath);
					argComboBoxes[0].setSelectedIndex(elfParams[0]);
					argComboBoxes[1].setSelectedIndex(elfParams[1]);
				}
				else frame.setTitle(title + " " + ver);
			}
		});
		fixBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currElfPath == null) {
					errorBeep(defToolkit);
					JOptionPane.showMessageDialog(null, "No ELF provided!", title, JOptionPane.ERROR_MESSAGE);
				} 
				else setProgress(frame, title + " " + ver, argComboBoxes, defToolkit);
			}
		});
		//add components
		fileMenu.add(choose);
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		panel.add(titleLabel,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(argLabels[0],gbc);
		panel.add(argComboBoxes[0],gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(argLabels[1],gbc);
		panel.add(argComboBoxes[1],gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(fixBtn,gbc);
		frame.add(panel);
		//set frame properties
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setTitle(title + " " + ver);
		frame.setVisible(true);
	}
	private static void setProgress(JFrame frame, String title, 
	JComboBox<String>[] argComboBoxes, Toolkit defToolkit) {
		//change progress bar background color and text color
		UIManager.put("ProgressBar.background", Color.WHITE);
		UIManager.put("ProgressBar.foreground", Color.GREEN);
		UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
		UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
		//initialize components
		Dimension barSize = new Dimension(300, 30);
		Font boldFont = new Font("Tahoma", Font.BOLD, 20);
		Font normalFont = new Font("Tahoma", Font.PLAIN, 15);
		GridBagConstraints gbc = new GridBagConstraints();
		JDialog dialog = new JDialog();
		JLabel label = new JLabel("Calculating CRC...");
		JPanel panel = new JPanel(new GridBagLayout());
		JProgressBar bar = new JProgressBar();
		//set component properties
		bar.setFont(normalFont);
		bar.setMaximumSize(barSize);
		bar.setMinimumSize(barSize);
		bar.setPreferredSize(barSize);
		bar.setStringPainted(true);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		label.setFont(boldFont);
		//add components
		panel.add(label,gbc);
		panel.add(new JLabel(" "), gbc);
		panel.add(bar, gbc);
		dialog.add(panel);
		//add window listener
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				frame.setEnabled(false);
			}
			@Override
			public void windowClosed(WindowEvent e) {
				frame.setEnabled(true);
			}
		});
		//set dialog properties
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setSize(512, 256);
		dialog.setLocationRelativeTo(null);
		dialog.setTitle(title);
		dialog.setVisible(true);
		//set and execute background worker
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				double time = Main.applyElfFix(argComboBoxes[0].getSelectedIndex() + 1, 
				argComboBoxes[1].getSelectedIndex() + 1, currElfPath, false, bar);
				defToolkit.beep();
				String msg = time >= 0 ? "ELF has been fixed in " + time + " seconds!" : "The CRCs are equal. No fixes are required.";
				JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
				dialog.dispose();
				return null;
			}
		};
		worker.execute();
	}
}
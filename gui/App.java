package gui;
//Tenkaichi ELF CRC Editor by ViveTheJoestar
import java.awt.Color;
import java.awt.Cursor;
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.Main;

public class App {
	//solution taken from Stephen C's answer in stackoverflow (https://stackoverflow.com/a/1402762)
	private static boolean isInternetAvailable(URI uri) {
		try {
			URL url = uri.toURL();
			URLConnection urlc = url.openConnection();
			urlc.connect();
			urlc.getInputStream().close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	private static File getElf(File currDir, FileNameExtensionFilter[] filters, Toolkit tk) {
		File elf = null;
		JFileChooser chooser = new JFileChooser();
		if (currDir != null) chooser.setCurrentDirectory(currDir);
		for (FileNameExtensionFilter flt: filters) chooser.addChoosableFileFilter(flt);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Choose a Sparking/Tenkaichi ELF...");
		while (elf == null) {
			int result = chooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File tmp = chooser.getSelectedFile();
				for (FileNameExtensionFilter flt: filters) {
					if (flt.getDescription().contains(tmp.getName().toUpperCase())) {
						elf = tmp;
						break;
					}
				}
				if (elf == null) {
					errorBeep(tk);
					String msg = "The chosen ELF's file name does not match with those of recognized Sparking/Tenkaichi ELFs!";
					JOptionPane.showMessageDialog(null, msg, "Invalid ELF Name", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (result == JFileChooser.CANCEL_OPTION) break;
		}
		return elf;
	}
	private static FileNameExtensionFilter[] getElfFilters(String[] btVers, String[] regions) {
		FileNameExtensionFilter[] filters = new FileNameExtensionFilter[Main.ELF_NAMES.length];
		for (int fltCnt = 0; fltCnt < filters.length; fltCnt++) {
			String desc = "[" + regions[fltCnt % 3] + "] " + btVers[fltCnt / 3] + " (" + Main.ELF_NAMES[fltCnt] + ")";
			String ext = Main.ELF_NAMES[fltCnt].substring(9, 11);
			filters[fltCnt] = new FileNameExtensionFilter(desc, ext);
		}
		return filters;
	}
	private static void errorBeep(Toolkit defToolkit) {
		Runnable runWinErrorSnd = (Runnable) defToolkit.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd != null) runWinErrorSnd.run();
	}
	private static void errorDialog(Exception e, Toolkit tk) {
		String errMsg = e.getClass().getSimpleName() + ": " + e.getMessage() + "\n";
		StackTraceElement[] stackTraceArr = e.getStackTrace();
		for (StackTraceElement ste: stackTraceArr) {
			String line = ste.toString().replace("[", "").replace("]", "");
			if (line.startsWith("gui") || line.startsWith("cmd")) errMsg += line + "\n";
		}
		errorBeep(tk);
		JOptionPane.showMessageDialog(null, errMsg, "Exception", JOptionPane.ERROR_MESSAGE);
	}
	
	@SuppressWarnings("unchecked")
	public static void setApp(String title) throws Exception {
		File[] currDir = new File[1];
		String ver = "v3.0";
		String[] labelText = {"Budokai Tenkaichi Version", "Game Region"};
		String[] btVers = {"BT1 / Sparking!", "BT2 / Sparking! NEO", "BT3 / Sparking! METEOR"};
		String[] regions = {"NTSC-J / Japan", "NTSC-U / USA", "PAL / EU"};
		Toolkit defToolkit = Toolkit.getDefaultToolkit();
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		//initialize components
		Font boldFont = new Font("Tahoma", Font.BOLD, 20), normalFont = new Font("Tahoma", Font.PLAIN, 15);
		Font urlFont = new Font("Tahoma", Font.BOLD, 15), titleFont = new Font("Tahoma", Font.BOLD, 30);
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
		for (int i = 0; i < argLabels.length; i++) {
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
				author.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				author.setFont(urlFont);
				author.setForeground(Color.BLUE);
				author.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							URI uri = new URI("https://github.com/ViveTheModder");
							if (isInternetAvailable(uri)) Desktop.getDesktop().browse(uri);
						} 
						catch (Exception ex) {
							errorDialog(ex, defToolkit);
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
				currDir[0] = getElf(currDir[0], getElfFilters(btVers, regions), defToolkit);
				if (currDir[0] != null) {
					frame.setTitle(title + " " + ver + " - " + currDir[0].getName());
					int[] elfParams = Main.getElfParams(currDir[0]);
					argComboBoxes[0].setSelectedIndex(elfParams[0]);
					argComboBoxes[1].setSelectedIndex(elfParams[1]);
				}
				else frame.setTitle(title + " " + ver);
			}
		});
		fixBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currDir[0] == null) {
					errorBeep(defToolkit);
					JOptionPane.showMessageDialog(null, "No ELF provided!", title, JOptionPane.ERROR_MESSAGE);
				} 
				else {
					try {
						int btVer = argComboBoxes[0].getSelectedIndex() + 1, region = argComboBoxes[1].getSelectedIndex() + 1;
						double time = Main.applyElfFix(btVer, region, currDir[0], false);
						defToolkit.beep();
						String msg = time >= 0 ? "ELF has been fixed in " + time + " seconds!" : "The CRCs are equal. No fixes are required.";
						JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
					}
					catch (IOException ex) {
						errorDialog(ex, defToolkit);
					}
				}
			}
		});
		//add components
		fileMenu.add(choose);
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		panel.add(titleLabel, gbc);
		panel.add(new JLabel(" "), gbc);
		panel.add(new JLabel(" "), gbc);
		panel.add(argLabels[0], gbc);
		panel.add(argComboBoxes[0], gbc);
		panel.add(new JLabel(" "), gbc);
		panel.add(argLabels[1], gbc);
		panel.add(argComboBoxes[1], gbc);
		panel.add(new JLabel(" "), gbc);
		panel.add(fixBtn, gbc);
		frame.add(panel);
		//set frame properties
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.setMinimumSize(new Dimension(512, 512));
		frame.setLocationRelativeTo(null);
		frame.setTitle(title + " " + ver);
		frame.setVisible(true);
	}
}
package cmd;
//Tenkaichi ELF CRC Editor by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JProgressBar;
import gui.App;

public class Main {
	// CRCs are ordered from BT1 to BT3, and every set of 3 CRCs is ordered like so: 
	private static final int[] CRCs = {
		// NTSC-J  |   NTSC-U  |    PAL
		0xE7D5481C, 0x7B0E28D0, 0x92EA9EF0,
		0xE2F289ED, 0xFE961D28, 0x278722BF,
		0xF28D21F1, 0x428113C2, 0xA422BB13
	};
	private static final String[] ELF_NAMES = {
		//  NTSC-J    |   NTSC-U     |    PAL
		"SLPS_255.60", "SLUS_212.27", "SLES_532.00",
		"SLPS_256.90", "SLUS_214.41", "SLES_541.64",
		"SLPS_258.15", "SLUS_216.78", "SLES_549.45",
	};
	private static int getCrc(RandomAccessFile elf, JProgressBar bar) throws IOException {
		byte[] input = new byte[4];
		int crc = 0, fileSize = (int) elf.length();
		if (bar != null) bar.setMaximum(fileSize);
		for (int pos = fileSize; pos >= 0; pos -= 4) {
			elf.seek(pos);
			elf.read(input);
			crc ^= LittleEndian.getUnsignedIntFromByteArray(input);
			if (bar != null) bar.setValue(bar.getValue() + 4);
		}
		return crc;
	}
	private static int overwriteELF(RandomAccessFile elf, int newCRC, int oldCRC) throws IOException {
		int fileSize = (int) elf.length();
		elf.seek(fileSize - 4);
		int xor = newCRC ^ oldCRC;
		elf.writeInt(LittleEndian.getInt(xor));
		elf.close();
		return xor;
	}
	public static double applyElfFix(int btVer, int region, File elfPath, boolean print, 
	JProgressBar bar) throws IOException {
		long start = System.currentTimeMillis();
		RandomAccessFile elf = new RandomAccessFile(elfPath, "rw");
		int ogCRC = CRCs[3 * (btVer - 1) + (region - 1)];
		if (print) System.out.println("Calculating the ELF's CRC...");
		int currCRC = getCrc(elf, bar);
		if (currCRC == ogCRC) {
			System.out.println("The CRCs are equal, therefore no fixes are required.");
			return 0;
		}
		long end = System.currentTimeMillis();
		double time = (end - start) / 1000.0;
		double elfTime = overwriteELF(elf, currCRC, ogCRC);
		if (print) System.out.printf("\nModified CRC: %8x\nOriginal CRC: %8x", currCRC, ogCRC);
		if (print) System.out.printf("\n XOR Results: %8x\n", elfTime);
		if (print) System.out.println("Time elapsed: " + time + " s");
		return time;
	}
	public static int[] getElfParams(File elfPath) {
		int[] elfParams = {-1, -1};
		for (int i = 0; i < ELF_NAMES.length; i++) {
			if (elfPath.getName().toUpperCase().equals(ELF_NAMES[i])) {
				elfParams[0] = (i + 1) / 3;
				if (ELF_NAMES[i].startsWith("SLPS")) elfParams[1] = 2;
				else if (ELF_NAMES[i].startsWith("SLUS")) elfParams[1] = 1;
				else elfParams[1] = 0;
			}
		}
		return elfParams;
	}
	public static void main(String[] args) {
		try {
			int[] elfParams = new int[2]; //btVer, then region
			File elfPath = null;
			String helpText = "USAGE: java -jar bt-elf-crc-editor.jar "
			+ "[bt-ver] [region-id] \"path/to/ELF\"\n"
			+ "[bt-ver] -> Budokai Tenkaichi version (1, 2, 3)\n"
			+ "[region-id] -> Region ID (1 -> NTSC-J / Japan, 2 -> NTSC-U / USA, 3 -> PAL / EU)";
			String title = "Tenkaichi ELF CRC Editor";
			if (args.length>2) {
				elfParams[0] = Integer.parseInt(args[0]);
				elfParams[1] = Integer.parseInt(args[1]);
				elfPath = new File(args[2].replace("\"", ""));
				if (elfParams[0]<1 || elfParams[0]>3) {
					System.out.println("ERROR: Invalid Budokai Tenkaichi version!");
					System.exit(1);
				}
				if (elfParams[1]<1 || elfParams[1]>3) {
					System.out.println("ERROR: Invalid region ID!");
					System.exit(2);
				}
				if (!elfPath.isFile()) {
					System.out.println("ERROR: Invalid ELF file path!");
					System.exit(3);
				}
				applyElfFix(elfParams[0], elfParams[1], elfPath, true, null);
			}
			else if (args.length>0) {
				if (args[0].equals("-h")) System.out.println(helpText);
			}
			else App.setApp(title);
		} catch (Exception e) {e.printStackTrace();}
	}
}
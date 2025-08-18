package cmd;
//Tenkaichi ELF CRC Editor by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Main 
{
	//CRCs are ordered from BT1 to BT3, and every set of 3 CRCs is ordered like so: NTSC-J, NTSC-U, PAL
	static final int[] CRCs = {0xE7D5481C,0x7B0E28D0,0x92EA9EF0,0xE2F289ED,0xFE961D28,0x278722BF,0xF28D21F1,0x428113C2,0xA422BB13};
	private static int getCrc(RandomAccessFile elf) throws IOException
	{
		byte[] input = new byte[4];
		int crc=0, fileSize = (int)elf.length();
		for (int pos=fileSize; pos>=0; pos-=4)
		{
			elf.seek(pos);
			elf.read(input);
			crc^=LittleEndian.getUnsignedIntFromByteArray(input);
		}
		return crc;
	}
	private static int overwriteELF(RandomAccessFile elf, int newCRC, int oldCRC) throws IOException
	{
		int fileSize = (int) elf.length();
		elf.seek(fileSize-4);
		int xor = newCRC^oldCRC;
		elf.writeInt(LittleEndian.getInt(xor));
		elf.close();
		return xor;
	}
	public static void main(String[] args) 
	{
		try
		{
			Scanner sc = new Scanner(System.in);
			int btVer, currCRC, region;
			String input;
			File elfPath;
			while (true)
			{
				System.out.print("Enter the Budokai Tenkaichi version whose ELF you are modifying (from 1 to 3): ");
				input = sc.nextLine();
				if (input.matches("[1-3]+"))
				{
					btVer = Integer.parseInt(input); break;
				}
				else System.out.println("Invalid Budokai Tenkaichi version. Try again!\n");
			}
			while (true)
			{
				System.out.print("Enter the game's region (1 -> NTSC-J/Japan, 2 -> NTSC-U/USA, 3 -> PAL/EU): ");
				input = sc.nextLine();
				if (input.matches("[1-3]+"))
				{
					region = Integer.parseInt(input); break;
				}
				else System.out.println("Invalid Budokai Tenkaichi region. Try again!\n");
			}
			while (true)
			{
				System.out.print("Enter the exact path of the ELF: ");
				input = sc.nextLine();
				File tempPath = new File(input);
				if (tempPath.isFile())
				{
					elfPath = tempPath; break;
				}
				else System.out.println("Invalid ELF file path. Try again!\n");
			}
			sc.close();
			
			long start = System.currentTimeMillis();
			RandomAccessFile elf = new RandomAccessFile(elfPath,"rw");
			int ogCRC = CRCs[3*(btVer-1)+(region-1)];
			System.out.println("Calculating the ELF's CRC...");
			currCRC = getCrc(elf);

			if (currCRC==ogCRC)
			{
				System.out.println("The CRCs are equal, therefore no fixes are required.");
				System.exit(1);
			}
			System.out.print(String.format("\nModified CRC: %8x\nOriginal CRC: %8x", currCRC, ogCRC));
			System.out.println(String.format("\n XOR Results: %8x",overwriteELF(elf, currCRC, ogCRC)));
			long finish = System.currentTimeMillis();
			System.out.println("Time elapsed: "+(finish-start)/1000.0+" s");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
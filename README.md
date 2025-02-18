# tenkaichi-elf-crc-editor
A command-line tool that overwrites any Budokai Tenkaichi ISO's ELF to restore the CRC it once had.

For context, when making changes to the ELF (Executable and Linkable Format, containing the source code), the CRC (Cyclic Redundancy Check, checksum) generated by PCSX2 will change.

This is important, because it can mistake the game for being another (or just an unknown one), affecting the emulation settings on some computers.

Although [the emulator is open-source](https://github.com/PCSX2/pcsx2/releases), I am not aware of the algorithm it uses for the checksum.

Otherwise, I would have the program generate the CRC based on the ELF's contents.

# Usage/Demonstration
![image](https://github.com/user-attachments/assets/829a0f83-c0e8-4861-9630-32a6f671357e)

![image](https://github.com/user-attachments/assets/f449d4b3-a73e-42c6-874e-1b1b04130651)

# Results
![image](https://github.com/user-attachments/assets/412c7ebe-f592-4d6c-b451-44959860a35f)

![image](https://github.com/user-attachments/assets/584723b4-0e15-45db-9960-03457f5ec7fb)

![image](https://github.com/user-attachments/assets/64f06b75-ac71-4d14-8ab5-731261712b3b)

![image](https://github.com/user-attachments/assets/5510bebd-8ea8-4c25-9520-d6f22725aa92)

package be.monfils.dcputoolchainlauncher;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.LinkedList;

public class DCPUToolchainLauncher extends JFrame {

	private JComboBox<String> action = new JComboBox<>(new String[] {"Run a binary", "Assemble a program"});
	private JPanel run_panel = new JPanel(), assemble_panel = new JPanel();
	private JButton launch = new JButton("Launch");

	private CardLayout cardLayout = new CardLayout();
	private JPanel cardPanel = new JPanel();

	/*
	 * RUN
	 */
	private JButton run_browse = new JButton("Browse for binary...");
	private JList<String> run_hardware_list = new JList<>();
	private JButton run_addLEM = new JButton("Add LEM1802"), run_addClock = new JButton("Add generic clock"), run_addKeyboard = new JButton("Add generic keyboard"), run_addEDC = new JButton("Add EDC"), run_addM35FD = new JButton("Add M35FD"), run_addM525HD = new JButton("Add M525HD"), run_addSpeaker = new JButton("Add Speaker"),run_remove = new JButton("Remove selected item(s)");
	private JPanel run_addHardware_panel = new JPanel();
	private JCheckBox addBootLoader = new JCheckBox("Automatically add bootloader");

	private DefaultListModel<String> run_hardware_listModel = new DefaultListModel<>();

	private File run_file, run_file_directory = new File(System.getProperty("user.home"));

	private boolean setup_run_listModel = true;

	/*
	 * ASSEMBLE
	 */
	private JButton assemble_browse = new JButton("Browse for main file...");
	private JList<String> assemble_hardware_list = new JList<>();
	private JButton assemble_addLEM = new JButton("Add LEM1802"), assemble_addClock = new JButton("Add generic clock"), assemble_addKeyboard = new JButton("Add generic keyboard"), assemble_addEDC = new JButton("Add EDC"), assemble_addM35FD = new JButton("Add M35FD"), assemble_addM525HD = new JButton("Add M525HD"), assemble_remove = new JButton("Remove selected item(s)");
	private JPanel assemble_addHardware_panel = new JPanel();

	private DefaultListModel<String> assemble_hardware_listModel = new DefaultListModel<>();

	private File assemble_file, assemble_file_directory = new File(System.getProperty("user.home"));

	private boolean setup_assemble_listModel = true;



	private LinkedList<Process> launchedProcesses = new LinkedList<>();

	private Options options;

	public DCPUToolchainLauncher(Options options) {
		this.setTitle("DCPU-Toolchain Launcher by Azertyfun");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLocationByPlatform(true);

		this.options = options;

		if(options.lastBin != null) {
			File f = new File(options.lastBin);
			if(f.exists()) {
				run_file = f;
				run_browse.setText("Browse for binary... (" + run_file.getAbsolutePath() + ")");
			}

			if(f.getParentFile().exists()) {
				run_file_directory = f.getParentFile();
			}
		}

		if(options.lastAssembly != null) {
			File f = new File(options.lastAssembly);
			if(f.exists()) {
				assemble_file = f;
				assemble_browse.setText("Browse for main file... (" + assemble_file.getAbsolutePath() + ")");
			}

			if(f.getParentFile().exists()) {
				assemble_file_directory = f.getParentFile();
			}
		}

		if(options.run_listModel != null) {
			run_hardware_listModel = options.run_listModel;
			setup_run_listModel = false;
		}

		if(options.assembly_listModel != null) {
			assemble_hardware_listModel = options.assembly_listModel;
			setup_assemble_listModel = false;
		}

		addBootLoader.setSelected(options.addBootLoader);

		if(options.action < 2 && options.action >= 0)
			action.setSelectedIndex(options.action);
		else
			options.action = 0;

		setupLayout();
		setupListeners();
		updateAction();

		this.pack();
		this.setVisible(true);
	}

	public void setupLayout() {
		getContentPane().setLayout(new BorderLayout());
		cardPanel.setLayout(cardLayout);

		getContentPane().add(action, BorderLayout.NORTH);
		getContentPane().add(cardPanel, BorderLayout.CENTER);
		getContentPane().add(launch, BorderLayout.SOUTH);

		BorderLayout run_layout = new BorderLayout();
		run_panel.setLayout(run_layout);

		run_addHardware_panel.setLayout(new GridLayout(4, 2));
		run_addHardware_panel.add(run_addClock);
		run_addHardware_panel.add(run_addEDC);
		run_addHardware_panel.add(run_addKeyboard);
		run_addHardware_panel.add(run_addLEM);
		run_addHardware_panel.add(run_addM35FD);
		run_addHardware_panel.add(run_addM525HD);
		run_addHardware_panel.add(run_addSpeaker);
		run_addHardware_panel.add(run_remove);
		run_addHardware_panel.add(addBootLoader);
		run_panel.add(run_addHardware_panel, BorderLayout.EAST);

		run_panel.add(run_browse, BorderLayout.NORTH);

		run_hardware_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		run_hardware_list.setLayoutOrientation(JList.VERTICAL);
		run_hardware_list.setVisibleRowCount(-1);

		if(setup_run_listModel) {
			run_hardware_listModel.addElement("Generic clock");
			run_hardware_listModel.addElement("Generic keyboard");
			run_hardware_listModel.addElement("LEM1802");
			run_hardware_listModel.addElement("EDC");
		}
		run_hardware_list.setModel(run_hardware_listModel);
		run_panel.add(new JScrollPane(run_hardware_list), BorderLayout.WEST);



		BorderLayout assemble_layout = new BorderLayout();
		assemble_panel.setLayout(assemble_layout);

		assemble_addHardware_panel.setLayout(new GridLayout(3, 2));
		assemble_addHardware_panel.add(assemble_addClock);
		assemble_addHardware_panel.add(assemble_addEDC);
		assemble_addHardware_panel.add(assemble_addKeyboard);
		assemble_addHardware_panel.add(assemble_addLEM);
		assemble_addHardware_panel.add(assemble_addM35FD);
		assemble_addHardware_panel.add(assemble_addM525HD);
		assemble_addHardware_panel.add(assemble_remove);
		assemble_panel.add(assemble_addHardware_panel, BorderLayout.EAST);

		assemble_panel.add(assemble_browse, BorderLayout.NORTH);

		assemble_hardware_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		assemble_hardware_list.setLayoutOrientation(JList.VERTICAL);
		assemble_hardware_list.setVisibleRowCount(-1);

		if(setup_assemble_listModel) {
			assemble_hardware_listModel.addElement("Generic clock");
			assemble_hardware_listModel.addElement("Generic keyboard");
			assemble_hardware_listModel.addElement("LEM1802");
			assemble_hardware_listModel.addElement("EDC");
		}
		assemble_hardware_list.setModel(assemble_hardware_listModel);
		assemble_panel.add(new JScrollPane(assemble_hardware_list), BorderLayout.WEST);

		cardPanel.add(run_panel, "run");
		cardPanel.add(assemble_panel, "assemble");
	}

	public void setupListeners() {
		action.addItemListener((e) -> updateAction());

		run_remove.addActionListener(actionEvent -> {
			while(run_hardware_list.getSelectedIndices().length > 0) {
				run_hardware_listModel.removeElementAt(run_hardware_list.getSelectedIndices()[0]);
			}
		});
		run_addClock.addActionListener(actionEvent -> run_hardware_listModel.addElement("Generic clock"));
		run_addKeyboard.addActionListener(actionEvent -> run_hardware_listModel.addElement("Generic keyboard"));
		run_addEDC.addActionListener(actionEvent -> run_hardware_listModel.addElement("EDC"));
		run_addLEM.addActionListener(actionEvent -> run_hardware_listModel.addElement("LEM1802"));
		run_addSpeaker.addActionListener(actionEvent -> run_hardware_listModel.addElement("Speaker"));
		run_addM35FD.addActionListener(actionEvent -> {
			JFileChooser jfc = new JFileChooser();
			int returnVal = jfc.showOpenDialog(DCPUToolchainLauncher.this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
				run_hardware_listModel.addElement("M35FD=" + jfc.getSelectedFile().getAbsolutePath());
		});
		run_addM525HD.addActionListener(actionEvent -> {
			JFileChooser jfc = new JFileChooser();
			int returnVal = jfc.showOpenDialog(DCPUToolchainLauncher.this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
				run_hardware_listModel.addElement("M525HD=" + jfc.getSelectedFile().getAbsolutePath());
		});

		run_browse.addActionListener(actionEvent -> {
			JFileChooser jfc;
			if(run_file_directory != null && run_file_directory.exists())
				jfc = new JFileChooser(run_file_directory);
			else
				jfc = new JFileChooser();
			int returnVal = jfc.showOpenDialog(DCPUToolchainLauncher.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				run_file = jfc.getSelectedFile();
				run_browse.setText("Browse for binary... (" + run_file.getAbsolutePath() + ")");
				options.lastBin = run_file.getAbsolutePath();
			}
		});



		assemble_remove.addActionListener(actionEvent -> {
			while(assemble_hardware_list.getSelectedIndices().length > 0) {
				assemble_hardware_listModel.removeElementAt(assemble_hardware_list.getSelectedIndices()[0]);
			}
		});
		assemble_addClock.addActionListener(actionEvent -> assemble_hardware_listModel.addElement("Generic clock"));
		assemble_addKeyboard.addActionListener(actionEvent -> assemble_hardware_listModel.addElement("Generic keyboard"));
		assemble_addEDC.addActionListener(actionEvent -> assemble_hardware_listModel.addElement("EDC"));
		assemble_addLEM.addActionListener(actionEvent -> assemble_hardware_listModel.addElement("LEM1802"));
		assemble_addLEM.addActionListener(actionEvent -> assemble_hardware_listModel.addElement("Speaker"));
		assemble_addM35FD.addActionListener(actionEvent -> {
			JFileChooser jfc = new JFileChooser();
			int returnVal = jfc.showOpenDialog(DCPUToolchainLauncher.this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
				assemble_hardware_listModel.addElement("M35FD=" + jfc.getSelectedFile().getAbsolutePath());
		});
		assemble_addM525HD.addActionListener(actionEvent -> {
			JFileChooser jfc = new JFileChooser();
			int returnVal = jfc.showOpenDialog(DCPUToolchainLauncher.this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
				assemble_hardware_listModel.addElement("M525HD=" + jfc.getSelectedFile().getAbsolutePath());
		});

		assemble_browse.addActionListener(actionEvent -> {
			JFileChooser jfc;
			if(assemble_file_directory != null && assemble_file_directory.exists())
				jfc = new JFileChooser(assemble_file_directory);
			else
				jfc = new JFileChooser();
			int returnVal = jfc.showOpenDialog(DCPUToolchainLauncher.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				assemble_file = jfc.getSelectedFile();
				assemble_browse.setText("Browse for main file... (" + assemble_file.getAbsolutePath() + ")");
				options.lastAssembly = assemble_file.getAbsolutePath();
			}
		});



		launch.addActionListener(actionEvent -> {
			LinkedList<String> commandLine = new LinkedList<>();

			switch (action.getSelectedIndex()) {
				case 0: //RUN
					if(run_file == null || !run_file.exists())
						return;

					commandLine.add("run");
					commandLine.add(run_file.getAbsolutePath());
					commandLine.add("--debugger");
					for(int i = 0; i < run_hardware_listModel.getSize(); ++i) {
						String hw = run_hardware_listModel.getElementAt(i);
						if(hw.equals("Generic clock")) {
							commandLine.add("--clock");
						} else if(hw.equals("Generic keyboard")) {
							commandLine.add("--keyboard");
						} else if(hw.equals("LEM1802")) {
							commandLine.add("--lem1802");
						} else if(hw.equals("EDC")) {
							commandLine.add("--edc");
						} else if(hw.equals("Speaker")) {
							commandLine.add("--speaker");
						} else if(hw.substring(0, 5).equals("M35FD") || hw.substring(0, 5).equals("M525HD")) {
							commandLine.add("--" + hw);
						}
					}

					if(addBootLoader.isSelected()) {
						try {
							File tmpFile = File.createTempFile("DCPUToolchainLauncher", Long.toString(System.currentTimeMillis()));

							byte[] header = Files.readAllBytes(Paths.get("bin/res/bold_header.bin"));
							int length = (Files.readAllBytes(Paths.get(run_file.getAbsolutePath()))).length;
							header[0x1FE * 2] = (byte) ((length / 1024 + 1) & 0xFF);

							FileOutputStream fos = new FileOutputStream(tmpFile);
							fos.write(header);
							fos.close();

							commandLine.add("--bootloader=" + tmpFile.getAbsolutePath());
						} catch (NoSuchFileException e) {
							System.err.println("Error: File not found: " + e.getFile() + ".\n");
							JOptionPane.showMessageDialog(null, "Could not find file " + e.getFile(), "Error", JOptionPane.ERROR_MESSAGE);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, "IOException: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					}

					break;
				case 1: //ASSEMBLE
					if(assemble_file == null || !assemble_file.exists())
						return;

					commandLine.add("run");
					commandLine.add(assemble_file.getAbsolutePath());
					commandLine.add("--assemble");
					commandLine.add("--debugger");
					for(int i = 0; i < assemble_hardware_listModel.getSize(); ++i) {
						String hw = assemble_hardware_listModel.getElementAt(i);
						if(hw.equals("Generic clock")) {
							commandLine.add("--clock");
						} else if(hw.equals("Generic keyboard")) {
							commandLine.add("--keyboard");
						} else if(hw.equals("LEM1802")) {
							commandLine.add("--lem1802");
						} else if(hw.equals("EDC")) {
							commandLine.add("--edc");
						} else if(hw.equals("Speaker")) {
							commandLine.add("--speaker");
						} else if(hw.substring(0, 5).equals("M35FD") || hw.substring(0, 5).equals("M525HD")) {
							commandLine.add("--" + hw);
						}
					}
					break;
			}

			launch(commandLine);
		});

		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent windowEvent) {

			}

			@Override
			public void windowClosing(WindowEvent windowEvent) {
				for(Process p : launchedProcesses) {
					if(!p.isAlive())
						continue;

					try {
						BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
						bf.write("stop\n");
						bf.flush();
						int i = 0;
						while(p.isAlive() && i++ < 20) {
							Thread.sleep(100);
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					p.destroy();
				}

				options.run_listModel = run_hardware_listModel;
				options.assembly_listModel = assemble_hardware_listModel;
				options.addBootLoader = addBootLoader.isSelected();

				try {
					File file_options = new File("options.json");
					file_options.createNewFile();

					Gson gson = new Gson();
					Files.write(Paths.get("options.json"), gson.toJson(options, Options.class).getBytes());
				} catch(IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void windowClosed(WindowEvent windowEvent) {

			}

			@Override
			public void windowIconified(WindowEvent windowEvent) {

			}

			@Override
			public void windowDeiconified(WindowEvent windowEvent) {

			}

			@Override
			public void windowActivated(WindowEvent windowEvent) {

			}

			@Override
			public void windowDeactivated(WindowEvent windowEvent) {

			}
		});
	}

	public void updateAction() {
		switch (action.getSelectedIndex()) {
			case 0:
				cardLayout.show(cardPanel, "run");
				options.action = 0;
				break;
			case 1:
				cardLayout.show(cardPanel, "assemble");
				options.action = 1;
				break;
		}
	}

	public void launch(LinkedList<String> commandLine) {
		String[] commandLine_array = new String[commandLine.size() + 3];
		commandLine_array[0] = System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java";
		commandLine_array[1] = "-jar";
		commandLine_array[2] = "DCPU-Toolchain.jar";
		for(int i = 0; i < commandLine.size(); ++i)
			commandLine_array[i + 3] = commandLine.get(i);

		System.out.print("Command line: ");
		for(String s : commandLine_array)
			System.out.print(s + " ");
		System.out.println();

		ProcessBuilder pb = new ProcessBuilder(commandLine_array);
		pb.directory(new File("bin"));
		pb.redirectErrorStream(true);
		try {
			launchedProcesses.add(pb.start());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public static void main(String[] args) {
		try {
			File file_options = new File("options.json");
			file_options.createNewFile();

			String fileContent = new String(Files.readAllBytes(Paths.get("options.json")), StandardCharsets.UTF_8);

			Gson gson = new Gson();
			Options options = gson.fromJson(fileContent, Options.class);
			if(options == null)
				options = new Options();

			DCPUToolchainLauncher dcpuToolchainLauncher = new DCPUToolchainLauncher(options);
		} catch (IOException e) {
			e.printStackTrace();
			DCPUToolchainLauncher dcpuToolchainLauncher = new DCPUToolchainLauncher(new Options());
		}
	}
}

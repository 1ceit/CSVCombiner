import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.DefaultTableModel;

public class Combiner extends JFrame {
    private JTextField file1Field, file2Field;
    private JButton browseButton1, browseButton2, combineButton, exportButton;
    private JTable previewTable;

    public Combiner() {
        setTitle("CSV File Combiner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 1));

        file1Field = new JTextField(20);
        file2Field = new JTextField(20);

        browseButton1 = new JButton("Browse File 1");
        browseButton2 = new JButton("Browse File 2");
        combineButton = new JButton("Combine Files");
        exportButton = new JButton("Export As");

        browseButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFile(file1Field);
            }
        });

        browseButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFile(file2Field);
            }
        });

        combineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                combineFiles();
            }
        });

        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportPreview();
            }
        });

        JPanel file1Panel = new JPanel();
        file1Panel.add(new JLabel("File 1:"));
        file1Panel.setForeground(Color.white); 
        file1Panel.add(file1Field);
        file1Panel.add(browseButton1);

        JPanel file2Panel = new JPanel();
        file2Panel.add(new JLabel("File 2:"));
        file2Panel.add(file2Field);
        file2Panel.add(browseButton2);

        JPanel combinePanel = new JPanel();
        combinePanel.add(combineButton);
        combinePanel.add(exportButton);
        exportButton.setVisible(false);

        panel.add(file1Panel);
        panel.add(file2Panel);
        panel.add(combinePanel);

        add(panel, BorderLayout.NORTH);

        previewTable = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(previewTable);
        add(tableScrollPane, BorderLayout.CENTER);
        previewTable.setGridColor(Color.BLACK);

        
    }

    private void chooseFile(JTextField textField) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Downloads"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void combineFiles() {
        String file1Path = file1Field.getText();
        String file2Path = file2Field.getText();
    
        if (!file1Path.endsWith(".csv") || !file2Path.endsWith(".csv")) {
            JOptionPane.showMessageDialog(this, "Please select CSV files only.","Error",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        try (BufferedReader reader1 = new BufferedReader(new FileReader(file1Path));
                BufferedReader reader2 = new BufferedReader(new FileReader(file2Path))) {
    
            // Read headers from file 1
            String[] headers = reader1.readLine().split(",");
            DefaultTableModel tableModel = new DefaultTableModel(headers, 0);
    
            // Read data from file 1
            String line;
            while ((line = reader1.readLine()) != null) {
                String[] rowData = line.split(",");
                if (!containsIgnoredTitles(rowData)) {
                    tableModel.addRow(rowData);
                }
            }
    
            // Read data from file 2
            while ((line = reader2.readLine()) != null) {
                String[] rowData = line.split(",");
                if (!containsIgnoredTitles(rowData)) {
                    tableModel.addRow(rowData);
                }
            }
    
            previewTable.setModel(tableModel);
            exportButton.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error combining files: " + e.getMessage());
        }
    }
    
    // Method to check if any of the ignored titles exist in a row
    private boolean containsIgnoredTitles(String[] rowData) {
        String[] ignoredTitles = {"Team", "Match", "Drive Type", "Climbing Method", "Auto Start Position", "Auto Notes Shot", "Auto Notes Made", "Auto Notes In Amp", "Teleop Notes Shot", "Teleop Notes Made", "Teleop Notes In Amp", "Notes", "Height", "Chassis Size", "Teleop Accuracy", "Auto Accuracy"};
        for (String title : ignoredTitles) {
            for (String data : rowData) {
                if (title.equals(data)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void exportPreview() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save As");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")+ "/Downloads"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy");
        String defaultFileName = "VictiScoutCombined-" + dateFormat.format(new Date()) + ".csv";
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            if (!filePath.endsWith(".csv")) {
                filePath += ".csv"; // Ensure the file has a .csv extension
            }

            try (PrintWriter writer = new PrintWriter(filePath)) {
                int rows = previewTable.getRowCount();
                int cols = previewTable.getColumnCount();

                // Write headers
                for (int i = 0; i < cols; i++) {
                    writer.print(previewTable.getColumnName(i));
                    if (i < cols - 1)
                        writer.print(",");
                }
                writer.println();

                // Write data
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        writer.print(previewTable.getValueAt(i, j));
                        if (j < cols - 1)
                            writer.print(",");
                    }
                    writer.println();
                }
                
                JOptionPane.showMessageDialog(this, "Exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting: " + e.getMessage(),"Error",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Combiner().setVisible(true);
            }
        });
    }
}

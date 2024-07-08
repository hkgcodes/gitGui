package com.gitReportGenerator.main;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.opencsv.exceptions.CsvException;

@SpringBootApplication
public class MainApplication implements CommandLineRunner {
    @Autowired
    private CsvReaderUtil csvReaderUtil;

    @Autowired
    private GitLabClient gitLabClient;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GitLab Commit Analyzer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            JLabel label = new JLabel("Select time range:");
            String[] options = { "Last 7 days", "Last 30 days" };
            JComboBox<String> comboBox = new JComboBox<>(options);
            JButton button = new JButton("Generate");

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);

            // Progress bar initialization
            JProgressBar progressBar = new JProgressBar();
            progressBar.setStringPainted(true); // Show progress percentage

            // Button for copying message to clipboard
            JButton copyButton = new JButton("Copy Message");
            copyButton.setVisible(false); // Initially hidden until message is displayed
            copyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    StringSelection stringSelection = new StringSelection(textArea.getText());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            });

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedOption = (String) comboBox.getSelectedItem();
                    int days = selectedOption.equals("Last 30 days") ? 30 : 7;
                    new Thread(() -> {
                        try {
                            // Read group IDs and names from CSV
                            Map<String, String> groupIdToNameMap = csvReaderUtil.readCsv("C:/Users/harie/dataload.csv");

                            // Set progress bar maximum
                            progressBar.setMaximum(groupIdToNameMap.size());
                            progressBar.setValue(0);

                            // Map to store commit counts per group and committer
                            Map<String, Map<String, Integer>> groupCommitCounts = new HashMap<>();

                            // Process each group ID and name
                            int progress = 0;
                            for (Map.Entry<String, String> entry : groupIdToNameMap.entrySet()) {
                                String groupId = entry.getKey();
                                String groupName = entry.getValue();

                                // Fetch projects for the current group ID
                                List<Map<String, Object>> projects = gitLabClient.getProjects(groupId);

                                // Map to store commit counts by committer for current group
                                Map<String, Integer> commitCounts = new HashMap<>();

                                for (Map<String, Object> project : projects) {
                                    String projectId = String.valueOf(project.get("id"));

                                    // Fetch commits for the current project ID
                                    List<Map<String, Object>> commits = gitLabClient.getCommits(projectId);

                                    // Filter commits based on selected time range and group by committer
                                    for (Map<String, Object> commit : commits) {
                                        try {
                                            LocalDateTime commitDateTime = parseCommitDateTime((String) commit.get("committed_date"));

                                            // Check if commit date is within the specified date range
                                            if (commitDateTime != null && isWithinDateRange(commitDateTime, days)) {
                                                String authorName = (String) commit.get("author_name");
                                                commitCounts.put(authorName, commitCounts.getOrDefault(authorName, 0) + 1);
                                            }
                                        } catch (Exception ex) {
                                            System.err.println("Error parsing commit date: " + ex.getMessage());
                                        }
                                    }
                                }

                                // Store commit counts for current group
                                groupCommitCounts.put(groupId, commitCounts);

                                // Update progress bar
                                progress++;
                                final int currentProgress = progress;
                                SwingUtilities.invokeLater(() -> progressBar.setValue(currentProgress));
                            }

                            // Prepare result message with customized header
                            StringBuilder message = new StringBuilder();
                            String dateRangeText = days == 30 ? "Last 30 days" : "Last 7 days";
                            message.append("Hi Team, this is the commit statistics for the period ")
                                    .append(LocalDate.now().minusDays(days).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                    .append(" - ")
                                    .append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                    .append(" based on your selection (")
                                    .append(dateRangeText)
                                    .append("):\n\n");

                            // Append commit statistics for each group
                            for (Map.Entry<String, String> entry : groupIdToNameMap.entrySet()) {
                                String groupId = entry.getKey();
                                String groupName = entry.getValue();

                                message.append("Group: ").append(groupName).append("\n");

                                // Get commit counts for current group
                                Map<String, Integer> commitCounts = groupCommitCounts.get(groupId);
                                if (commitCounts != null) {
                                    for (Map.Entry<String, Integer> commitEntry : commitCounts.entrySet()) {
                                        String authorName = commitEntry.getKey();
                                        int count = commitEntry.getValue();
                                        message.append("  ").append(authorName).append(": ").append(count).append(" commits\n");
                                    }
                                } else {
                                    message.append("  No commits found.\n");
                                }

                                message.append("\n");
                            }

                            // Update UI with result message
                            textArea.setText(message.toString());

                            // Enable "Copy Message" button
                            copyButton.setVisible(true);
                        } catch (IOException | CsvException ex) {
                            ex.printStackTrace();
                            textArea.setText("Failed to process data: " + ex.getMessage());
                        }
                    }).start();
                }
            });

            JPanel panel = new JPanel();
            panel.add(label);
            panel.add(comboBox);
            panel.add(button);
            panel.add(copyButton); // Add copyButton to panel
            panel.add(progressBar); // Add progressBar to panel

            frame.getContentPane().add(panel, "North");
            frame.getContentPane().add(new JScrollPane(textArea), "Center");

            frame.setVisible(true);
        });
    }

    // Method to parse commit date-time from GitLab response
    private LocalDateTime parseCommitDateTime(String commitDateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return LocalDateTime.parse(commitDateString, formatter);
        } catch (Exception e) {
            System.err.println("Error parsing commit date: " + e.getMessage());
            return null;
        }
    }

    // Method to check if a commit date is within the last N days
    private boolean isWithinDateRange(LocalDateTime commitDateTime, int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        LocalDate toDate = LocalDate.now();
        LocalDate commitDate = commitDateTime.toLocalDate();
        return !commitDate.isBefore(fromDate) && !commitDate.isAfter(toDate);
    }
}

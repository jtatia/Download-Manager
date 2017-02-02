/**
 * Created by jaita on 21-Jan-17.
 */
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

//THe DownLoad Manager
public class DownloadManager extends JFrame implements Observer {
    //Add download txt field
    private JTextField addTextField;

    //Download table's data model
    private DownloadsTableModel tableModel;

    //Table listing downloads
    private JTable table;

    //Buttons for managing the selected download
    private JButton pauseButton, resumeButton;
    private JButton cancelButton, clearButton;

    //Currently selected download
    private Download selectedDownload;

    //Flag for whether or not table selection is being cleared
    private boolean clearing;

    //Constructor for Download Manager
    public DownloadManager()
    {
        //set application title
        setTitle("Download Manager");

        //set window size
        setSize(640, 480);

        //Handle window closing events
        addWindowListener(new WindowAdapter() {
            public void windowClosiing(WindowEvent e) {
                actionExit();
            }
        });

        //Set up file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });
        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        //set up add panel
        JPanel addPanel = new JPanel();
        addTextField = new JTextField(30);
        addPanel.add(addTextField);
        JButton addButton = new JButton("Add Download");
        addButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              actionAdd();
            }
        });
     addPanel.add(addButton);
    //Set up Download table
        tableModel = new DownloadsTableModel();
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                tableSelectionChanged();
            }
        });

        //Allow only one row at a time to be selected
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Set up progressBar as renderer for progress column
        ProgressRenderer renderer = new ProgressRenderer(0,100);
        renderer.setStringPainted(true); //show progress text
        table.setDefaultRenderer(JProgressBar.class,renderer);

        //Set table's row height large enough to fit JProgressBar
        table.setRowHeight(
                (int) renderer.getPreferredSize().getHeight());

        //set up DownloadPanel
        JPanel downloadPanel = new JPanel();
        downloadPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
        downloadPanel.setLayout(new BorderLayout());
        downloadPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        //Set up buttons panel
        JPanel buttonsPanel = new JPanel();
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionPause();
            }
        });
        pauseButton.setEnabled(false);
        buttonsPanel.add(pauseButton);
        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionResume();
            }
        });
        resumeButton.setEnabled(false);
        buttonsPanel.add(resumeButton);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionCancel();
            }
        });
        cancelButton.setEnabled(false);
        buttonsPanel.add(cancelButton);
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionClear();
            }
        });
        clearButton.setEnabled(false);
        buttonsPanel.add(clearButton);

        //Add Panels to display
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(addPanel,BorderLayout.NORTH);
        getContentPane().add(downloadPanel,BorderLayout.CENTER);
        getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
    }

    private void actionExit() {
        System.exit(0);
    }

    //Add a new Download
    private void actionAdd() {
        URL verifiedUrl = verifyUrl(addTextField.getText());
        if (verifiedUrl != null) {
            tableModel.addDownload(new Download(verifiedUrl));
            addTextField.setText(""); //reset add text field
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Verify download URL
    private URL verifyUrl(String url) {
        //Only allow HTTP URLs
        if (!url.toLowerCase().startsWith("http://"))
              return null;

        //Verify format of URL
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        }catch (Exception e) {
            return null;
        }

        //Make sure URL specifies a file
        if (verifiedUrl.getFile().length() < 2)
            return null;

        return verifiedUrl;
    }

    //Called when table row selection changes
    private void tableSelectionChanged() {
        /*Unregister from receiving notifications from the last selected download */
        if (selectedDownload != null)
            selectedDownload.deleteObserver(DownloadManager.this);

        /*If not in the middle of clearing a download,
        set the selected download and register to receive notifications
         from it*/
        if(!clearing && table.getSelectedRow() > -1) {
            selectedDownload = tableModel.getDownload(table.getSelectedRow());
            selectedDownload.addObserver(DownloadManager.this);
            updateButtons();
        }
    }

    //Pause the selected download
    private void actionPause() {
        selectedDownload.pause();
        updateButtons();
    }

    //resume the download
    private void actionResume() {
        selectedDownload.resume();
        updateButtons();
    }

    //cancel the selected download
    private void actionCancel() {
        selectedDownload.cancel();
        updateButtons();
    }

    //clear the selected buttons
    private void actionClear() {
        clearing = true;
        tableModel.clearDownload(table.getSelectedRow());
        clearing = false;
        selectedDownload = null;
        updateButtons();
    }

    /*Update each button's state based off of the
    currently selected downloads status
     */
    private void updateButtons() {
        if(selectedDownload != null) {
            int status = selectedDownload.getStatus();
            switch (status) {
                case Download.DOWNLOADING:
                    pauseButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.PAUSED:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.ERROR:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
                    break;
                default: //COMPLETE OR CANCELLED
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
            }
        }else {
            //No download is selected in table
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
            cancelButton.setEnabled(false);
            clearButton.setEnabled(false);
            }
        }

        //Update is called when a Download notifies its observer of any changes
    public void update(Observable o, Object arg) {
        //Update buttons if the selected download has changed
        if (selectedDownload != null && selectedDownload.equals(o)) {
            updateButtons();
        }
    }

    //Run the Download Manager

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DownloadManager manager = new DownloadManager();
                manager.setVisible(true);
            }
        });
    }
}
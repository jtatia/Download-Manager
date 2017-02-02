/**
 * Created by jaita on 19-Jan-17.
 */
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

//This class manages the download table's data
public class DownloadsTableModel extends AbstractTableModel implements Observer {

    //these are the names for the table's columns
    private static final String[] columnNames = {"URL","Size","Progress","Status"};

    //These are the classes for each columns values
    private static final Class[] columnClasses = {String.class,String.class,JProgressBar.class,String.class};

    //The table's list of downloads
    private ArrayList<Download> downloadList = new ArrayList<Download>();

    //Add a new download to the table
    public void addDownload(Download download) {
        //Register to be notified when the download changes.
        download.addObserver(this);
        downloadList.add(download);

     //Fire table row insertion notification to table
    fireTableRowsInserted(getRowCount() - 1, getRowCount()-1);
    }

    //get a download for the specified row
public Download getDownload(int row) {
return downloadList.get(row);
}

//remove a download from the list
public void clearDownload(int row) {
    downloadList.remove(row);

    //fire table notification to table
    fireTableRowsDeleted(row,row);
}

//get table's column count
public int getColumnCount() {
    return columnNames.length;
}

//get a column's name
    public String getColumnName(int col) {
        return columnNames[col];
    }

    //get a column's class
    public Class<?> getColumnClass(int col) {
        return columnClasses[col];
    }

    //get a table's row count

    public int getRowCount() {
        return downloadList.size();
    }

    //get value specific row and column combination

    public Object getValueAt(int row,int col) {
        Download download = downloadList.get(row);
        switch (col) {
            case 0: //URL
                return download.getURL();
            case 1://Siz
                int size = download.getSize();
                return (size == -1)? "": Integer.toString(size);
            case 3://Status
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }

    /*Update is called when a Download notifies its observers of any changes */

    public void update(Observable o, Object arg) {
        int index = downloadList.indexOf(o);

        //FIre table row update notification to table
        fireTableRowsUpdated(index,index);
    }
}



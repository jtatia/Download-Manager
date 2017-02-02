/**
 * Created by jaita on 19-Jan-17.
 */
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.html.ObjectView;

//This class renders aJProgressBar in a table cell.
public class ProgressRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressRenderer(int min, int max) {
        super(min,max);
    }
    //returns this JProgressBar as the renderer for the given table cell

    public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected,boolean hasFocus,int row,int column) {

        setValue((int) ((Float)value).floatValue());
        return  this;
    }
}

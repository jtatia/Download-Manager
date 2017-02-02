/**
 * Created by jaita on 16-Jan-17.
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadFactory;

//This class downloads a file from a URL
public class Download extends Observable implements Runnable {
    //Max size of download buffer
    private static final int MAX_BUFFER_SIZE = 1024;
    //these are the status name
    public static final String STATUSES[] = {"Downloading", "Paused", "Complete", "Cancelled", "Error"};

    //status codes
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    private URL url;
    private int size;
    private int downloaded;
    private int status;

    //Constructor for download
    public Download(URL url) {
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;

        //begin download
        download();
    }

    //get Download url
    public String getURL() {
        return url.toString();
    }

    //get this downloads size
    public int getSize() {
        return size;
    }

    //Get this downloads progress
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

    //get download status
    public int getStatus() {
        return status;
    }

    //pause this download
    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    //resume download
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        download();
    }

    //cancel this download
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    //Mark this download as having error
    private void error() {
        status = ERROR;
        stateChanged();
    }

    //start or resume download
    public void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    //Get file name portion of URL
    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    //Download file

    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;

        try {
            //open connection to url
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //specify what portion of file to downlaod
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

            //connect to server
            connection.connect();

            //make sure response code is in the 200 range
            if (connection.getResponseCode() / 100 != 2) {
                error();
            }

            //check for valid contend length
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
            }

            //set download size if not yet set
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }

            //open file and seek to the end of it

            file = new RandomAccessFile(getFileName(url), "rw");
            file.seek(downloaded);

            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
                //size buffer according to how much of the file is left to download
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                //Read from server into buffer
                int read = stream.read(buffer);
                if(read == -1)
                    break;

                //write buffer to file
                file.write(buffer,0,read);
                downloaded += read;
                stateChanged();
            }
            //change status to complete if this point was reached before downloading has finished
            if(status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
        }catch (Exception e)
        {
            error();
        } finally {
          //close file
            if(file !=null) {
                try {
                    file.close();
                }catch (Exception e) {}
            }

            //close connection to server
            if(stream !=null) {
                try {
                    stream.close();
                }catch (Exception e){}
            }
        }
    }
    //notify observers that this downloads status has changed

    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
}
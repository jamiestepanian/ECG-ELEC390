package com.elec390.teamb.ecg;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used to graph .ecg files
 */
public class DoctorViewerActivity extends AppCompatActivity {
    private int data_size = 0;
    private File ecg_datafile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_viewer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Check if access to external storage is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission to access external storage
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);}
        GraphView graph = findViewById(R.id.ecgDataGraph);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);
        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(250);
        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time (s)");
        gridLabel.setHumanRounding(true);
        gridLabel.setLabelsSpace(-2);
        gridLabel.setVerticalLabelsVisible(false);
        //gridLabel.setVerticalAxisTitle("Voltage (mV)");

        if(getIntent().getData() != null){
            String scheme = getIntent().getData().getScheme();
            // Open from File browser
            if(scheme.equals("file")) {
            try {
                ecg_datafile = new File(getIntent().getData().getPath());
                FileReader ecgFile = new FileReader(ecg_datafile);
                BufferedReader bufferedFile = new BufferedReader(ecgFile);
                while (bufferedFile.readLine() != null)data_size++;
                bufferedFile.close();
            } catch (Exception e) {e.printStackTrace();}
            }
            // Open from Gmail
            if (scheme.equals("content")) {
                try {
                    // Create temp file
                    File ecgdataroot = new File(Environment.getExternalStorageDirectory(), "ECGData");
                    // Create storage folder if it doesn't exist
                    if (!ecgdataroot.exists()) ecgdataroot.mkdirs();
                    // Create file
                    ecg_datafile = new File(ecgdataroot,"temp.ecg");
                    InputStream inputStream = getContentResolver().openInputStream(getIntent().getData());
                    //FileUtils.copyInputStreamToFile(inputStream, ecg_datafile);
                    OutputStream outStream = new FileOutputStream(ecg_datafile);
                    byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                    FileReader ecgFile = new FileReader(ecg_datafile);
                    BufferedReader bufferedFile = new BufferedReader(ecgFile);
                    while (bufferedFile.readLine() != null)data_size++;
                    bufferedFile.close();
                } catch (Exception e) {e.printStackTrace();}
            }
            if(ecg_datafile != null) {
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(getData());
                graph.addSeries(series);
            }
        }
    }

    /**
     * Returns an array of DataPoint objects from the .ecg file
     */
    private DataPoint[] getData() {
        DataPoint[] values = new DataPoint[data_size-1];
        try {
            FileReader ecgFile = new FileReader(ecg_datafile);
            BufferedReader bufferedFile = new BufferedReader(ecgFile);
            bufferedFile.readLine();
            for(int i=0 ; i<data_size-1 ; i++) {
                String sTemp = bufferedFile.readLine();
                String[] splitLine = sTemp.split(",");
                double x = Double.parseDouble(splitLine[0]);
                short y = Short.parseShort(splitLine[1]);
                DataPoint v = new DataPoint(x, y/20);
                values[i] = v;
            }
            bufferedFile.close();
        } catch (Exception e) {e.printStackTrace();}
        return values;
    }
}
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;

import javafx.scene.layout.Background;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;

/**
 * With this tool, states and data can be displayed "live" and compared with previous series.
 * 
 * Supplementary material to the book: 
 * "Reinforcement Learning From Scratch: Understanding Current Approaches - with Examples in Java and Greenfoot" by Uwe Lorenz.
 * https://link.springer.com/book/10.1007/978-3-031-09030-1
 * 
 * Ausgabe auf Deutsch: https://link.springer.com/book/9783662683101
 * 
 * Licensing CC-BY-SA 4.0 
 * Attribution - Sharing under the same conditions
 * 
 * www.facebook.com/ReinforcementLearningJava
 * github.com/sn-code-inside/Reinforcement-Learning
 *
 * www.x-ai.eu
 * 
 * @author Uwe Lorenz
 * @version 1.2 (14.11.2023)
 */
public class JfxMonitor {
    private static final String styleSheet = "resources/logchart.css";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss,SSS");
    private static final NumberFormat nf = NumberFormat.getInstance(new java.util.Locale("en", "US"));
  
    private LineChart<Number,Number> s_lineChart = null;
    private LineChart<Number,Number> a_lineChart = null;
    private LineChart<Number,Number> learningLineChart = null;
    
    private int displayTimeIntervalLength  = 500;
    private int displayStepWith = 1;

    private TextArea taState1 = new TextArea();
    private TextArea taState2 = new TextArea();
    private TextArea taLearningCSV = new TextArea();
    private TextArea taInfo = new TextArea();
    
    private String[] sensornames = null;
    private String[] motornames = null;
    
    public JfxMonitor(String agent, String[] sensornames, String[] motornames) {
        this.sensornames = sensornames;
        this.motornames  = motornames;
        nf.setMaximumFractionDigits(3);
        Platform.runLater(()->{
            Stage stage = new Stage();
            stage.setTitle("agents data");
            
            Tab tabState = new Tab("current episode states/actions");
            Tab tabLearning = new Tab("learning");
            Tab tabInfo = new Tab("general informations");
           
            /*produce tab for displaying an agent's state */
            GridPane gpTabState = new GridPane();
            s_lineChart = createLineChart("sensor values","from t-"+this.displayTimeIntervalLength+" until t","sensors",sensornames);
            a_lineChart = createLineChart("motor values","from t-"+this.displayTimeIntervalLength+" until t","motors",motornames);
            gpTabState.setStyle("-fx-background-color: lightgray");
            gpTabState.add(taState1,0,0);
            gpTabState.add(taState2,1,0);
            gpTabState.add(s_lineChart,0,1);
            gpTabState.add(a_lineChart,1,1);
            gpTabState.setPadding(new Insets (4, 4, 4, 4));
            gpTabState.setGridLinesVisible(false);
            ColumnConstraints cc1 = new ColumnConstraints();
            cc1.setPercentWidth(50);
            ColumnConstraints cc2 = new ColumnConstraints();
            cc2.setPercentWidth(50);
            gpTabState.getColumnConstraints().addAll(cc1,cc2);
            RowConstraints row1 = new RowConstraints();
            row1.setPercentHeight(30);
            RowConstraints row2 = new RowConstraints();
            row2.setPercentHeight(70);
            gpTabState.getRowConstraints().addAll(row1,row2);
            tabState.setContent(gpTabState);
            
            /* produce tab for displaying episode data */
            learningLineChart = createSingleLineChart("fitness curve","episode","fitness","individual 0");
            GridPane gpTabLearning = new GridPane();
            ColumnConstraints ccl1 = new ColumnConstraints();
            ColumnConstraints ccl2 = new ColumnConstraints();
            ccl1.setPercentWidth(67);
            ccl2.setPercentWidth(33);
            gpTabLearning.getColumnConstraints().addAll(ccl1,ccl2);
            RowConstraints rowl1 = new RowConstraints();
            rowl1.setPercentHeight(100);
            gpTabLearning.getRowConstraints().addAll(rowl1);
            gpTabLearning.add(learningLineChart,0,0);
            gpTabLearning.add(taLearningCSV,1,0);
            gpTabLearning.setPadding(new Insets (4, 4, 4, 4));
            tabLearning.setContent(gpTabLearning);
            taLearningCSV.appendText("episode;result\n");
            
            /* produce tab for displaying general information */
            tabInfo.setContent(taInfo);
            
            // all tabs in the "TabPane"
            TabPane tabsInfo = new TabPane();
            tabsInfo.getTabs().add(tabState);
            tabsInfo.getTabs().add(tabLearning);
            tabsInfo.getTabs().add(tabInfo);
            
            Scene scene  = new Scene(tabsInfo,800,600);
          
            scene.getStylesheets().add(styleSheet);
            stage.setScene(scene);
            stage.show();
        });
    }

    private LineChart createLineChart(String title, String sXAxisLabel, String sYAxisLabel, String[] curveNames){ 
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(sXAxisLabel);
        yAxis.setLabel(sYAxisLabel);
       
        ArrayList <XYChart.Series> curves = new ArrayList <XYChart.Series> ();
        for (String cn : curveNames){
            XYChart.Series series = new XYChart.Series();
            series.setName(cn);
            curves.add(series);
        }
            
        LineChart <Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle(title);
        for ( XYChart.Series sXY : curves ){ 
            lineChart.getData().add(sXY);
        }
        
        setAxisBounds(lineChart,-this.displayTimeIntervalLength,0,true);
        return lineChart;
    }
    
     private LineChart createSingleLineChart(String title, String sXAxisLabel, String sYAxisLabel, String curveName){ 
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(sXAxisLabel);
        yAxis.setLabel(sYAxisLabel);     
        ArrayList <XYChart.Series> curves = new ArrayList <XYChart.Series> ();       
        XYChart.Series series = new XYChart.Series();
        series.setName(curveName);
        curves.add(series);
        LineChart <Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle(title);
        for ( XYChart.Series sXY : curves ){ 
            lineChart.getData().add(sXY);
        } 
        return lineChart;
    }
    
    /**
     * Appends new sensor and motor values (t corresponds to x).
     */
    public void append(int t, double[] sensorValues, double[] motorValues) throws Exception{
       if (sensorValues.length!=this.sensornames.length){
           throw new Exception("The number of sensors ("+sensornames.length+") does not match the number of values ("+sensorValues.length+") given.");
       }
       
       if (motorValues.length!=this.motornames.length) {
           throw new Exception("The number of sensors ("+motornames.length+")does not match the number of values ("+motorValues.length+") given.");
       }
       
       Platform.runLater(() -> {
           int i=0;
           ObservableList <XYChart.Series<Number,Number>> curves = s_lineChart.getData();  
           double lowerBound = t-this.displayTimeIntervalLength;
           int displayDataSize=this.displayTimeIntervalLength / displayStepWith +2; // divided by step width 
           // add a new value to all sensor- and motorcurves
           for(double y : sensorValues){
              curves.get(i).getData().add(new XYChart.Data <Number,Number>(t,y));
              int toRemove = curves.get(i).getData().size()-displayDataSize;
              if (toRemove>0) (curves.get(i).getData()).remove(0,toRemove);
              i++;
           }
           setAxisBounds(s_lineChart,lowerBound,t,true);
      
           i=0;
           curves = a_lineChart.getData();
           for(double y : motorValues){
              curves.get(i).getData().add(new XYChart.Data <Number,Number>(t,y));
              int toRemove = curves.get(i).getData().size()-displayDataSize;
              if (toRemove>0) (curves.get(i).getData()).remove(0,toRemove);
              i++;
           }
           setAxisBounds(a_lineChart,lowerBound,t,true);
       });
    }
   
    public void setAxisBounds(LineChart<Number, Number> myChart, double min, double max, boolean isXAxis) {
        NumberAxis axis;
        if (isXAxis)
            axis=(NumberAxis) myChart.getXAxis();
        else
            axis=(NumberAxis) myChart.getYAxis();
            
        axis.setAutoRanging(false);
        axis.setAnimated(false);
        axis.setTickLabelsVisible(false);
        axis.setTickUnit((max-min)/10);
        axis.setLowerBound(min);
        axis.setUpperBound(max);
    }
    
    public void clearEpsiodeData(){
        Platform.runLater(() -> {
            ObservableList <XYChart.Series<Number,Number>> curves = s_lineChart.getData();
            for ( XYChart.Series<Number,Number> series : curves ){
                series.getData().clear();
            }
            curves = a_lineChart.getData();
            for ( XYChart.Series<Number,Number> series : curves ){
                series.getData().clear();
            }
        });
    }
    
    /**
     * Updates the state informations about an agent.
     * @param info the state informations (attributes).
     */
    public void setAgentStateInfos(String info){
        Platform.runLater(() -> {
            taState1.setText(info);
        });
    }
    
    /**
     * Updates the additional state informations about an agent.
     * @param info the state informations (attributes).
     */
    public void setAgentStateAdditionalInfos(String info){
        Platform.runLater(() -> {
            taState2.setText(info);
        });
    }

    /**
     * Appends textual informations to the general information tab.
     * @param textual informations
     */
    public void append(String information){
        Platform.runLater(() -> {
            taInfo.appendText(information);
        });
    }
    
    /**
     * Appends a row of textual informations to the general information tab.
     * @param textual informations
     */
    public void appendln(String information){
        append(information+"\n");
    }
    
    /**
     * Appends a timestamp to the textual informations tab.
     */
    public void appendTimeStamp()
    {
        append(sdf.format(new Timestamp(System.currentTimeMillis())));
    }
    
    /**
     * Adds episode result data.
     */
    public void appendEpisodeResult(int episode, double y){
        this.taLearningCSV.appendText(episode+";"+nf.format(y)+"\n");
        ObservableList <XYChart.Series<Number,Number>> curves = learningLineChart.getData();     
        curves.get(0).getData().add(new XYChart.Data <Number,Number>(episode,y));  
    }
}
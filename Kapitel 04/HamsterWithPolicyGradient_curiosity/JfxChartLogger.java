import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * With this tool, on-line data accumulating in the program can be displayed "live", stored and compared
 * with previous series.
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
public class JfxChartLogger {
    private static final String styleSheet = "resources/logchart.css";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss-SSS_z", new Locale("en","US"));
    private Writer fileWriter = null;
    private boolean isLogFileOpen = false;
    private String logfile = null;
    
    private String descriptionOfCurve0 = "no name"; 
    
    private ArrayList <XYChart.Series<Number,Number>> curves = new ArrayList <XYChart.Series<Number,Number>> ();
    private LineChart<Number,Number> lineChart = null;
    
    private TextArea taChartCSV = new TextArea();
    private TextArea taInputCSV = new TextArea();
    private TextArea taTextLog = new TextArea();
    
    private Button btInsertSeries = null;
    private TextField tfDescriptionOfCurve = null;

    private Stage stage = null;
    
    public JfxChartLogger(String path, String diagramTitle, String descriptionOfCurve, String descXAxis, String descYAxis) {
        descriptionOfCurve0 = descriptionOfCurve;
        if (path!=null){
            logfile = path+"data_"+sdf.format(new Timestamp(System.currentTimeMillis()))+".csv";
            
            try{
                fileWriter = new FileWriter(logfile, false);
                isLogFileOpen = true;
                appendln("log file;"+logfile);
            }catch(java.io.IOException e){
               e.printStackTrace();
            }
               
        }
        
        Platform.runLater(()->{
            stage = new Stage();
            stage.setTitle(diagramTitle);
            Tab tabChart = new Tab("curve");
            Tab tabChartCSV = new Tab("data (csv)");
            Tab tabInputCSV = new Tab("insert comparison curve");
            Tab tabTextLog = new Tab("general info");
            
            TabPane loggerTabs = new TabPane();
            loggerTabs.getTabs().add(tabChart);
            loggerTabs.getTabs().add(tabChartCSV);
            loggerTabs.getTabs().add(tabInputCSV);
            loggerTabs.getTabs().add(tabTextLog);
           
            taChartCSV.setTooltip(new Tooltip("Here the data of the main curve is displayed in CSV format."));
            taChartCSV.appendText(descriptionOfCurve+"\n");
            taChartCSV.appendText("curve id;"+descXAxis+";"+descYAxis+"\n");
            tabChartCSV.setContent(taChartCSV);
           
            btInsertSeries = new Button("insert series");
            btInsertSeries.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event)
                {
                    String bez = tfDescriptionOfCurve.getText();
                    insertSeries(bez);
                }
            });
            tfDescriptionOfCurve = new TextField();
            tfDescriptionOfCurve.setEditable(false);
            tfDescriptionOfCurve.setPrefWidth(320);
            tfDescriptionOfCurve.setStyle("-fx-background-color: lightgray");
            tfDescriptionOfCurve.setTooltip(new Tooltip("The name corresponds to the first line from the input data field."));  
            taInputCSV.setTooltip(new Tooltip("Insert CSV data here to create another curve. (e.g. via copy&paste from a data tab)."));
            taInputCSV.textProperty().addListener((obs,textOld,textNew)->{
               BufferedReader br = new BufferedReader(new StringReader(textNew));
               try{
                   String firstLine = br.readLine();
                   tfDescriptionOfCurve.setText(firstLine);
                }catch(Exception e){
                    
                }
            });
           
            FlowPane fpTop = new FlowPane();
            fpTop.setHgap(8);
            fpTop.getChildren().add(btInsertSeries);
            fpTop.getChildren().add(new Label("  description:"));
            fpTop.getChildren().add(tfDescriptionOfCurve);

            BorderPane bpTab = new BorderPane();
            bpTab.setCenter(taInputCSV);
            bpTab.setTop(fpTop);
            BorderPane.setAlignment(fpTop,Pos.TOP_LEFT);
            BorderPane.setAlignment(taInputCSV,Pos.CENTER);
            BorderPane.setMargin(fpTop, new Insets(5,5,5,5));
            tabInputCSV.setContent(bpTab);
           
            tabTextLog.setContent(taTextLog);
             
            //create LineChart
            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel(descXAxis);
            yAxis.setLabel(descYAxis);
             
            XYChart.Series <Number,Number> series0 = new XYChart.Series <Number,Number> ();
            series0.setName(descriptionOfCurve0);
            curves.add(series0);
            
            lineChart = new LineChart<Number,Number>(xAxis,yAxis);
            lineChart.setCreateSymbols(false);
            lineChart.setTitle(diagramTitle);
            lineChart.getData().add(curves.get(0));
            tabChart.setContent(lineChart);
            
            Scene scene  = new Scene(loggerTabs,800,600);
            scene.getStylesheets().add(styleSheet);
            
            stage.setScene(scene);
            stage.show();
            scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        });
    }
    
    /** 
     * Tries to save the data before closing the window. (Dialog removed...)
     */
    private void closeWindowEvent(WindowEvent event) {
        System.out.println("Logger window close request ...");
     /*   Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().remove(ButtonType.OK);
        alert.getButtonTypes().add(ButtonType.CANCEL);
        alert.getButtonTypes().add(ButtonType.YES);
        alert.setTitle("Quit logging");
        alert.setContentText(String.format("Do you want to stop the data logging?"));
        alert.initOwner(stage.getOwner());
        Optional<ButtonType> res = alert.showAndWait();

        if(res.isPresent()) {
            if(res.get().equals(ButtonType.CANCEL)){
                event.consume();
            }else if(res.get().equals(ButtonType.YES)){*/
                try{
                    if (fileWriter!=null){
                        save(true);
                        System.out.println("Logger closed.");
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
         //   }
       // }     
    }        
    
    /**
     * Inserts a new curve with the CSV data from the corresponding text field.
     * @param bez Designation of the curve. (If a curve with the designation already exists, it will not be inserted).
     */
    private void insertSeries( String desc )
    {
        Platform.runLater(() -> {
            String description = desc;
            for (int i=0;i<curves.size();i++){
                XYChart.Series <Number,Number> serie = curves.get(i);
                if (serie.getName().equals(desc)){
                    System.out.println("A curve with the designation '"+desc+"' already exists!");
                    return;
                }
            }
            String csvDaten = taInputCSV.getText();
            List<XYChart.Data<Number,Number>> data = new ArrayList<>();
            int z=0; String line = null;
            try {
                BufferedReader br = new BufferedReader(new StringReader(csvDaten));
                while ((line = br.readLine()) != null) {
                    String[] xyStr = line.split(";"); 
                    try{
                        if (xyStr.length==3){
                            data.add(new Data <Number, Number> (Double.parseDouble(xyStr[1]),Double.parseDouble(xyStr[2])));
                        }else{
                            if ((z<2)&&(!line.contains(";"))){
                                description = line; 
                            }
                        }
                    }catch(NumberFormatException nfe){
                        if (z>=2) { // skip line 0:name, line 1:table header
                            System.out.println("Error in line "+z);
                        }
                    }
                    z++;
                }
                br.close();
            } catch(Exception e) {
                e.printStackTrace();
            }    
            addCurve(description,data);
        });
    }
    
    public int addCurve( String name ) {
        return addCurve(name,null);
    }
    
    /**
     * Adds a new curve to chart.
     * @param name short description string of the curve
     * @param data initial data
     * @return number of the curve
     */
    private int addCurve( String name, List<XYChart.Data<Number,Number>> data) {
        XYChart.Series <Number, Number> newSeries = new XYChart.Series <Number, Number> ();
        newSeries.setName(name);
        if (data==null) {
            data = new ArrayList <XYChart.Data<Number,Number>>();
        }
        newSeries.getData().addAll(data);
        int num = curves.size();
        curves.add(newSeries);
        lineChart.getData().add(newSeries);
        return num;
    }
    
    /**
     * Clears the curve data.
     */
    @SuppressWarnings("unchecked")
    public void clear(){
        Platform.runLater(() -> {
            curves.clear();
            lineChart.getData().removeAll();
            XYChart.Series <Number, Number> series0 = new XYChart.Series <Number, Number> ();
            series0.setName(descriptionOfCurve0);
            curves.add(series0);
        });
    }
    
    /**
     * Adds curve data to a y-values array. The data will be added to curve 0.
     * @param double[] y y-values array
     */
    public void append(double[] y){
        append(y,0);
    }
    
    /**
     * Adds curve data to a y-values array. The array indices serve as x-values.
     * @param double[] y y-values array
     * @param double[] curve_id Number of the curve to be added to.
     */
    public void append(double[] y, int curve_id){
       Platform.runLater(() -> {
           List<XYChart.Data <Number, Number>> data = new ArrayList<>();
           for(int i=0;i<y.length;++i){
               String csv_line = curve_id+";"+i+";"+y[i]+"\n";
               taChartCSV.appendText(csv_line);
               try{
                   if (isLogFileOpen) fileWriter.write(csv_line);
                } catch(java.io.IOException e) {
                    e.printStackTrace();
                }
               data.add(new XYChart.Data <Number, Number>(i,y[i]));
           }
           curves.get(curve_id).getData().addAll(data);
       });
    }

    /**
     * Adds curve data.
     * @param x argument
     * @param y result
     * @param additionalCSVData additional csv data/remarks
     */
    public void append(double x, double y){
        append(x,y,0,"");
    }
    
    /**
     * Adds curve data.
     * @param x argument
     * @param y result
     * @param additionalCSVData additional csv data/remarks
     */
    public void append(double x, double y, String additionalCSVData){
        append(x,y,0,additionalCSVData);
    }
    
    /**
     * Appends curve data to one of the existing curves.
     * @param x argument
     * @param y result
     * @param curve_id Number of the curve to be added to.
     * @param additionalCSVData additional csv data/remarks
     */
    public void append(double x, double y, int curve_id, String additionalCSVData){
         String csv_line = curve_id+";"+x+";"+y+";"+additionalCSVData+"\n";  
         try{
             if (isLogFileOpen) fileWriter.write(csv_line);
         } catch(java.io.IOException e) {
             e.printStackTrace();
         }
         Platform.runLater(() -> {
           taChartCSV.appendText(csv_line);
           curves.get(curve_id).getData().add(new XYChart.Data<>(x, y));
        });
    }
    
    /**
     * Adds textual information.
     * @param information The text to be added in the info tab.
     */
    public void append(String information){
        Platform.runLater(() -> {
            taTextLog.appendText(information);
            try{
               if (isLogFileOpen) fileWriter.write(information);
            } catch(java.io.IOException e) {
               e.printStackTrace();
            }
        });
    }
    
    /**
     * Adds a line of textual information.
     * @param information The text line to be added in the information tab.
     */
    public void appendln(String information){
        append(information+"\n");
    }
    
    /**
     * Adds a timestamp in the info tab.
     */
    public void appendTimeStamp()
    {
        append(getTimeStamp());
    }
    
    public static String getTimeStamp(){
        return sdf.format(new Timestamp(System.currentTimeMillis()));
    }
    
    /**
     * Store cached data to disc.
     */
    public void save(boolean close){
         try{
             if (isLogFileOpen){ 
                 if (close) fileWriter.write("Closing log file. ("+getTimeStamp()+")");
                 fileWriter.close();
                 isLogFileOpen = false;
                 System.out.println("Log file "+logfile+" ("+getTimeStamp()+") saved.");
                 if (!close){
                     fileWriter = new FileWriter(logfile, true);
                     isLogFileOpen = true;
                 }
             }else{
                 System.out.println("Logfile is already closed!");
             }
         }catch(java.io.IOException e){
               e.printStackTrace();
         }
    }  
}
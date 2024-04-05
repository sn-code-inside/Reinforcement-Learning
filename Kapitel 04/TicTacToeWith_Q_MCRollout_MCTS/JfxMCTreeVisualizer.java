import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;

/**
 * Tool for displaying monte-carlo trees.
 * 
 * Supplementary material to the Springer book
 * 'Reinforcement Learning from scratch - Understanding current approaches with Java and Greenfoot' 
 * Uwe Lorenz
 * 
 * https://www.facebook.com/ReinforcementLearningJava
 *
 * license: CC BY-SA 4.0 (Attribution-ShareAlike)
 *
 * @author Uwe Lorenz
 * @version 0.9 (nicht finalisiert)
 */
public class JfxMCTreeVisualizer{
    private TreeView <MCT_Node> treeView;
    private TextArea taTextLog;
    private static final String styleSheet = "resources/logchart.css";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss,SSS");
   
    public JfxMCTreeVisualizer(MC_Tree mct, String diagrammTitel) {
        Platform.runLater(()->{
            Stage stage = new Stage();
            stage.setTitle(diagrammTitel);
            
            Tab tabVisualization = new Tab("Baum");
            Tab tabTextLog = new Tab("Infos");
             
            TabPane loggerTabs = new TabPane();
            loggerTabs.getTabs().add(tabVisualization);
            loggerTabs.getTabs().add(tabTextLog);
            
            taTextLog = new TextArea();
            tabTextLog.setContent(taTextLog);
            
            // mct root
            treeView = erstelleVisualization(mct.getRoot());
         
            StackPane sTreeViewPane = new StackPane();
            sTreeViewPane.setPadding(new Insets(5));
            sTreeViewPane.getChildren().add(treeView);
           
            tabVisualization.setContent( sTreeViewPane );

            Scene scene  = new Scene(loggerTabs,800,600);
            scene.getStylesheets().add(styleSheet);
            stage.setScene(scene);
            stage.show();
        });
    }
    
    public void updateVisualisation(MC_Tree mct){
        Platform.runLater(()->{
            treeView = erstelleVisualization(mct.getRoot());
        });
    }

    private TreeView <MCT_Node> erstelleVisualization(MCT_Node rootknoten){
        Node nodeIcon = produziereNodeIcon(rootknoten.getState());
        TreeItem<MCT_Node> tiRoot = new TreeItem<MCT_Node> (rootknoten,nodeIcon);   
        rekursiverAufbau(rootknoten,tiRoot,0);
        return new TreeView<MCT_Node>(tiRoot);
    }
    
    private void rekursiverAufbau(MCT_Node vaterknoten, TreeItem tiVater, int tiefe ){
        ArrayList <MCT_Node> kinder = vaterknoten.getChildren();
        for (MCT_Node kind : kinder){
            Node nodeIcon = produziereNodeIcon(kind.getState());
            TreeItem<MCT_Node> tiKind = new TreeItem<MCT_Node> (kind,nodeIcon);            
            tiVater.getChildren().add(tiKind);
            rekursiverAufbau(kind, tiKind, tiefe+1);
        }
    }
    
    private Node produziereNodeIcon(char[] zustand){
        Label lbS = new Label(TicTacToe_Env.matrixToString(zustand));
        lbS.setFont(new Font("Courier New", 12));
        Scene scene = new Scene(new StackPane(lbS));
        return new ImageView(lbS.snapshot(null, null));
    }
    
    /**
     * Fügt Textinformationen hinzu.
     * @param information Text, der im Info-Reiter hinzugefügt werden soll.
     */
    public void append(String information){
        Platform.runLater(() -> {
            taTextLog.appendText(information);
        });
    }
    
    /**
     * Fügt eine Zeile Textinformationen hinzu.
     * @param information Textzeile,die im Info-Reiter hinzugefügt werden soll.
     */
    public void appendln(String information){
        append(information+"\n");
    }
    
    /**
     * Fügt einen Zeitstempel im Info-Reiter hinzu.
     */
    public void appendTimeStamp()
    {
        append(sdf.format(new Timestamp(System.currentTimeMillis())));
    }
    
}

package sniffer.main.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

import org.quartz.SchedulerException;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sniffer.main.schedule.CleanImagesDirScheduler;
import sniffer.main.socket.SocketSniffer;
import sniffer.main.utils.LogStyle;
import sniffer.main.utils.Utils;

public class MainSceneController implements Initializable{
	
	private Stage mainStage;
	
	@FXML
	private VBox logContainer;
	
	@FXML
	private HBox choosedPropsWrapper;
	
	@FXML
	private VBox choosePropsWrapper;
	
	@FXML
	private Label portNumLabel,versionNumLabel;
	
	@FXML
	private StackPane rootStack;
	
	private final Tooltip labelPortTooltip = new Tooltip();
	
	private SocketSniffer socketSniffer;
	private CleanImagesDirScheduler cleanImagesDirScheduler;
	
	private Thread acceptClients;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		String version = "Work in progress..";
		if(getClass().getPackage().getImplementationVersion() != null) {
			version  = getClass().getPackage().getImplementationVersion();
		}
		versionNumLabel.setText(version);
		socketSniffer = new SocketSniffer();
		cleanImagesDirScheduler = new CleanImagesDirScheduler();
		try {
			cleanImagesDirScheduler.startSchedule();
		} catch (SchedulerException e) {
			Utils.getInstance().doWhenExceptionOccurs(e, "Unenable to start scheduler to set cleaning dir");
			e.printStackTrace();
		}
		labelPortTooltip.setText("Fare il doppio click sul numero della porta per chiudere la connessione.");
		labelPortTooltip.setStyle("-fx-font-size: 12");
		handlePortLabelAction();
	}

	public void handleOnCloseEvent() {
		socketSniffer.setStopped(true);
		if(acceptClients != null) {
			acceptClients.stop();
		}
		try {
			cleanImagesDirScheduler.stopSchedule();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
	
	public void handleError(Exception ex, String message) {
		
		Platform.runLater(() -> {
			JFXDialogLayout dialogLayout = new JFXDialogLayout();
			dialogLayout.setMaxHeight(720);
			dialogLayout.setMaxWidth(1080);
			JFXDialog dialog = new JFXDialog(rootStack, dialogLayout, JFXDialog.DialogTransition.CENTER);
			Label heading = new Label("Some error occured");
			heading.getStyleClass().addAll("font-18","error-dialog-heading");
			dialogLayout.setHeading(heading);
			
			JFXButton close = new JFXButton("Chiudi");
			close.setOnMouseClicked(event -> {
				dialog.close();
			});
			dialogLayout.setActions(close);
			
			VBox content = new VBox(10);
			content.setAlignment(Pos.CENTER);
			Label shortError = new Label(ex.getMessage());
			StackPane errorContainer = new StackPane();
			ScrollPane scroll = new ScrollPane(errorContainer);
			scroll.getStyleClass().add("transparent-bg-scroll-pane");
			shortError.setOnMouseClicked(event ->{
				scroll.setVisible(true);
				scroll.setManaged(true);
			});
			shortError.setWrapText(true);
			shortError.setMaxWidth(350);
			shortError.getStyleClass().addAll("font-12", "dialog-short-message");
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			Label errorText = new Label(sw.toString());
			errorText.setWrapText(true);
			errorText.setMaxWidth(980);
			scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
			errorText.getStyleClass().addAll("font-12", "dialog-short-message");
			errorContainer.getChildren().add(errorText);
			scroll.setVisible(false);
			scroll.setManaged(false);
			content.getChildren().addAll(shortError, new Separator(),scroll);
			dialogLayout.setBody(content);
			
			dialog.show();

		});
	}
	
	public void appendInfo(String message, LogStyle logStyle) {
		Platform.runLater(() ->{
			Label text = new Label(message);
			text.getStyleClass().add(logStyle.getStyle());
			logContainer.getChildren().add(text);
		});
		
	}
	
	public void appendImageDownloadedInfo(String message, final String imgPath) {
		Platform.runLater(() ->{
			Label text = new Label(message);
			text.getStyleClass().add(LogStyle.SUCCESS.getStyle());
			text.setOnMousePressed(event -> {
				File f = new File(imgPath);
				openFile(f);
			});
			logContainer.getChildren().add(text);
		});
		
	}
	
	private void openFile(File file) {
		if(file != null) {
			Desktop dt = Desktop.getDesktop();
		    try {
				dt.open(file);
			} catch (IOException e) {
				Utils.getInstance().doWhenExceptionOccurs(e, String.format("Unenable to open image with path %s", file.getAbsolutePath()));
				e.printStackTrace();
			}
		}
	}
	
	public void applyStyletoPortNumLabel(LogStyle logStyle) {
		Platform.runLater(() -> {
			portNumLabel.getStyleClass().clear();
			portNumLabel.getStyleClass().addAll("font-18",logStyle.getStyle());
			if(logStyle == LogStyle.SUCCESS) {
				portNumLabel.setTooltip(labelPortTooltip);
			}else {
				portNumLabel.setTooltip(null);
			}
		});
	}
	
	public void handleChooseFile(ActionEvent event) {
		FileChooser fileChooser = showFileChooser();
		File file = fileChooser.showOpenDialog(mainStage);
		if(file != null) {
			Integer portNum = Utils.getInstance().getPortFromPropsFile(file);
			if(portNum != null) {
				portNumLabel.setText(portNum.toString());
				applyStyletoPortNumLabel(LogStyle.WARN);
				choosePropsWrapper.setVisible(false);
				choosedPropsWrapper.setVisible(true);
				 Runnable task = () -> {
					 socketSniffer.acceptClients(portNum, false);
				 };
				 acceptClients = new Thread(task);
				 acceptClients.setDaemon(true);
				 acceptClients.start();
				 handleError(new UnsupportedOperationException("Messaggio di test"), "Messaggio di testst asdasda");
			}
			
		}
		
	}
	
	public void handlePortLabelAction() {
		portNumLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
		    @Override 
		    public void handle(MouseEvent event) {
		        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
		        	if(portNumLabel.getStyleClass().contains(LogStyle.SUCCESS.getStyle())) {
			        	Alert alert = new Alert(AlertType.CONFIRMATION, "La connessione con il socket verrà terminata, vuoi procedere?", ButtonType.YES, ButtonType.NO);
			        	alert.showAndWait();
			        	if (alert.getResult() == ButtonType.YES) {
			        		socketSniffer.setStopped(true);
			        	}
		        	}
		        }
		    }
		});
	}
	
	private FileChooser showFileChooser() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Selezionare il file di properties *.properties");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PROPERTIES files (*.properties)", "*.properties");
		fileChooser.getExtensionFilters().add(extFilter);
		return fileChooser;
	}
}

package sniffer.main;

import com.jfoenix.controls.JFXDecorator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import sniffer.main.controller.MainSceneController;
import sniffer.main.utils.Constants;
import sniffer.main.utils.Utils;

public class MainApp extends Application{
	
	private final Double SCALE_W = 0.5;
	private final Double SCALE_H = 0.27;
	
	public static void run(String args[]) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		initStage(primaryStage);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-scene.fxml"));
		Parent root = (Parent)loader.load();
		MainSceneController controller = (MainSceneController) loader.getController();
		controller.setMainStage(primaryStage);
		Utils.getInstance().setMainSceneControler(controller);
		JFXDecorator decorator = new JFXDecorator(primaryStage, root);
		Scene mainScene = new Scene(decorator);
		mainScene.getStylesheets().add(getClass().getResource("/css/fonts.css").toExternalForm());
		mainScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(mainScene);
		primaryStage.setOnCloseRequest(windowEvent -> {
			controller.handleOnCloseEvent();
		});
		primaryStage.show();
		
	}
	
	
	private void initStage(Stage primaryStage) {
		primaryStage.setTitle(Constants.APP_TITLE);
		primaryStage.getIcons().add(new Image(getClass().getResource("/favico.png").toExternalForm()));
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double prefferedWidth = screenBounds.getWidth() * SCALE_W;
		double prefferedHeight = screenBounds.getWidth() * SCALE_H;
		primaryStage.setWidth(prefferedWidth);
		primaryStage.setHeight(prefferedHeight);
		
	}

}

package client;

import java.io.IOException;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;

import javafx.scene.image.ImageView;

import javafx.event.Event;

import database_manipulator.User;


public class LoginController {
	
	public Main _med_;
	
	public TextField username_field;
	public PasswordField password_field;
	public Label info_label;
	public CheckBox remember_me_checkbox;
	public ImageView logo_image;
	
	public LoginController(){
	}
	public LoginController(Main _med_) {
		this._med_ = _med_;
	}
	
	public void login(Event ev) throws Exception
	{
		String uname = username_field.getText();
		String pass = password_field.getText();
		
		_med_.current_user = User.getByUname(uname, _med_.connector.conn);
		User current_user = _med_.current_user;
		if(current_user!=null && current_user.getPassword().equals(pass)) {
			_med_.home_controller.start((Stage)((Control)ev.getSource()).getScene().getWindow());
		}
		else {
			_med_.current_user = null;
			info_label.setText("Invalid username or password!");
		}
		if(remember_me_checkbox.isSelected()) {
			Main.write_cookie(uname+","+pass);
		}
		else {
			Main.write_cookie("");
		}
		
	}
	
	public void goto_signup(Event ev) throws IOException 
	{
		
		_med_.signup_controller.start((Stage)((Control)ev.getSource()).getScene().getWindow());
	}
	
	
	public void load_fxml() throws IOException
	{
		FXMLLoader fl = new FXMLLoader();
		Parent root = fl.load(getClass().getResource("Login.fxml").openStream());
		info_label = (Label)Pane.class.cast(root).getChildren().get(0);
	}
	
	
	public void start(Stage window) throws IOException
	{
		FXMLLoader fl = new FXMLLoader();
		Parent root = fl.load(getClass().getResource("Login.fxml").openStream());
		_med_.login_controller = fl.getController();
		
		_med_.login_controller._med_ = _med_;
		
		Scene scene = new Scene(root);
		window.setScene(scene);
		window.show();
	}
}

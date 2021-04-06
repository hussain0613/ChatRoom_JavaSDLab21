package client;

import java.io.IOException;
import java.sql.SQLException;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import database_manipulator.User;


public class SignUpController {
	public Main _med_;
	
	public TextField name_field, username_field, email_field;
	public PasswordField password_field, confirm_password_field;
	public Label info_label;
	
	public SignUpController(){
	}
	
	public SignUpController(Main _med_){
		this._med_ = _med_;
	}
	
	public void signup(Event ev) throws SQLException, IOException, ClassNotFoundException
	{
		String name = name_field.getText();
		String uname = username_field.getText();
		String email = email_field.getText();
		email.toLowerCase();
		String pass = password_field.getText();
		String pass2 = confirm_password_field.getText();
		if(!pass.equals(pass2)) {
			info_label.setText("Passwords do not match!");
		}
		else if(pass.length()<5) {
			info_label.setText("Password must be at least 5 characters long.");
		}
		else if(uname.length() == 0 || !uname.matches("^[a-zA-Z0-9._]+$")) info_label.setText("Invalid username.");
		else if(email.length()<5 || !email.matches("^[a-z0-9._-]+@[a-z0-9.-]+\\.[a-z0-9]{2,6}$")) info_label.setText("Invalid email");
		else {
				
			try {
				User.create_new_user(name, uname, email, pass, _med_.connector.conn);
				info_label.setText("Successfully created account!");
				goto_login(ev);
			}catch(MySQLIntegrityConstraintViolationException e) {
				String err_msg = e.getMessage();
				if(err_msg.contains("username")) {
					info_label.setText("Username already exists");
				}
				else if(err_msg.contains("email")) info_label.setText("Email already exists!");
				else info_label.setText(err_msg);
			}
		}
	}
	
	public void goto_login(Event ev) throws IOException 
	{
		
		_med_.login_controller.start(_med_.window);
		_med_.login_controller.info_label.setText(info_label.getText());
	}
	
	
	public void start(Stage window) throws IOException
	{
		FXMLLoader fl = new FXMLLoader();
		Parent root = fl.load(getClass().getResource("SigunUp.fxml").openStream());
		_med_.signup_controller = fl.getController();
		_med_.signup_controller._med_ =_med_;
		Scene scene = new Scene(root);
		window.setScene(scene);
		window.show();
	}
}

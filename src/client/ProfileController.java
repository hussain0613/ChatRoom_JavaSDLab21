package client;

import java.io.IOException;
import java.sql.SQLException;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import database_manipulator.User;

public class ProfileController {
	Main _med_;
	public TextField name_field, username_field, email_field;
	public PasswordField password_field, confirm_password_field;
	public Label info_label, email_lbl, psd_lbl;
	public Button multi_purpose_button, confirm_button, dlt_acnt_btn;
	public User user;
	
	public ProfileController() {}
	public ProfileController(Main _med_) {
		this._med_ = _med_;
	}
	
	public void edit(Event ev)
	{
		if(multi_purpose_button.getText().equals("Edit")) {
			set_info_fields_editable(true);
		}
		else if(multi_purpose_button.getText().equals("Save")) {
			String pass = password_field.getText(), uname = username_field.getText(), email = email_field.getText();
			if(pass.length()<5) {
				info_label.setText("Password must be at least 5 characters long.");
			}
			else if(uname.length() == 0 || !uname.matches("^[a-zA-Z0-9._]+$")) info_label.setText("Invalid username.");
			else if(email.length()<5 || !email.matches("^[a-z0-9._%-]+@[a-z0-9.%-]+\\.[a-z0-9]{2,6}$")) info_label.setText("Invalid email");
			else {
				user.setName(name_field.getText());
				user.setUsername(username_field.getText());
				user.setEmail(email_field.getText());
				user.setPassword(password_field.getText());
				set_confirm_fields_visible(true);
				confirm_button.setOnAction(cev->{
					confirm_edit(cev);
				});
			}
		}
	}
	
	
	public void delete_account(Event ev) {
		set_confirm_fields_visible(true);
		confirm_button.setOnAction(cev->{
			confirm_delete(cev);
		});
	}
	
	public void confirm_edit(Event ev)
	{
		if(_med_.current_user.getPassword().equals(confirm_password_field.getText())) {
			try {
				user.save();
				_med_.home_controller.info_label.setText("Succesfully updated!");
				
				set_info_fields_editable(false);
				set_confirm_fields_visible(false);
				
			} catch (SQLException e) {
				_med_.home_controller.info_label.setText(e.getMessage());
				
				set_confirm_fields_visible(false);
				set_info_fields_editable(true);
			}
		}
		else {
			_med_.home_controller.info_label.setText("Wrong password");
		}
	}
	
	
	public void confirm_delete(Event ev) {
		if(_med_.current_user.getPassword().equals(confirm_password_field.getText())) {
			try {
				User.delete(user.getId(), _med_.connector.conn);
				try {
					_med_.home_controller.logout(ev);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SQLException e) {
				_med_.home_controller.info_label.setText(e.getMessage());
				
				set_confirm_fields_visible(false);
				set_info_fields_editable(true);
			}
		}
		else {
			_med_.home_controller.info_label.setText("Wrong password");
		}
	}
	
	
	public void set_info_fields_editable(boolean is_editable){
		name_field.setEditable(is_editable);
		username_field.setEditable(is_editable);
		email_field.setEditable(is_editable);
		password_field.setEditable(is_editable);
		
		if(is_editable) {
			multi_purpose_button.setText("Save");
		}
		else {
			multi_purpose_button.setText("Edit");
		}
		
		// doing them manually to prevent circular infinite calling
		confirm_password_field.setVisible(!is_editable);
		confirm_button.setVisible(!is_editable);
		
		if(is_editable)confirm_password_field.setText(null);
		//if(is_editable) multi_purpose_button.setText("Edit");
	}
	
	public void set_confirm_fields_visible(boolean is_visible){
		set_info_fields_editable(!is_visible);
		
		confirm_password_field.setVisible(is_visible);
		confirm_button.setVisible(is_visible);
		
		if(!is_visible)confirm_password_field.setText(null);
		if(is_visible) multi_purpose_button.setText("Edit");
	}
	
	
	//public void post_load_init() {
	public void post_load_init(Main _med_) {
		this._med_ = _med_;
		if(_med_.current_user == null) {
			info_label.setText("Not logged in!");
			return;
		}
		name_field.setText(user.getName());
		username_field.setText(user.getUsername());
		email_field.setText(user.getEmail());
		password_field.setText(user.getPassword());
		
		if(_med_.current_user.getId() != user.getId()) {
			multi_purpose_button.setVisible(false);
			email_field.setVisible(false);
			password_field.setVisible(false);
			email_lbl.setVisible(false);
			psd_lbl.setVisible(false);
			dlt_acnt_btn.setVisible(false);
		}
	}
	
		
	public Parent getRoot(User user) throws IOException
	{
		FXMLLoader fl = new FXMLLoader();
		Parent root = fl.load(getClass().getResource("Profile.fxml").openStream());
		_med_.profile_controller = fl.getController();
		_med_.profile_controller.user = user;
		_med_.profile_controller.post_load_init(_med_);
		
		_med_.home_controller.title.setText(user.getUsername());
		return root;
	}
	
}

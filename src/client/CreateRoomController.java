package client;

import java.io.IOException;
import java.sql.SQLException;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Control;
import database_manipulator.Room;

public class CreateRoomController {
	public Main _med_;
	
	public TextField roomname_field;
	
	public CreateRoomController() {}
	public CreateRoomController(Main _med_) {
		this._med_ = _med_;
	}
	
	public void create_room(Event ev) throws SQLException, IOException
	{
		Room.create_new_room(_med_.current_user.getId(), roomname_field.getText(), _med_.connector.conn);
		_med_.home_controller.start( (Stage) ((Control)ev.getSource()).getScene().getWindow() );
		_med_.home_controller.info_label.setText("Room Created Successfully!");
	}
	
	public Parent getRoot() throws IOException
	{
		FXMLLoader fl = new FXMLLoader();
		Parent root = fl.load(getClass().getResource("CreateRoom.fxml").openStream());
		/*
		Main.create_room_controller = fl.getController();
		Main.home_controller.title.setText("Create new room");
		*/
		
		_med_.create_room_controller = fl.getController();
		_med_.create_room_controller._med_ = _med_;
		_med_.home_controller.title.setText("Create new room");
		
		return root;
	}

}

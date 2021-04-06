package client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database_manipulator.Invitation;
import database_manipulator.Membership;
import database_manipulator.Room;
import database_manipulator.User;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;



public class HomeController implements Runnable{
	public Main _med_;
	
	public Label info_label, title;
	public ScrollPane mainbody, leftNav;
	public ArrayList<Membership> memberships;
	public HashMap<Integer, Hyperlink> membership_hps;
	public Button inv_btn;
	
	//ArrayList<Invitation> invs;
	
	Thread room_check, inv_check;
	//public Button left_nav_toggole_button;
	
	public HomeController(){
		
	}
	
	public HomeController(Main _med_){
		this._med_ = _med_;
	}
	
	public void logout(Event ev) throws Exception
	{
		stop();
		_med_.login_controller.start( (Stage) ((Control)ev.getSource()).getScene().getWindow());
		_med_.login_controller.info_label.setText("");
		Main.write_cookie("");
	}
	
	public void goto_home(Event ev) throws IOException 
	{
		if(_med_.room_view_controller != null) _med_.room_view_controller.stop();
		
		title.setText("Home");
		mainbody.setContent(_med_.profile_controller.getRoot(_med_.current_user));
	}
	
	public void goto_invitations(Event ev) throws SQLException
	{
		if(_med_.room_view_controller != null) _med_.room_view_controller.stop();
		title.setText("Invitations");
		//_med_.invitations_controller.post_load_init();
		_med_.invitations_controller.post_load_init(_med_);
	}
	
	
	public void check_memberships_timestamps() throws SQLException 
	{
		// implementing in one of various way, time complexity wise not one of good ones may be.
		while(_med_.current_user != null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				break;
			}
			/*ArrayList<Membership>*/ memberships = Membership.getByUserId(_med_.current_user.getId(), _med_.connector.conn);
			if(memberships.size() == 0) continue;
			Iterator<Membership> it = memberships.iterator();
			while(it.hasNext()) {
				Membership mb = it.next();
				mb.update_obj();
				if(_med_.room_view_controller.mb != null && _med_.room_view_controller.mb.getId() == mb.getId()) {
					//mb.update_obj();
					//continue;
				}
				if(mb.getLast_seen_message_ts() < mb.getRoom().getLast_message_timestamp()) {
					//System.out.println(mb.getId());
					Hyperlink hp = membership_hps.get(mb.getId());
					if(hp!= null) hp.setStyle("-fx-font-weight: bold;");
				}
			}
		}
		//check_memberships_timestamps();
	}
	
	public void check_invitations() throws SQLException {
		boolean flag;
		while(_med_.current_user != null) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				break;
			}
			// shudhu pending ba null gula filter kore ber korar bebostha korle bhalo hbe may be
			ArrayList<Invitation> invs = Invitation.getByUserId(_med_.current_user.getId(), _med_.connector.conn);
			if(invs.size() == 0) continue;
			Iterator<Invitation> it = invs.iterator();
			flag = false;
			while(it.hasNext()) {
				Invitation inv = it.next();
				if(inv.getStatus().equals("pending") || inv.getStatus().equals("null")) {
					//inv_btn.setStyle("-fx-font-weight: bold;");
					flag = true;
					break;
				}
			}
			//System.out.println("hasPendingInv: "+ flag);
			if(flag) inv_btn.setStyle("-fx-font-weight: bold;");
			else inv_btn.setStyle("-fx-font-weight: normal;");
		}
	}
	
	public void check_rooms() {
		
		while(true) {
			
		}
	}
	
	public void run() {
		System.out.println("Starting thread: "+ room_check.getName());
		try {
			check_memberships_timestamps();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	
	//public void post_load_init() throws IOException, SQLException{
	public void post_load_init(Main _med_) throws IOException, SQLException{
		this._med_ = _med_;
		membership_hps = new HashMap<Integer, Hyperlink>();
		mainbody.setContent(_med_.profile_controller.getRoot(_med_.current_user));
		try {
			populate_left_nav();
		}catch(SQLException e) {
			System.out.println(e);
		}
		
		room_check = new Thread(this);
		room_check.setName(room_check.getName()+" room's last msg check");
		room_check.start();
		
		
		inv_check = new Thread(()->{
			System.out.println("Starting thread: "+ inv_check.getName());
			try {
				check_invitations();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			});
		inv_check.setName(inv_check.getName()+" inv check");
		inv_check.start();
	}
	
	
	public void populate_left_nav() throws SQLException
	{
		User user = _med_.current_user;
		memberships = user.getMemberships(_med_.connector.conn);
		Iterator<Membership> it = memberships.iterator();
		
		VBox vbox = new VBox(25);
		Button create_room_button = new Button();
		create_room_button.setText("+");
		create_room_button.setOnAction(ev->{
			try {
				mainbody.setContent(_med_.create_room_controller.getRoot());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		vbox.getChildren().add(create_room_button);
		
		while(it.hasNext()) {
			Membership mb = it.next();
			Room room = mb.getRoom();
			Hyperlink hp = new Hyperlink();
			membership_hps.put(mb.getId(), hp);
			hp.setText(mb.getRoom().getName());
			
			if(mb.getLast_seen_message_ts() < room.getLast_message_timestamp()) {
				hp.setStyle("-fx-font-weight: bold;");
			}
			
			hp.setOnAction(ev->{
				try {
					if(_med_.room_view_controller != null) _med_.room_view_controller.stop();
					if(mb == null || mb.getRoom() == null) {
						_med_.home_controller.start(_med_.window);
						_med_.home_controller.info_label.setText("You are not a member of the room or the room does not exist.");
					}
					else mainbody.setContent(_med_.room_view_controller.getRoot(mb.getRoom()));
				} catch (IOException | SQLException e) {
					e.printStackTrace();
				}
			});
			vbox.getChildren().add(hp);
			
			Separator sep = new Separator();
			sep.setOrientation(Orientation.HORIZONTAL);
			sep.setPrefSize(25, 2);
			vbox.getChildren().add(sep);
		}
		vbox.setPadding(new Insets(50, 10, 10, 10));
		leftNav.setContent(vbox);
	}
	
	public void start(Stage window) throws IOException, SQLException
	{
		FXMLLoader fl = new FXMLLoader();
		Parent root = fl.load(getClass().getResource("Home.fxml").openStream());
		/*
		Main.home_controller = fl.getController();
		Main.home_controller.post_load_init();
		*/
		
		_med_.home_controller = fl.getController();
		_med_.home_controller.post_load_init(_med_);
		
		Scene scene = new Scene(root);
		
		scene.setRoot(root);
		window.setScene(scene);
		window.show();
	}
	
	public void stop()
	{
		if(inv_check != null && !inv_check.isInterrupted()) inv_check.interrupt();
		if(room_check != null && !room_check.isInterrupted()) room_check.interrupt();
		if(_med_.room_view_controller != null) _med_.room_view_controller.stop();
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_med_.current_user = null;
	}
}

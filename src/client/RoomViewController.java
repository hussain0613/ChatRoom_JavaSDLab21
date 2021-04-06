package client;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import database_manipulator.Invitation;
import database_manipulator.Membership;
import database_manipulator.Message;
import database_manipulator.Room;

import database_manipulator.User;

public class RoomViewController implements Runnable{
	public Main _med_;
	
	public ScrollPane room_nav, display;
	public TextArea chatbox;
	public Room room;
	public Membership mb;
	public double client_end_last_seen_msg_time_stamp;
	public Thread room_msg_check;
	
	public VBox display_root;
	
	public RoomViewController() {}
	public RoomViewController(Main _med_) {
		this._med_ = _med_;
	}
	
	public void check_msg() throws SQLException {
		while(_med_.current_user !=null && _med_.room_view_controller.mb != null && room != null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				break;
			}
			mb = Membership.getByUserIdAndRoomId(_med_.current_user.getId(), room.getId(), _med_.connector.conn);
			if(mb == null || _med_.current_user == null) break;
			room = Room.getById(room.getId(), _med_.connector.conn);
			if((mb != null) && (client_end_last_seen_msg_time_stamp < room.getLast_message_timestamp() ||
					mb.getLast_seen_message_ts() < room.getLast_message_timestamp())) {
				//System.out.println("found some msgs it seems, room id = " + room.getId() + " current_user = " + _med_.current_user.getId());
				
				ArrayList<Message> msgs= room.getMessages(client_end_last_seen_msg_time_stamp);
				//System.out.println("msgs arraylist: "+ msgs + "timestamps ");
				Iterator<Message> it = msgs.iterator();
				while(it.hasNext()) {
					Message msg = it.next();
					Platform.runLater(()->{
					try {
						create_message_card(msg.getUser().getUsername(), msg.getMessage(), msg.getCreated());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}});
					//create_message_card(msg.getUser().getUsername(), msg.getMessage(), msg.getCreated());
					client_end_last_seen_msg_time_stamp = msg.getCreated();
					mb.setLast_seen_message_ts(client_end_last_seen_msg_time_stamp);
					mb.save();
					//System.out.println("eikhane to kaj korar kotha.. :(");
				}
				
				//display.setVvalue(1.0);
			}
			//else {
				//System.out.println("now new msgs yet for this room or somethin's wrong, room id = " + room.getId() + " current_user = " + _med_.current_user.getId());
			//}
		}
	}
	
	public void check_members() {}
	
	public void run() {
		System.out.println("Starting thread: "+ room_msg_check.getName());
		try {
			check_msg();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send_message() throws SQLException {
		if(chatbox.getText() == null || chatbox.getText().equals("")) return;
		// membership and room are being updated inside this func
		Message msg = Message.create_message(chatbox.getText(), _med_.current_user.getId(), room.getId(), _med_.connector.conn);
		if (msg == null) {
			_med_.home_controller.info_label.setText("You are not a member of this room or the room does not exist.");
		}
		//client_end_last_seen_msg_time_stamp = msg.getCreated();
		
		//System.out.println(msg.getCreated());
		//create_message_card(_med_.current_user.getUsername(), chatbox.getText(), msg.getCreated());
		chatbox.setText(null);
	}
	
	public void create_message_card(String sender_uname, String msg, double timestamp) throws SQLException {
		if(msg == null || sender_uname == null) return;
		User sender = User.getByUname(sender_uname, _med_.connector.conn);
		
		Pane card = new Pane();
		
		Hyperlink sender_uname_hp = new Hyperlink(sender_uname);
		
		Label msg_lbl = new Label(msg);
		Separator sep = new Separator();
		sender_uname_hp.setStyle("-fx-font-weight: bold;");
		msg_lbl.setWrapText(true);
		
		sender_uname_hp.setOnAction(ev->{
			try {
				_med_.home_controller.mainbody.setContent(_med_.profile_controller.getRoot(sender));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		Date created_dbl = new Date((long) (timestamp));
		//String created_str = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(created_dbl);
		SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd hh:mm a");
		df.setTimeZone(TimeZone.getTimeZone("BDT"));
		String created_str = df.format(created_dbl);
		
		Label time_label = new Label(created_str);
		time_label.setStyle("-fx-font-size: 10");
		
		HBox hbox = new HBox(15);
		hbox.getChildren().addAll(sender_uname_hp, time_label);
		
		
		sep.setOrientation(Orientation.HORIZONTAL);
		sep.setLayoutY(25);
		msg_lbl.setLayoutY(30);
		sep.prefWidthProperty().bind(hbox.widthProperty());
		
		card.getChildren().add(hbox);
		card.getChildren().add(sep);
		card.getChildren().add(msg_lbl);
		
		display_root.getChildren().add(card);
		
		if(sender.getId() == _med_.current_user.getId()) {
			display.setVvalue(1.0); // onnanno notun msg er jonno ekta button pop up hobe bottome jaoar jonno
			_med_.home_controller.membership_hps.get(mb.getId()).setStyle("-fx-font-weight: normal");
		}
		/*else {
			//display.setVvalue(1.0);
			display.setVvalue( ((VBox)display.getContent()).getHeight() );
			
		}*/
	}
	
	public void populate_display() throws SQLException {
		
		VBox root = new VBox(15);
		root.setPadding(new Insets(20));
		display.setContent(root);
		display_root = root;
		
		// eita pore karon create_message_card kaj korte hoile display er content thaka laage
		ArrayList<Message> msgs= Message.getByRoomId(room.getId(), _med_.connector.conn);
		Iterator<Message> it = msgs.iterator();
		while(it.hasNext()) {
			Message msg = it.next();
			create_message_card(msg.getUser().getUsername(), msg.getMessage(), msg.getCreated());
			client_end_last_seen_msg_time_stamp = msg.getCreated();
		}
		
		display.setVvalue(1.0);
		
		//System.out.println(mb.getId());
		_med_.home_controller.membership_hps.get(mb.getId()).setStyle("-fx-font-weight: normal;");
		mb.setLast_seen_message_ts(room.getLast_message_timestamp());
		mb.save();
	}
	
	public void populate_nav() throws SQLException {
		
		ArrayList<Membership> memberships = room.getMemberships();
		Iterator<Membership> it = memberships.iterator();
		
		VBox vbox = new VBox(25);
		TextField invite_field = new TextField();
		invite_field.setPromptText("username");
		invite_field.setPrefWidth(90);
		Button invite_button = new Button();
		invite_button.setText("Invite");
		invite_button.setOnAction(ev->{
			try {
				User user = User.getByUname(invite_field.getText(), _med_.connector.conn);
				if(user != null) {
					Invitation inv = Invitation.create_invitation(_med_.current_user.getId(), user.getId(), room.getId(), _med_.connector.conn);
					if(inv == null) {
						_med_.home_controller.info_label.setText(user.getUsername() +" is already a member!");
						return;
					}
					_med_.home_controller.info_label.setText("Invitation to user '"+ user.getUsername() + "' is sent successfully!");
				}
				else {
					_med_.home_controller.info_label.setText("User with username '"+ invite_field.getText() + "' does not exist!");
				}
			} catch (SQLException e) {
				if(e.getMessage().contains("integrity")) {
					_med_.home_controller.info_label.setText("Invitation to user '"+ invite_field.getText() + "'"
							+ " has been sent already!");
				}
			}
		});
		vbox.getChildren().add(invite_field);
		vbox.getChildren().add(invite_button);
		
		while(it.hasNext()) {
			Membership mb_room = it.next();
			Hyperlink hp = new Hyperlink();
			hp.setText(mb_room.getUser().getUsername());
			hp.setOnAction(ev->{
				try {
					stop();
					_med_.home_controller.mainbody.setContent(_med_.profile_controller.getRoot(mb_room.getUser()));
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
		
		
		Button leave_room_btn = new Button();
		leave_room_btn.setText("Leave room");
		leave_room_btn.setOnAction(ev->{
			try {
				Membership.delete(mb.getId(), _med_.connector.conn);
				stop();
				_med_.home_controller.start(_med_.window);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		vbox.getChildren().add(leave_room_btn);
		
		if(mb.getRole().equals("creator")) {
			Button delete_room_btn = new Button();
			delete_room_btn.setText("Delete room");
			delete_room_btn.setOnAction(ev->{
				try {
					Room.delete(room.getId(), _med_.connector.conn);
					stop();
					_med_.home_controller.start(_med_.window);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			vbox.getChildren().add(delete_room_btn);
		}
		
		
		vbox.setPadding(new Insets(50, 20, 10, 10));
		room_nav.setContent(vbox);
	}
	
	public void post_load_init(Room room) throws SQLException 
	{
		this.room = room;
		mb = Membership.getByUserIdAndRoomId(_med_.current_user.getId(), room.getId(), _med_.connector.conn);
		if(mb == null) {
			try {
				_med_.home_controller.start(_med_.window);
				_med_.home_controller.info_label.setText("You are not a member of this room or the room does not exist.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		populate_display();
		populate_nav();
		
		room_msg_check = new Thread(this);
		room_msg_check.setName(room_msg_check.getName()+" inside room msg check");
		room_msg_check.start();
		//Platform.runLater(this);
		
		_med_.home_controller.title.setText(room.getName());
	}
	
	public Parent getRoot(Room room) throws IOException, SQLException{
		if(room == null) return null;
		
		FXMLLoader fl = new FXMLLoader();
		
		Parent root = fl.load(getClass().getResource("RoomView.fxml").openStream());
		_med_.room_view_controller = fl.getController();
		_med_.room_view_controller._med_ =_med_;
		_med_.room_view_controller.post_load_init(room);
		
		return root;
	}
	
	public void stop()
	{
		if(room_msg_check!= null && !room_msg_check.isInterrupted()) room_msg_check.interrupt();
		mb =null;
		room = null;
	}
}

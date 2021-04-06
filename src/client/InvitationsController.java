package client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.Pane;
import database_manipulator.Invitation;
import database_manipulator.Membership;

public class InvitationsController {
	public Main _med_;
	
	public InvitationsController() {
		// TODO Auto-generated constructor stub
	}
	
	InvitationsController(Main _med_){
		this._med_ = _med_;
	}
	
	public void goto_home(Event ev) throws IOException 
	{
		_med_.home_controller.mainbody.setContent(_med_.profile_controller.getRoot(_med_.current_user));
	}
	
	public void accept(Invitation inv) throws SQLException {
		Membership mb = Membership.getByUserIdAndRoomId(inv.getUser_id(), inv.getRoom_id(), _med_.connector.conn);
		if(mb != null) {
			_med_.home_controller.info_label.setText("You are alread a member of this room.");
			return;
		}
		_med_.current_user.accept_invitation(inv.getId(), true, true);
		_med_.home_controller.populate_left_nav();
		//_med_.home_controller.mainbody.setContent(post_load_init());
		_med_.home_controller.mainbody.setContent(post_load_init(_med_));
	}
	public void reject(Invitation inv) throws SQLException{
		Membership mb = Membership.getByUserIdAndRoomId(inv.getUser_id(), inv.getRoom_id(), _med_.connector.conn);
		if(mb != null) {
			Membership.delete(mb.getId(), _med_.connector.conn);
			_med_.home_controller.populate_left_nav();
		}
		_med_.current_user.accept_invitation(inv.getId(), true, false);
		//_med_.home_controller.mainbody.setContent(post_load_init());
		_med_.home_controller.mainbody.setContent(post_load_init(_med_));
	}
	
	//public Pane post_load_init() throws SQLException
	public Pane post_load_init(Main _med_) throws SQLException
	{
		this._med_ = _med_;
		
		Pane pane = new Pane();
		ArrayList<Invitation>  invs = Invitation.getByUserId(_med_.current_user.getId(), _med_.connector.conn);
		Iterator<Invitation> it = invs.iterator();
		int i = 1;
		while(it.hasNext()) {
			
			Invitation inv = it.next();
			Label lbl = new Label(
				"Invitation for room '" + inv.getRoom().getName() + "' from user '" + inv.getSender().getUsername() +
				"'. status = " + inv.getStatus()
				);
			lbl.setLayoutX(20);
			lbl.setLayoutY(i*30);
			
			Button accept_btn = new Button("Accept");
			Button reject_btn = new Button("Reject");
			Button edit_resp_btn = new Button("Edit Response");
			
			
			accept_btn.setLayoutX(420);
			accept_btn.setLayoutY(i * 30);
			
			
			reject_btn.setLayoutX(620);
			reject_btn.setLayoutY(i * 30);
			
			edit_resp_btn.setLayoutX(720);
			edit_resp_btn.setLayoutY(i * 30);
			
			accept_btn.setOnAction(ev->{
				try {
					accept(inv);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			
			reject_btn.setOnAction(ev->{
				try {
					reject(inv);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			
			edit_resp_btn.setOnAction(ev->{
				pane.getChildren().add(accept_btn);
				pane.getChildren().add(reject_btn);
			});
			
			
			pane.getChildren().add(lbl);
			
			if (inv.getStatus().equals("pending") || inv.getStatus().equals("null")) {
				pane.getChildren().add(accept_btn);
				pane.getChildren().add(reject_btn);
			}
			else {
				pane.getChildren().add(edit_resp_btn);
			}
			++i;
		}
		_med_.home_controller.mainbody.setContent(pane);
		return pane;
	}
}

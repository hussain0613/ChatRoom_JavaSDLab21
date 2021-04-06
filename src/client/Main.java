package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;

import database_manipulator.DBConnector;
import database_manipulator.User;

public class Main extends Application {
	
	public String vend, host, port, dbname, dbuname, dbpass;
	
	public DBConnector connector;
	public User current_user;
	
	public Stage window;
	
	public LoginController login_controller;
	public SignUpController signup_controller;
	public HomeController home_controller;
	
	public ProfileController profile_controller;
	public CreateRoomController create_room_controller;
	public InvitationsController invitations_controller;
	public RoomViewController room_view_controller;
	
	
	public static String read_config() throws Exception{
		/*
		 *  need to implement some kind of decryption so that other people can't read the cookies
		 */
		
		String path = "src/client/config";
		File config_file = new File(path);
		
		return read_file(config_file);
		
	}
	
	public static String read_cookie() throws Exception{
		/*
		 *  need to implement some kind of decryption so that other people can't read the cookies
		 */
		
		String path = "src/client/cookies";
		File cookies_file = new File(path);
		
		return read_file(cookies_file);
	}
	
	
	public static String read_file(File file) throws Exception{
		/*
		 *  need to implement some kind of decryption so that other people can't read the cookies
		 */
		
		// reading cookies from file
		BufferedReader br = new BufferedReader(new FileReader(file));
		String content = "";
		int temp;
		while((temp = br.read()) != -1) {
			content += (char) temp;
		}
		br.close();
		return content;
	}
	
	public static void write_cookie(String in_cookies) throws Exception{
		/*
		 *  need to implement some kind of encryption so that other people can't read the cookies
		 */
		
		String path = "src/client/cookies";
		File cookies_file = new File(path);
		if(in_cookies != null) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(cookies_file));
			int len = in_cookies.length();
			for(int i = 0; i<len; ++i) {
				bw.append(in_cookies.charAt(i));
			}
			bw.close();
		}
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		window = primaryStage;
		
		String config = read_config();
		String vars[] = config.split(",", 6);
		vend = vars[0];
		host = vars[1];
		port = vars[2];
		dbname = vars[3];
		dbuname = vars[4];
		dbpass = vars[5];
		
		
		
		Main _med_ = this;
		
		connector = new DBConnector(vend, host, port, dbname, dbuname, dbpass);
		connector.createConnection();
		login_controller = new LoginController(_med_);
		signup_controller = new SignUpController(_med_);
		home_controller = new HomeController(_med_);
		profile_controller = new ProfileController(_med_);
		create_room_controller = new CreateRoomController(_med_);
		invitations_controller = new InvitationsController(_med_);
		room_view_controller = new RoomViewController(_med_);
		
		
		String[] cookies = read_cookie().split(",", 0);
		if(cookies.length == 2) {
			current_user = User.getByUname(cookies[0], connector.conn);
			if(current_user!=null && current_user.getPassword().equals(cookies[1])) {
				home_controller.start(primaryStage);
			}
			else {
				current_user = null;
				login_controller.start(primaryStage);
			}
		}
		
		else {
			login_controller.start(primaryStage);
		}
		
		primaryStage.show();
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(ev->{
			if(home_controller.room_check != null && !home_controller.room_check.isInterrupted()) home_controller.room_check.interrupt();
			System.exit(0);
		});
		window.setTitle("ChatRoom");
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		
		launch(args);
	}
}

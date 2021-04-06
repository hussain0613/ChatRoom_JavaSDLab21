package dbInstaller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import database_manipulator.DBConnector;
import database_manipulator.User;
import database_manipulator.Room;
import database_manipulator.Message;
import database_manipulator.Membership;
import database_manipulator.Invitation;

public class Main extends Application{
	public TextField vend_f, host_f, port_f, dbname_f, dbuname_f, dbpass_f;
	public Label info_lbl;
	public String vend, host, port, dbname, dbuname, dbpass;
	
	public void create() throws ClassNotFoundException, SQLException, IOException {
		info_lbl.setText("Fetching data");
		
		//vend = vend_f.getText();
		vend = "jdbc:mysql";
		host = host_f.getText();
		port = port_f.getText();
		dbname = dbname_f.getText();
		dbuname = dbuname_f.getText();
		dbpass = dbpass_f.getText();
		
		//*
		if(vend.equals("")) vend = "jdbc:mysql";
		else System.out.println("in else got vend: " + vend);
		if(host.equals("")) host = "localhost";
		if(port.equals("")) port = "3306";
		if(dbname.equals("")) dbname = "test";
		if(dbuname.equals("")) dbuname = "root";
		if(dbpass.equals("")) dbpass = "";
		//*/
		
		info_lbl.setText("Connecting to server");
		//DBConnector connector = new DBConnector(vend, host, port, dbname, dbuname, dbpass);
		//host = "remotemysql.com"; port = "3306"; dbname = "PCArjnVzIp"; dbuname = "PCArjnVzIp"; dbpass = "ktVA5ybZWD";
		//host = "sql6.freemysqlhosting.net"; port = "3306"; dbname ="sql6402435"; dbuname="sql6402435"; dbpass="4YEE7eVgq4";
		//DBConnector connector = new DBConnector(vend, host, port, dbname, dbuname, dbpass);
		//DBConnector connector = new DBConnector(vend, "sql101.epizy.com", "3306", "epiz_28256378_chatroom_db5", "epiz_28256378", "pPskPcD5A5OZD");
		
		//host = "johnny.heliohost.org"; port = "3306"; dbname ="ibuykhai_sdlab21_chatroom_db"; dbuname="uname_placeholder"; dbpass="pass_placeholder";
		//DBConnector connector = new DBConnector(vend, "johnny.heliohost.org", port, "ibuykhai_sdlab21_chatroom_db", "uname_placeholder", "pass");
		DBConnector connector = new DBConnector(vend, host, port, dbname, dbuname, dbpass);
		
		Connection conn = connector.createConnection();
		
		info_lbl.setText("Creating tables");
		User.create_table(conn);
		Room.create_table(conn);
		Membership.create_table(conn);
		Message.create_table(conn);
		Invitation.create_table(conn);
		
		info_lbl.setText("storing configuration values");
		
		store_config();
		
		info_lbl.setText("Finished");
	}
	public void store_config() throws IOException {
		String data = String.format("%s,%s,%s,%s,%s,%s", vend, host, port, dbname, dbuname, dbpass);
		String path = "src/client/config";
		File config_file = new File(path);
		if(data != null) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(config_file));
			int len = data.length();
			for(int i = 0; i<len; ++i) {
				bw.append(data.charAt(i));
			}
			bw.close();
		}
	}
	
	public void start(Stage window) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("MainFXML.fxml"));
		Scene scene = new Scene(root);
		window.setScene(scene);
		window.setResizable(false);
		window.setOnCloseRequest(ev->{
			System.exit(0);
		});
		window.show();
	}
	
	public static void main(String args[])
	{
		launch(args);
	}
}

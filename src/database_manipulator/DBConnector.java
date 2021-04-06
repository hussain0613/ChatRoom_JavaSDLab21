package database_manipulator;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnector {
	public String vendor, host, port, dbname, user, pass;
	public Connection conn;
	public DBConnector(String vendor, String host, String port, String dbname, String user, String pass) {
		this.vendor = vendor;
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.user = user;
		this.pass = pass;
	}
	public Connection createConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		String conn_string = vendor+"://"+host+":"+port+"/"+dbname;
		System.out.println(conn_string);
		this.conn = DriverManager.getConnection(conn_string, user, pass);
		return conn;
	}
	
}

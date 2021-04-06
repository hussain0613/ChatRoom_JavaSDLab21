package database_manipulator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class User {
	public static final String __tablename__ = "user";
	private int flag; //0 means nothings changed, 1 new entry not saved yet, 2 uname changed, 4-email, 8-pass and 16-name has changed
	private int id;
	private String name, username, email, password;
	private double created; // kono timestamp type use korte hobe
	private Connection conn;
	
	public User(Connection conn) {
		flag = 1;
		id = -1;
		this.conn = conn;
	}
	
	public void setUsername(String username) {
		if(this.username!= null && this.username.equals(username)) return;
		else this.username = username;
		
		if(id<0) flag = 1;
		else flag = flag | 2;
	}
	
	public void setEmail(String email) {
		if(this.email!=null && this.email.equals(email)) return;
		else this.email = email;
		
		if(id<0) flag = 1;
		else flag = flag | 4;
	}
	
	public void setPassword(String password) {
		if(this.password!= null &&this.password.equals(password)) return;
		else this.password = password;
		
		if(id<0) flag = 1;
		else flag = flag | 8;
	}
	public void setName(String name) {
		if(this.name != null && this.name.equals(name)) return;
		else this.name = name;
		
		if(id<0) flag = 1;
		else flag = flag | 16;
	}
	
	
	public String getName() {
		return name;
	}
	public String getUsername() {
		return username;
	}
	public int getId() {
		return id;
	}
	public String getPassword() {
		return password;
	}public String getEmail() {
		return email;
	}
	public double getCreated() {
		return created;
	}
	
	public void getMessages() { //can't be void
		
	}
	public void loadRooms() throws SQLException { //can't be void
		
		
	}
	public void save() throws SQLException {
		Statement st = conn.createStatement();
		String query = "update " + __tablename__ + " set ";
		
		if(flag<=0) {
			System.out.println("Nothing changed for user(id="+id+", uname="+ username+")");
			return;
		}
		else if(flag == 1) {
			st.execute("begin;");
			query = String.format("insert into "+ __tablename__+ "(name, username, email, password) values(\"%s\", \"%s\", \"%s\", \"%s\");",
					name, username, email, password);
			st.execute(query);
			
			query = "select last_insert_id();";
			ResultSet r = st.executeQuery(query);
			r.next();
			id = Integer.parseInt(r.getString("last_insert_id()"));
			r.close();
			st.execute("commit;");
			st.close();
			flag = 0;
			return;
		}
		else {
			String q2 = null;
			if((flag&2) == 2) {
				if(q2 == null) q2 = "";
				q2 += "username=\""+username+"\" ";
			}
			if((flag&4) == 4) {
				if(q2 == null)q2 = "";
				else q2 += ",";
				q2 += "email=\""+email+"\" ";
			}
			if((flag&8) == 8) {
				if(q2 == null)q2 = "";
				else q2 += ",";
				q2 += "password=\""+password+"\" ";
			}
			if((flag&16) == 16) {
				if(q2 == null)q2 = "";
				else q2 += ",";
				q2 += "name=\""+name+"\" ";
			}
			query += q2;
			query += "where id=" + id + ";";
		}
		//System.out.println(query);
		st.execute(query);
		if(flag ==1) System.out.println("Succesfully created new user!");
		else System.out.println("Succesfully updated user!");
		st.close();
		flag = 0;
	}
	
	
	public ArrayList<Membership> getMemberships(Connection conn) throws SQLException{
		return Membership.getByUserId(id, conn);
	}
	
	public Membership accept_invitation(int id, boolean is_inv_id, boolean accept) throws SQLException {
		Invitation inv;
		if(is_inv_id) {
			inv = Invitation.getById(id, conn);
		}
		else { // room_id
			inv = Invitation.getByRoomAndUserId(id, this.id, conn);
		}
		Membership mb = null;
		if(accept) {
			inv.setStatus("accepted");
			mb = Membership.create_membership(this.id, inv.getRoom_id(), conn);
		}
		else {
			inv.setStatus("rejected");
		}
		inv.save();
		return mb;
	}
	
	public static User login(String uname, String pwd, Connection conn) throws SQLException { // may be better return some kind of token instead of
		User user = getByUname(uname, conn);
		if(user != null && pwd.equals(user.getPassword())) { // insecure way for checking password
			return user;
		}else {
			return null;
		}
	}
	
	public static User create_new_user(String name, String uname, String email, String password, Connection conn) throws SQLException {
		User user = new User(conn);
		user.name =name;
		user.username=uname;
		user.email=email;
		user.password=password;
		
		user.save();
		
		return user;
	}
	
	public static User getById(int id, Connection conn) throws SQLException {
		User user = new User(conn);
		Statement st = conn.createStatement();
		String query = "select * from " + __tablename__+ " where id="+id+";";
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			/*user.id = Integer.parseInt(r.getString("id"));
			user.name = r.getString("name");
			user.username = r.getString("username");
			user.email = r.getString("email");
			user.password = r.getString("password");
			*/
			from_rset_to_obj(r, user);
		}
		//user.flag = 0;
		r.close();
		st.close();
		
		if(user.username != null) return user;
		else return null;
	}
	public static User getByUname(String uname, Connection conn) throws SQLException {
		User user = new User(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+ __tablename__ +" where username=\""+uname+"\";";
		//System.out.println(query);
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			/*user.id = Integer.parseInt(r.getString("id"));
			user.name = r.getString("name");
			user.username = r.getString("username");
			user.email = r.getString("email");
			user.password = r.getString("password");
			user.created = Double.parseDouble(r.getString("account_created"));*/
			from_rset_to_obj(r, user);
		}
		//user.flag = 0;
		r.close();
		st.close();
		
		if(user.username != null) return user;
		else return null;
	}
	public static User getByEmail(String email, Connection conn) throws SQLException {
		User user = new User(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+__tablename__+" where email=\""+email+"\";";
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			/*
			user.id = Integer.parseInt(r.getString("id"));
			user.name = r.getString("name");
			user.username = r.getString("username");
			user.email = r.getString("email");
			user.password = r.getString("password");
			user.created = Double.parseDouble(r.getString("account_created"));
			*/
			from_rset_to_obj(r, user);
		}
		//user.flag = 0;
		r.close();
		st.close();
		
		if(user.username != null) return user;
		else return null;
	}
	
	public static boolean delete(int id, Connection conn) throws SQLException {
		String query = "delete from "+ __tablename__ +" where id="+id+";";
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
		return true;
	}
	
	
	public static void from_rset_to_obj(ResultSet r, User user) throws NumberFormatException, SQLException {
		user.id = Integer.parseInt(r.getString("id"));
		user.name = r.getString("name");
		user.username = r.getString("username");
		user.email = r.getString("email");
		user.password = r.getString("password");
		user.created = r.getTimestamp("account_created").getTime();
		user.flag = 0;
	}
	
	
	public static void create_table(Connection conn) throws SQLException {
		String query = "create table if not exists " + __tablename__ +"(";
		query += "id int not null auto_increment,";
		query += "name varchar(200),";
		query += "username varchar(200) not null,";
		query += "email varchar(200) not null,";
		query += "password varchar(200) not null,";
		//query += "account_created double default unix_timestamp(current_timestamp),";
		//query += "account_created timestamp default utc_timestamp,";
		query += "account_created timestamp default current_timestamp,";
		
		query += "primary key(id),";
		query += "unique(username),";
		query += "unique(email)";
		query += ");";
		
		//System.out.println(query);
		
		Statement stm = conn.createStatement();
		stm.execute(query);
		System.out.println("Created table user succesfully!");
		stm.close();
	}
	
}

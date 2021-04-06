package database_manipulator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Message {
	
	public static final String __tablename__ = "message";
	
	private int flag; //0 means nothings changed, 1 new entry not saved yet, 2 uname changed, 4-email, 8-pass and 16-name has changed
	private int id, user_id, room_id;
	private User user;
	private Room room;
	private String message;
	private double created; // kono timestamp type use korte hobe
	private Connection conn;
	
	public Message(Connection conn) {
		flag = 1;
		id = -1;
		this.conn = conn;
	}
	
	public Message(String msg, int uid, int rid, Connection conn) {
		flag = 1;
		id = -1;
		message = msg;
		user_id = uid;
		room_id = rid;
		this.conn = conn;
	}
	
	
	public int getId() {
		return id;
	}
	public double getCreated() {
		return created;
	}
	
	public int getRoom_id() {
		return room_id;
	}
	public int getUser_id() {
		return user_id;
	}
	
	
	public String getMessage() {
		return message;
	}
	public Room getRoom() throws SQLException {
		//if(not_loaded)
		loadRoom();
		return room;
	}
	public User getUser() throws SQLException {
		//if(not_loaded)
		loadUser();
		return user;
	}
	
	public void loadUser() throws SQLException {
		user = User.getById(user_id, conn);
	}
	public void loadRoom() throws SQLException {
		room = Room.getById(room_id, conn);
	}
	
	public void save() throws SQLException {
		Statement st = conn.createStatement();
		String query = "update "+__tablename__+" set ";
		
		if(flag<=0) {
			System.out.println("Nothing changed for membership(id="+id+", room_id="+ room_id+", user_id="+ user_id+")");
			return;
		}
		else if(flag == 1) {
			query = "begin;";
			st.execute(query);
			query = String.format("insert into "+__tablename__+"(room_id, user_id, message) values(%d, %d, \"%s\");",
					room_id, user_id, message);
			st.execute(query);
			//query = "select last_insert_id(), created from "+__tablename__+" where id = last_insert_id();";
			query = "select last_insert_id()";
			ResultSet r = st.executeQuery(query);
			r.next();
			id = Integer.parseInt(r.getString("last_insert_id()"));
			//created = Double.parseDouble(r.getString("created"));
			query = "select created from "+__tablename__+" where id = "+id +";";
			r = st.executeQuery(query);
			r.next();
			created = r.getTimestamp("created").getTime();
			r.close();
			
			//System.out.println("msg created: " + created);
			query = "commit;";
			st.execute(query);
			st.close();
			//System.out.println("msg creation done");
			flag = 0;
			return;
		}
		else {
			String q2 = null;
			if((flag&2) == 2) {
				if(q2 == null)q2 = "";
				q2 += "message=\""+message+"\" ";
			}
			query += q2;
			query += "where id=" + id + ";";
		}
		//System.out.println(query);
		st.execute(query);
		if(flag == 1) System.out.println("Succesfully created new message!");
		else System.out.println("Succesfully updated message!");
		st.close();
		flag = 0;
	}
	
	public static Message create_message(String msg, int user_id, int room_id, Connection conn) throws SQLException {
		Membership mb = Membership.getByUserIdAndRoomId(user_id, room_id, conn);
		if(mb == null) return null;
		
		Message message = new Message(msg, user_id, room_id, conn);
		message.save();
		
		Room room = message.getRoom();// may be room_view e korle more efficient hoito
		if(room.getLast_message_timestamp()< message.created) {
			room.setLast_message_timestamp(message.created);
			room.save();
		}
		
		if(mb.getLast_seen_message_ts() < message.created) {
			mb.setLast_seen_message_ts(message.created);
			mb.save();
		}
		
		/*if(mb.getLast_seen_message_ts() == room.getLast_message_timestamp()) System.out.println("1 Message.create_message func is ok");
		else System.out.println("1 Message.create_message func is not ok");
		
		if(mb.getRoom_id() == room.getId()) System.out.println("2 Message.create_message func is ok");
		else System.out.println("2 Message.create_message func is not ok");*/
		
		return message;
	}
	
	
	public static ArrayList<Message> getByUserId(int user_id, Connection conn) throws SQLException{
		ArrayList<Message> messages = new ArrayList<Message>();
		String q = "select * from "+__tablename__+" where user_id = "+user_id+";";
		ResultSet r = conn.createStatement().executeQuery(q);
		
		while(r.next()) {
			Message message = new Message(conn);
			/*message.id = Integer.parseInt(r.getString("id"));
			message.user_id = Integer.parseInt(r.getString("user_id"));
			message.room_id = Integer.parseInt(r.getString("room_id"));
			message.message = r.getString("message");
			message.flag = 0;*/
			from_rset_to_obj(r, message);
			messages.add(message);
		}
		r.close();
		return messages;
	}
	
	public static ArrayList<Message> getByRoomId(int room_id, Connection conn) throws SQLException{
		ArrayList<Message> messages = new ArrayList<Message>();
		String q = "select * from "+__tablename__+" where room_id = "+room_id+";";
		ResultSet r = conn.createStatement().executeQuery(q);
		
		while(r.next()) {
			Message message = new Message(conn);
			/*message.id = Integer.parseInt(r.getString("id"));
			message.user_id = Integer.parseInt(r.getString("user_id"));
			message.room_id = Integer.parseInt(r.getString("room_id"));
			message.message = r.getString("message");
			message.created = Double.parseDouble(r.getString("created"));
			message.flag = 0*/
			from_rset_to_obj(r, message);;
			messages.add(message);
			//System.out.println("message created ts: "+message.created);
		}
		r.close();
		return messages;
	}
	
	public static ArrayList<Message> getByRoomId(int room_id, double ts, Connection conn) throws SQLException{ // filtered with time
		ArrayList<Message> messages = new ArrayList<Message>();
		//String q = "select * from "+__tablename__+" where room_id = "+room_id+" and created > "+ ts + ";";
		String q = "select * from "+__tablename__+" where room_id = "+room_id+" and created > '"+ new Timestamp( (long) ts) + "';";
		//System.out.println("msg filtering query: "+q);
		ResultSet r = conn.createStatement().executeQuery(q);
		
		while(r.next()) {
			Message message = new Message(conn);
			/*message.id = Integer.parseInt(r.getString("id"));
			message.user_id = Integer.parseInt(r.getString("user_id"));
			message.room_id = Integer.parseInt(r.getString("room_id"));
			message.message = r.getString("message");
			message.created = Double.parseDouble(r.getString("created"));
			message.flag = 0;*/
			from_rset_to_obj(r, message);
			messages.add(message);
		}
		r.close();
		return messages;
	}
	
	
	public static Message getById(int id, Connection conn) throws SQLException {
		Message message = new Message(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+__tablename__+" where id="+id+";";
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			/*message.id = Integer.parseInt(r.getString("id"));
			message.user_id = Integer.parseInt(r.getString("user_id"));
			message.room_id = Integer.parseInt(r.getString("room_id"));
			message.message = r.getString("message");
			message.created = Double.parseDouble(r.getString("created"));*/
			from_rset_to_obj(r, message);
		}
		//message.flag = 0;
		r.close();
		st.close();
		
		if(message.id != -1) return message;
		else return null;
	}
	
	public static boolean delete(int id, Connection conn) throws SQLException {
		String query = "delete from "+__tablename__+" where id="+id+";";
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
		return true;
	}
	
	public static void from_rset_to_obj(ResultSet r, Message message) throws NumberFormatException, SQLException {
		message.id = Integer.parseInt(r.getString("id"));
		message.user_id = Integer.parseInt(r.getString("user_id"));
		message.room_id = Integer.parseInt(r.getString("room_id"));
		message.message = r.getString("message");
		message.created = r.getTimestamp("created").getTime();
		message.flag = 0;
	}
	
	public static void create_table(Connection conn) throws SQLException {
		String query = "create table if not exists "+__tablename__+"(";
		query += "id int not null auto_increment,";
		query += "user_id int,";
		query += "room_id int,";
		query += "message varchar(200),"; // boro korte hobe
		//query += "created double default unix_timestamp(current_timestamp),";
		//query += "created timestamp default utc_timestamp,";
		query += "created timestamp default current_timestamp,";
		
		query += "primary key(id),";
		query += "foreign key(user_id) references "+User.__tablename__+"(id) on delete cascade on update cascade,";
		query += "foreign key(room_id) references "+Room.__tablename__+"(id) on delete cascade on update cascade";
		query += ");";
		
		Statement stm = conn.createStatement();
		stm.execute(query);
		System.out.println("Created table message succesfully!");
		stm.close();
	}
	
}

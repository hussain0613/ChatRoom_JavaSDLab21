package database_manipulator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Room {
	public static final String __tablename__ = "room";
	
	private int flag; //0 means nothings changed, 1 new entry not saved yet, 2 uname changed, 4-email, 8-pass and 16-name has changed
	private int id, creator_id;
	private String name;
	private double created, last_message_timestamp; // kono timestamp type use korte hobe
	private Connection conn;
	
	public Room(Connection conn) {
		flag = 1;
		id = -1;
		this.conn = conn;
	}
	
	public Room(int creator_id,Connection conn) {
		flag = 1;
		id = -1;
		this.conn = conn;
		this.creator_id = creator_id;
	}
	
	public void setName(String name) {
		if(this.name != null && this.name.equals(name)) return;
		else this.name = name;
		
		if(id<0) flag = 1;
		else flag = flag | 2;
	}
	public void setLast_message_timestamp(double last_message_timestamp) {
		this.last_message_timestamp = last_message_timestamp;
		
		if(id<0) flag = 1;
		else flag = flag | 4;
	}
	
	
	public String getName() {
		return name;
	}
	public int getCreator_id() {
		return creator_id;
	}
	public double getLast_message_timestamp() {
		return last_message_timestamp;
	}
	public double getCreated() {
		return created;
	}
	public int getId() {
		return id;
	}
	
	public void save() throws SQLException {
		Statement st = conn.createStatement();
		String query = "update " + __tablename__ + " set ";
		
		if(flag<=0) {
			System.out.println("Nothing changed for room(id="+id+", room="+ name+")");
			return;
		}
		else if(flag == 1) {
			st.execute("begin;");
			query = String.format("insert into " + __tablename__ +"(name, creator_id) values(\"%s\", %d);",
					name, creator_id);
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
				if(q2 == null)q2 = "";
				q2 += "name=\""+name+"\" ";
			}
			if((flag&4) == 4) {
				if(q2 == null)q2 = "";
				//q2 += "last_message_timestamp="+last_message_timestamp+" ";
				q2 += "last_message_timestamp='"+ new Timestamp((long) last_message_timestamp)+"' ";
				//q2 += "last_message_timestamp=utc_timestamp ";
			}
			query += q2;
			query += "where id=" + id + ";";
		}
		//System.out.println("room save query: "+query);
		st.execute(query);
		//if(flag ==1) System.out.println("Succesfully created new room!");
		/*else*/ System.out.println("Succesfully updated room!");
		flag = 0;
		st.close();
	}
	
	public ArrayList<Membership> getMemberships() throws SQLException{
		return Membership.getByRoomId(id, conn);
	}
	
	public ArrayList<Message> getMessages() throws SQLException{
		return Message.getByRoomId(id, conn);
	}
	
	public ArrayList<Message> getMessages(double ts) throws SQLException{
		return Message.getByRoomId(id, ts, conn);
	}
	
	public static Room create_new_room(int creator_id, String room_name, Connection conn) throws SQLException {
		Room room = new Room(creator_id, conn);
		room.name = room_name;
		room.save();
		
		Membership mb = Membership.create_membership(creator_id, room.id, conn);
		mb.setRole("creator");
		mb.save();
		
		return room;
	}
	
	public static Room getById(int id, Connection conn) throws SQLException {
		Room room = new Room(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+ __tablename__ +" where id="+id+";";
		ResultSet r = st.executeQuery(query); 
		while(r.next()) {
			/*room.id = Integer.parseInt(r.getString("id"));
			room.creator_id = Integer.parseInt(r.getString("creator_id"));
			room.name = r.getString("name");
			room.last_message_timestamp = Double.parseDouble(r.getString("last_message_timestamp"));
			room.created = Double.parseDouble(r.getString("room_created"));*/
			from_rset_to_obj(r, room);
		}
		//room.flag = 0;
		r.close();
		st.close();
		
		if(room.name != null) return room;
		else return null;
	}
	
	public static boolean delete(int id, Connection conn) throws SQLException {
		String query = "delete from "+__tablename__+" where id="+id+";";
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
		return true;
	}
	
	public static void from_rset_to_obj(ResultSet r, Room room) throws NumberFormatException, SQLException {
		room.id = Integer.parseInt(r.getString("id"));
		room.creator_id = Integer.parseInt(r.getString("creator_id"));
		room.name = r.getString("name");
		//room.last_message_timestamp = Double.parseDouble(r.getString("last_message_timestamp"));
		room.last_message_timestamp = r.getTimestamp("last_message_timestamp").getTime();
		//room.created = Double.parseDouble(r.getString("room_created"));
		room.created = r.getTimestamp("room_created").getTime();
		room.flag = 0;
	}
	
	
	public static void create_table(Connection conn) throws SQLException {
		String query = "create table if not exists "+__tablename__+"(";
		query += "id int not null auto_increment,";
		query += "name varchar(200),";
		query += "creator_id int,";
		//query += "last_message_timestamp double default unix_timestamp(current_timestamp),";
		query += "last_message_timestamp timestamp default utc_timestamp,";
		//query += "last_message_timestamp timestamp default '1970-01-01 00:00:01',";
		//query += "room_created double default unix_timestamp(current_timestamp),";
		//query += "room_created timestamp default utc_timestamp,";
		query += "room_created timestamp default current_timestamp,";
		
		query += "primary key(id),";
		query += "foreign key(creator_id) references "+User.__tablename__+"(id) on delete cascade on update cascade";
		query += ");";
		
		Statement stm = conn.createStatement();
		stm.execute(query);
		System.out.println("Created table room succesfully!");
		stm.close();
	}
	
}

package database_manipulator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Membership {
	
	public static final String __tablename__ = "membership";
	
	private int flag; //0 means nothings changed, 1 new entry not saved yet, 2 uname changed, 4-email, 8-pass and 16-name has changed
	private int id, user_id, room_id;
	private User user; // member
	private Room room;
	private String role;
	private double created, last_seen_message_ts; // kono timestamp type use korte hobe
	private Connection conn;
	
	public Membership(Connection conn) {
		flag = 1;
		id = -1;
		this.conn = conn;
	}
	
	public Membership(int uid, int rid, Connection conn) {
		flag = 1;
		id = -1;
		user_id = uid;
		room_id = rid;
		this.conn = conn;
	}
	
	public void setRole(String role) {
		if(this.role != null && this.role.equals(role)) return;
		else this.role = role;
		
		if(id<0) flag = 1;
		else flag = flag | 2;
	}
	public void setLast_seen_message_ts(double last_seen_message_ts) {
		if(this.last_seen_message_ts == last_seen_message_ts) return;
		this.last_seen_message_ts = last_seen_message_ts;
		if(id<0) flag = 1;
		else flag = flag | 4;
	}
	
	
	public int getId() {
		return id;
	}
	
	public int getRoom_id() {
		return room_id;
	}
	public int getUser_id() {
		return user_id;
	}
	public double getCreated() {
		return created;
	}
	public double getLast_seen_message_ts() {
		return last_seen_message_ts;
	}
	
	public String getRole() {
		return role;
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
	
	public void update_obj() throws SQLException
	{
		Membership mb = getById(id, conn);
		this.last_seen_message_ts = mb.last_seen_message_ts;
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
			query = String.format("insert into "+__tablename__+"(room_id, user_id, role) values(%d, %d, \"%s\");",
					room_id, user_id, role);
			st.execute(query);
			query = "select last_insert_id();";
			ResultSet r = st.executeQuery(query);
			r.next();
			id = Integer.parseInt(r.getString("last_insert_id()"));
			r.close();
			query = "commit;";
			st.execute(query);
			st.close();
			flag = 0;
			return;
		}
		else {
			String q2 = null;
			if((flag&2) == 2) {
				if(q2 == null)q2 = "";
				q2 += "role=\""+role+"\" ";
			}
			if((flag&4) == 4) {
				if(q2 == null)q2 = "";
				//q2 += "last_seen_message_timestamp=\""+last_seen_message_ts+"\" ";
				q2 += "last_seen_message_timestamp='"+ new Timestamp((long) last_seen_message_ts)+"' ";
				//q2 += "last_seen_message_timestamp= utc_timestamp ";
			}
			query += q2;
			query += "where id=" + id + ";";
		}
		//System.out.println(query);
		st.execute(query);
		if(flag ==1) System.out.println("Succesfully added member to room!");
		else System.out.println("Succesfully updated membership!");
		st.close();
		flag = 0;
	}
	
	public static ArrayList<Membership> getByUserId(int user_id, Connection conn) throws SQLException{
		ArrayList<Membership> memberships = new ArrayList<Membership>();
		String q = "select * from "+__tablename__+" where user_id = "+user_id+";";
		ResultSet r = conn.createStatement().executeQuery(q);
		
		while(r.next()) {
			Membership membership = new Membership(conn);
			from_rset_to_obj(r, membership);
			memberships.add(membership);
		}
		r.close();
		return memberships;
	}
	
	public static ArrayList<Membership> getByRoomId(int room_id, Connection conn) throws SQLException{
		ArrayList<Membership> memberships = new ArrayList<Membership>();
		String q = "select * from "+__tablename__+" where room_id = "+room_id+";";
		ResultSet r = conn.createStatement().executeQuery(q);
		while(r.next()) {
			Membership membership = new Membership(conn);
			from_rset_to_obj(r, membership);
			memberships.add(membership);
		}
		r.close();
		return memberships;
	}
	
	public static Membership getById(int id, Connection conn) throws SQLException {
		Membership membership = new Membership(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+__tablename__+" where id="+id+";";
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			from_rset_to_obj(r, membership);
		}
		r.close();
		st.close();
		
		if(membership.id != -1) return membership;
		else return null;
	}
	
	public static Membership getByUserIdAndRoomId(int user_id, int room_id, Connection conn) throws SQLException {
		Membership membership = new Membership(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+__tablename__+" where user_id="+user_id+" and room_id = " + room_id + ";";
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			from_rset_to_obj(r, membership);
		}
		r.close();
		st.close();
		
		if(membership.id != -1) return membership;
		else return null;
	}
	
	
	public static Membership create_membership(int user_id, int room_id, Connection conn) throws SQLException {
		Membership membership = new Membership(user_id, room_id, conn);
		if(membership.getRoom().getCreator_id() == user_id) membership.role = "creator";
		membership.save();
		return membership;
	}
	
	
	public static boolean delete(int id, Connection conn) throws SQLException {
		String query = "delete from "+__tablename__+" where id="+id+";";
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
		return true;
	}
	
	public static void from_rset_to_obj(ResultSet r, Membership mb) throws NumberFormatException, SQLException {
		mb.id = Integer.parseInt(r.getString("id"));
		mb.user_id = Integer.parseInt(r.getString("user_id"));
		mb.room_id = Integer.parseInt(r.getString("room_id"));
		mb.role = r.getString("role");
		//mb.last_seen_message_ts = Double.parseDouble(r.getString("last_seen_message_timestamp"));
		mb.last_seen_message_ts = r.getTimestamp("last_seen_message_timestamp").getTime();
		//mb.created = Double.parseDouble(r.getString("membership_created"));
		mb.created = r.getTimestamp("membership_created").getTime();
		mb.flag = 0;
	}
	
	public static void create_table(Connection conn) throws SQLException {
		String query = "create table if not exists "+__tablename__+"(";
		query += "id int not null auto_increment,";
		query += "user_id int,";
		query += "room_id int,";
		query += "role varchar(200) default \"member\",";
		//query += "last_seen_message_timestamp double default unix_timestamp(current_timestamp),";
		query += "last_seen_message_timestamp timestamp default utc_timestamp,";
		//query += "last_seen_message_timestamp timestamp default '1970-01-01 00:00:01',";
		//query += "membership_created double default unix_timestamp(current_timestamp),";
		//query += "membership_created timestamp default utc_timestamp,";
		query += "membership_created timestamp default current_timestamp,";
		
		
		query += "primary key(id),";
		query += "foreign key(user_id) references "+User.__tablename__+"(id) on delete cascade on update cascade,";
		query += "foreign key(room_id) references "+Room.__tablename__+"(id) on delete cascade on update cascade,";
		query += "constraint UC_"+__tablename__+" unique(user_id, room_id)";
		query += ");";
		
		Statement stm = conn.createStatement();
		stm.execute(query);
		System.out.println("Created table membership succesfully!");
		stm.close();
	}
	
}

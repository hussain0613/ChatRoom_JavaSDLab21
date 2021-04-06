package database_manipulator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Invitation {
	public static final String __tablename__ = "invitation";
	
	
	private int flag; //0 means nothings changed, 1 new entry not saved yet, 2 uname changed, 4-email, 8-pass and 16-name has changed
	private int id, sender_id, user_id, room_id;
	private User user; // member
	//private User sender;
	private Room room;
	private String status;
	private double created, last_seen_message_ts; // kono timestamp type use korte hobe
	private Connection conn;
	
	public Invitation(Connection conn) {
		flag = 1;
		id = -1;
		this.conn = conn;
	}
	
	public Invitation(int sid, int uid, int rid, Connection conn) {
		flag = 1;
		id = -1;
		sender_id = sid;
		user_id = uid;
		room_id = rid;
		this.conn = conn;
	}
	
	public void setStatus(String status) {
		if(this.status != null && this.status.equals(status)) return;
		else this.status = status;
		
		if(id<0) flag = 1;
		else flag = flag | 2;
	}
	public void setLast_seen_message_ts(double last_seen_message_ts) {
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
	
	public String getStatus() {
		return status;
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
	
	public User getSender() throws SQLException {
		return User.getById(sender_id, conn);
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
			System.out.println("Nothing changed for invitation(id="+id+", room_id="+ room_id+", user_id="+ user_id+")");
			return;
		}
		else if(flag == 1) {
			query = "begin;";
			st.execute(query);
			query = String.format("insert into "+__tablename__+"(room_id, user_id, sender_id, status) values(%d, %d, %d, \"%s\");",
					room_id, user_id, sender_id, status);
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
				q2 += "status=\""+status+"\" ";
			}
			query += q2;
			query += "where id=" + id + ";";
		}
		//System.out.println(query);
		st.execute(query);
		if(flag ==1) System.out.println("Succesfully added new invitation!");
		else System.out.println("Succesfully updated invitation!");
		st.close();
		flag = 0;
	}
	
	public static ArrayList<Invitation> getByUserId(int user_id, Connection conn) throws SQLException{
		ArrayList<Invitation> invitations = new ArrayList<Invitation>();
		String q = "select * from "+__tablename__+" where user_id = "+user_id+";";
		ResultSet r = conn.createStatement().executeQuery(q);
		
		while(r.next()) {
			Invitation invitation = new Invitation(conn);
			/*invitation.id = Integer.parseInt(r.getString("id"));
			invitation.sender_id = Integer.parseInt(r.getString("sender_id"));
			invitation.user_id = Integer.parseInt(r.getString("user_id"));
			invitation.room_id = Integer.parseInt(r.getString("room_id"));
			invitation.status = r.getString("status");
			invitation.created = Double.parseDouble(r.getString("created"));
			invitation.flag = 0;*/
			from_rset_to_obj(r, invitation);
			invitations.add(invitation);
		}
		r.close();
		return invitations;
	}
	
	public static ArrayList<Invitation> getByRoomId(int room_id, Connection conn) throws SQLException{
		ArrayList<Invitation> invitations = new ArrayList<Invitation>();
		String q = "select * from "+__tablename__+" where room_id = "+room_id+";";
		ResultSet r = conn.createStatement().executeQuery(q);
		
		while(r.next()) {
			Invitation invitation = new Invitation(conn);
			/*invitation.id = Integer.parseInt(r.getString("id"));
			invitation.sender_id = Integer.parseInt(r.getString("sender_id"));
			invitation.user_id = Integer.parseInt(r.getString("user_id"));
			invitation.room_id = Integer.parseInt(r.getString("room_id"));
			invitation.status = r.getString("status");
			invitation.created = Double.parseDouble(r.getString("created"));
			invitation.flag = 0;*/
			from_rset_to_obj(r, invitation);
			invitations.add(invitation);
		}
		r.close();
		return invitations;
	}
	
	public static Invitation getById(int id, Connection conn) throws SQLException {
		Invitation invitation = new Invitation(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+__tablename__+" where id="+id+";";
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			/*invitation.id = Integer.parseInt(r.getString("id"));
			invitation.sender_id = Integer.parseInt(r.getString("sender_id"));
			invitation.user_id = Integer.parseInt(r.getString("user_id"));
			invitation.room_id = Integer.parseInt(r.getString("room_id"));
			invitation.status = r.getString("status");
			invitation.created = Double.parseDouble(r.getString("created"));*/
			from_rset_to_obj(r, invitation);
		}
		//invitation.flag = 0;
		r.close();
		st.close();
		
		if(invitation.id != -1) return invitation;
		else return null;
	}
	public static Invitation getByRoomAndUserId(int room_id, int user_id, Connection conn) throws SQLException {
		Invitation invitation = new Invitation(conn);
		Statement st = conn.createStatement();
		String query = "select * from "+__tablename__+" where user_id="+user_id+" and room_id = " +room_id +";";
		ResultSet r = st.executeQuery(query);
		while(r.next()) {
			/*invitation.id = Integer.parseInt(r.getString("id"));
			invitation.sender_id = Integer.parseInt(r.getString("sender_id"));
			invitation.user_id = Integer.parseInt(r.getString("user_id"));
			invitation.room_id = Integer.parseInt(r.getString("room_id"));
			invitation.status = r.getString("status");
			invitation.created = Double.parseDouble(r.getString("created"));*/
			from_rset_to_obj(r, invitation);
		}
		invitation.flag = 0;
		r.close();
		st.close();
		
		if(invitation.id != -1) return invitation;
		else return null;
	}
	
	public static Invitation create_invitation(int sender_id, int user_id, int room_id, Connection conn) throws SQLException {
		Membership mb = Membership.getByUserIdAndRoomId(user_id, room_id, conn);
		if(mb != null) return null;
		Invitation inv = new Invitation(sender_id, user_id, room_id, conn);
		inv.save();
		return inv;
	}
	
	public static boolean delete(int id, Connection conn) throws SQLException {
		String query = "delete from "+__tablename__+" where id="+id+";";
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
		return true;
	}
	
	public static void from_rset_to_obj(ResultSet r, Invitation invitation) throws SQLException {
		invitation.id = Integer.parseInt(r.getString("id"));
		invitation.sender_id = Integer.parseInt(r.getString("sender_id"));
		invitation.user_id = Integer.parseInt(r.getString("user_id"));
		invitation.room_id = Integer.parseInt(r.getString("room_id"));
		invitation.status = r.getString("status");
		//invitation.created = Double.parseDouble(r.getString("created"));
		invitation.created = r.getTimestamp("created").getTime();
		invitation.flag = 0;
	}
	
	public static void create_table(Connection conn) throws SQLException {
		String query = "create table if not exists "+__tablename__+"(";
		query += "id int not null auto_increment,";
		query += "sender_id int,";
		query += "user_id int,";
		query += "room_id int,";
		query += "status varchar(200) default \"pending\",";
		//query += "created double default unix_timestamp(current_timestamp),";
		//query += "created timestamp default utc_timestamp,";
		query += "created timestamp default current_timestamp,";
		
		
		query += "primary key(id),";
		query += "foreign key(sender_id) references "+User.__tablename__+"(id) on delete cascade on update cascade,";
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

package me.Paulomart.ItsYourTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;

public class MysqlConnector {
	
	private ItsYourTime plugin;
	private Connection conn;
	private HashMap<String, Long> timeHandler = new HashMap<String, Long>();
	
	public MysqlConnector(ItsYourTime p){
		this.plugin = p;
		try {
			intStats();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void intStats() throws SQLException{
		String url = "jdbc:mysql://"+plugin.getTimeConfig().getMysqlHost()+":"+plugin.getTimeConfig().getMysqlPort()+"/"+plugin.getTimeConfig().getMysqlDatabase();
		String user = plugin.getTimeConfig().getMysqlUser();
		String password = plugin.getTimeConfig().getMysqlPassword();
		conn = DriverManager.getConnection(url, user, password);
		runMysql("CREATE TABLE IF NOT EXISTS `"+plugin.getTimeConfig().getMysqlTablename()+"` (  `Name` varchar(255) NOT NULL,  `PlayTime` bigint(255) DEFAULT '0') ENGINE=InnoDB DEFAULT CHARSET=latin1");
		reopenconnection();
	}
	
	public void closeStats(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void leave(String player){
		player = player.toLowerCase();
		if (!timeHandler.containsKey(player)){
			return;
		}
		Long JoinTime = timeHandler.get(player);
		Long CurrTime = System.currentTimeMillis();
		
		Long PlayTime = CurrTime - JoinTime;
		
		PlayTime = PlayTime / 1000; //Ms -> Secs.
		
		PlayTime += getPlayTime(player);
		
		String SQL ="UPDATE `"+plugin.getTimeConfig().getMysqlTablename()+"` " +
					"SET `playTime` = '" + String.valueOf(PlayTime) + "' " +
					"WHERE `Name` = '" + player + "'";
		runMysql(SQL);
		
		System.out.println("grr");
		
		timeHandler.remove(player);
	}
	
	public void join(String player){
		player = player.toLowerCase();
		testPlayer(player);
		if (timeHandler.containsKey(player)){
			timeHandler.remove(player);
		}
		timeHandler.put(player, System.currentTimeMillis());
	}
	
	public String timestamp(){
		Date zeit = new Date(0);
		zeit.setTime(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		return sdf.format(zeit); 
	}
	
	public void testPlayer(String player){
		player = player.toLowerCase();
		try {
			if (!hasStats(player)){
				createStats(player);			
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createStats(String player) throws SQLException{
		player = player.toLowerCase();
		String SQL = "INSERT INTO `"+plugin.getTimeConfig().getMysqlTablename()+"` (`Name`) VALUES('"+player+"')";
		Statement stmt = conn.prepareStatement(SQL);
		stmt.executeUpdate(SQL);
	    stmt.close();
	}
		
	public Long getPlayTime(String player){
		player = player.toLowerCase();
		long playTime = 0L;
		String SQL = "SELECT * FROM `"+plugin.getTimeConfig().getMysqlTablename()+"` WHERE `Name` LIKE '" + player + "'";
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(SQL);
			while (res.next()){
				if (res.getString("Name").equals(player)){
					playTime = res.getLong("PlayTime");
				}
			}	
			res.close();
		    stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return playTime;
	}
		
	public Long getPlayTimeReal(String player){
		player = player.toLowerCase();
		long playTime = getPlayTime(player);
		
		if (timeHandler.containsKey(player)){
			playTime= playTime + ((System.currentTimeMillis() - timeHandler.get(player)) / 1000);
		}
		
		return playTime;
	}
	
	private boolean hasStats(String player) throws SQLException {
		player = player.toLowerCase();
		boolean hasStats = false;
		String SQL = "SELECT * FROM `"+plugin.getTimeConfig().getMysqlTablename()+"` WHERE `Name` LIKE '" + player + "'";
		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery(SQL);
		while (res.next()){
			if (res.getString("Name").equals(player)){
				hasStats = true;						
			}
		}	
		res.close();
	    stmt.close();
	    return hasStats;
	}
	
	public void runMysql(String SQL){
		try {
			Statement stmt = conn.prepareStatement(SQL);
			stmt.executeUpdate(SQL);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static final long sleepUntil(Calendar date){
		return (Math.max(date.getTimeInMillis() - System.currentTimeMillis(), 0));
	}
	
	public void reopenconnection(){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			public void run() {	
				String SQL = "SELECT 1 FROM "+plugin.getTimeConfig().getMysqlTablename();
				Statement stmt;
				try {
					stmt = conn.createStatement();
					ResultSet res = stmt.executeQuery(SQL);	
					res.close();
				    stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				reopenconnection();
			}
						
		}, 36000L);
	}
	
}

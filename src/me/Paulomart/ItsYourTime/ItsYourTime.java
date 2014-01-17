package me.Paulomart.ItsYourTime;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.Paulomart.GPex.GPex;
import me.Paulomart.GPex.PexGroup;

public class ItsYourTime extends JavaPlugin{

	@Getter
	private GPex gpex;
	private Logger log;
	@Getter
	private MysqlConnector mysqlConnector;
	@Getter
	private EventListener eventListener;
	@Getter
	private TimeConfig timeConfig;
	
	/*
	 * Todo
	 * 
	 * non Promote perx
	 * handle reload.
	 * 
	 * 
	 */
	
	
	public void onEnable(){
		gpex = (GPex) Bukkit.getServer().getPluginManager().getPlugin("GPex");
		if (gpex == null){
			log.warning("Could not find GPex on this server :(");
			this.setEnabled(false);
			return;
		}
		
		timeConfig = new TimeConfig(this);
		timeConfig.load();
		log = this.getLogger();
		log.info("Hooking into GPex");
		mysqlConnector = new MysqlConnector(this);
		eventListener = new EventListener(this);
	}
	
	public void onDisable(){
		mysqlConnector.closeStats();
		timeConfig.save();
	}
	
	public void scheduleMove(final String playerName){
		//TODO FIX
		Group storageGroup = findNextGroup(playerName);
		final PexGroup nextGroup = storageGroup.getGroup();
		if (nextGroup == null){
			return;
		}
		long moveTime = storageGroup.getMoveTime() - mysqlConnector.getPlayTimeReal(playerName);
		log.info(String.valueOf(mysqlConnector.getPlayTimeReal(playerName)));
		log.info(playerName +" in "+ moveTime + " to "+ nextGroup.getName());
		
		if (gpex.getPexMysql().getGroup(playerName).equals(nextGroup)){
			//Dont move if Group is same.
			return;
		}
		
		
		if (moveTime <= 0){
			gpex.getPexMysql().setGroup(playerName, nextGroup);
			scheduleMove(playerName);
			return;
		}
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (Bukkit.getServer().getPlayerExact(playerName) == null){
					return;
				}
				gpex.getPexMysql().setGroup(playerName, nextGroup);
				scheduleMove(playerName);
			}
		}, moveTime*20L);	
	}
	
	public void prt(Object obj){
		log.info(obj.toString());
	}
	
	public Group findNextGroup(String player){
		PexGroup group = null;
		long playedTime = mysqlConnector.getPlayTimeReal(player);
		
		//TODO FIX THIS SHIT HERE. DOSENT WORK AT ALL.
		long last = -1;
		for (Long time : timeConfig.getMoveTimes().keySet()){
			prt(time);
			prt(playedTime);
			prt(last);
			if (time <= playedTime){
				prt("1. True");
				//gorup is ok.
				if (time < last || last == -1){
					prt("2. True");
					last = time;
					group = timeConfig.getMoveTimes().get(time);
					prt("High: "+group.getName());
				}
			}
			prt("Curr;");
			prt((group == null) ? "null" : group.getName());
			prt("----------------------");
		}
		
		prt(group.getName());
		/*
		long currTime = 0;
		for (Long time : timeConfig.getMoveTimes().keySet()){
			if (time >= playedTime && (time >= currTime || currTime == 0)){
				
				currTime = time;
			}
		}
*/
		return new Group(group, last);
	}
	
		
	class Group{
		@Getter
		private long moveTime;
		@Getter
		private PexGroup group;
		
		public Group(PexGroup pexGroup, long moveTime){
			this.moveTime = moveTime;
			this.group = pexGroup;
		}			
	}
}

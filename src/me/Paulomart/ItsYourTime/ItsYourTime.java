package me.Paulomart.ItsYourTime;

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
		for (Player player : Bukkit.getServer().getOnlinePlayers()){
			mysqlConnector.join(player.getName());
		}
		
	}
	
	public void onDisable(){
		for (Player player : Bukkit.getServer().getOnlinePlayers()){
			mysqlConnector.leave(player.getName());
		}
		mysqlConnector.closeStats();
		timeConfig.save();
	}
	
	public void scheduleMove(final Player player){
		if (player.hasPermission("ItsYourTime.ItsYourTime")){
			return;
		}
		
		final String playerName = player.getName();
		
		Group storageGroup = findNextGroup(playerName);
		final PexGroup nextGroup = storageGroup.getGroup();

		long moveTime = storageGroup.getMoveTime() - mysqlConnector.getPlayTimeReal(playerName);
		if (moveTime <= 0){
			player.sendMessage("§a§lDu bist einen Rang aufgestiegen!");
			gpex.getPexMysql().setGroup(playerName, nextGroup);
			gpex.handlePex(player);
			player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 1);
			scheduleMove(player);
			return;
		}
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (Bukkit.getServer().getPlayerExact(playerName) == null){
					return;
				}
				player.sendMessage("§a§lDu bist einen Rang aufgestiegen!");
				gpex.getPexMysql().setGroup(playerName, nextGroup);
				gpex.handlePex(player);
				player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 1);
				scheduleMove(player);
			}
		}, moveTime*20L);	
	}
	
	public Group findNextGroup(String player){
		long playedTime = mysqlConnector.getPlayTimeReal(player);				
		Long[] sorted = new Long[timeConfig.getMoveTimes().keySet().size()];
			
		int i = 0;
		for (Long key : timeConfig.getMoveTimes().keySet()){
			sorted[i] = key;
			i++;
		}
		
		Arrays.sort(sorted);
		
		Group sGroup = null;
		
		int last = 0;
		for (int j = sorted.length-1; j > -1; j--) {
			Long time = sorted[j];
			if (time > playedTime){	
				sGroup = new Group(timeConfig.getMoveTimes().get(time), time);
			}
			last = j;
		}
		
		if (sGroup == null){
			if (last >= sorted.length){
				last = sorted.length-1;
			}
			
			long time = sorted[last+1];
			sGroup = new Group(timeConfig.getMoveTimes().get(time), time);
		}
		return sGroup;
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

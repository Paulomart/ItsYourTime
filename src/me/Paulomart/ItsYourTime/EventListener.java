package me.Paulomart.ItsYourTime;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener{

	private ItsYourTime itsYourTime;
	
	public EventListener(ItsYourTime itsYourTime) {
		this.itsYourTime = itsYourTime;
		Bukkit.getServer().getPluginManager().registerEvents(this, itsYourTime);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event){
		onPlayerLeave(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		onPlayerLeave(event.getPlayer());
	}
	
	private void onPlayerLeave(Player player){
		String playerName = player.getName();
		itsYourTime.getMysqlConnector().leave(playerName);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		String playerName = event.getPlayer().getName();
		itsYourTime.getMysqlConnector().join(playerName);
		itsYourTime.scheduleMove(event.getPlayer());
	}


}

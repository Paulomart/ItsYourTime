package me.Paulomart.ItsYourTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Getter;

import me.Paulomart.GPex.PexGroup;

/**
 * Based on the basic Config component by Paulomart.
 * @author Paulomart
 *
 */
public class TimeConfig {

	private ItsYourTime itsYourTime;	
	private FileConfiguration config;
	private File configFile;
	//======================================
	@Getter
	private HashMap<Long, PexGroup> moveTimes = new HashMap<Long, PexGroup>();
	@Getter
	private String mysqlUser = "";
	@Getter
	private String mysqlPassword = "";
	@Getter
	private String mysqlDatabase = "";
	@Getter
	private String mysqlHost = "";
	@Getter
	private String mysqlTablename = "";
	@Getter
	private int mysqlPort = 3306;

	public TimeConfig(ItsYourTime itsYourTime) {
		this.itsYourTime = itsYourTime;
		this.config = itsYourTime.getConfig();
		this.config.options().copyDefaults(true);
		configFile = new File(itsYourTime.getDataFolder().getAbsolutePath(), "config.yml");
	}

	public boolean load(){
		hardLoad(config, configFile);
		loadDefaults("config.yml", config);
		
		for (String time : config.getConfigurationSection("moveTimes").getKeys(false)){
			PexGroup group = itsYourTime.getGpex().getPexConfig().getGroups().get(config.getString("moveTimes."+time));
			if (group != null){
				moveTimes.put(Long.valueOf(time), group);
			}
		}
		
		mysqlDatabase = config.getString("mysql.database");
		mysqlHost = config.getString("mysql.host");
		mysqlPassword = config.getString("mysql.password");
		mysqlPort = config.getInt("mysql.port");
		mysqlTablename = config.getString("mysql.tablename");
		mysqlUser = config.getString("mysql.user");
		
		hardSave(config, configFile);
		return true;
	}
	
	public boolean save(){
		hardSave(config, configFile);
		return true;
	}
	
	/**
	 * Saves a Config to the local disk.
	 * @param config Config that will be saved
	 * @param file File in that the Config will be Saved
	 */
	public void hardSave(FileConfiguration config, File file){
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a Config form the local disk.
	 * @param config Config that will be loaded
	 * @param file File out that the Config will be loaded
	 */
	public void hardLoad(FileConfiguration config, File file){
		if (!file.exists()){
			return;
		}
		try {
			config.load(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads default values form a resource out of the jar in to a Config
	 * @param resource Name of the Resource that will be used
	 * @param config Config that will get the default values
	 */
	private void loadDefaults(String resource, FileConfiguration config){
		YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(itsYourTime.getResource(resource));
		config.setDefaults(defaultConfig);
	}
	
}

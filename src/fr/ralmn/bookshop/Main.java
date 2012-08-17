package fr.ralmn.bookshop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.NBTTagCompound;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

public class Main extends JavaPlugin {

	public FileConfiguration lang;
	public static Main instance;

	@Override
	public void onDisable() {
		System.out.println("BookShop : Disable");
	}

	@Override
	public void onEnable() {
		System.out.println("BookShop : Enable");
		getConfig().options().copyDefaults(true);
		saveConfig();

		String lang = getConfig().getString("lang");
		File langf = null;
		if (this.lang == null) {
			langf = new File(getDataFolder(), lang + ".yml");
		}
		try {
			if(!langf.exists()){
				langf.createNewFile();
			}
			this.lang = YamlConfiguration.loadConfiguration(langf);

			InputStream defConfigStream = this.getResource("lang.yml");
			if (defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration
						.loadConfiguration(defConfigStream);
				this.lang.setDefaults(defConfig);
				this.lang.options().copyDefaults(true);
				this.lang.save(langf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		instance = this;

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!(sender instanceof Player)) {
			return false;
		}

		Player pl = (Player) sender;
		if (label.equalsIgnoreCase("bookshop")) {

			if (args.length > 1) {

				if (args[0].startsWith("d") && args.length >= 2) {
					ThreadDownload d = new ThreadDownload(args[1], pl, this);
					d.start();
					return true;

				} else if (args[0].startsWith("u") && args.length >= 2) {
					CraftItemStack cis = (CraftItemStack) pl.getItemInHand();

					if (cis.getType() != Material.WRITTEN_BOOK && cis.getType() != Material.BOOK_AND_QUILL) {
						pl.sendMessage(ChatColor.RED
								+ lang.getString("hand.notbook"));
						pl.sendMessage(cis.getType().toString());
						return true;
					}
					NBTTagCompound tag = cis.getHandle().getTag();
					
					String us = pl.getName();
					if(args.length >= 3){
						us= args[2];
					}
					ThreadUpload u = new ThreadUpload(pl, tag, us,
							args[1]);
					u.start();
					return true;
				} else if (args[0].startsWith("l") && args.length >= 2) {
					long t1 = System.currentTimeMillis();
					String user = args[1];
					List<String> books = getBookList(user);
					long t2 = System.currentTimeMillis();

					if (books.isEmpty()) {
						pl.sendMessage(lang.getString("user.notfind"));
						return true;
					}

					String book_str = lang.getString("book.of") + " " + user + " : ";

					for (String u : books) {
						book_str += u;

						if (books.lastIndexOf(u) != (books.size() - 1))
							book_str += ", ";
					}

					pl.sendMessage(book_str);
					return true;

				}

			}

		}

		return false;
	}

	public List<String> getBookList(String user) {
		List<String> book_list = new ArrayList<String>();
		String url = "http://aperturelaboratories.eu/bookshop/api/members.php?username="
				+ user;
		long t1 = System.currentTimeMillis();
		try {
			InputStream is = (new URL(url)).openStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = "";
			String r = "";
			while ((line = br.readLine()) != null) {

				r += line;

			}

			if (r == null) {
				return book_list;
			}

			JSONObject jso = new JSONObject(r);
			Iterator<String> myIter = jso.keys();


		    List<String> keys = new ArrayList<String>();

		    while(myIter.hasNext()){
		    	keys.add(myIter.next());
		    }
		    Collections.sort(keys);
			
		    for(String key : keys){
		    	
		    	book_list.add(jso.getString(key) + " (" + key + ")");
		    	
		    }
		    
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();


		return book_list;
	}

	public String getBookTitle(String user, String id) {
		String title = "";
		String url = "http://aperturelaboratories.eu/bookshop/api/download.php?username="
				+ user + "&id=" + id;

		try {

			InputStream is = (new URL(url)).openStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String l = br.readLine();
			JSONObject json_data = new JSONObject(l);

			if (json_data.isNull("error")) {
				title = json_data.getString("title").replace("+", " ");
			} else {
				title = json_data.getString("error");
			}
			return title;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return title;
	}

	public List<String> getUserList() {
		List<String> users = new ArrayList<String>();

		String url = "http://aperturelaboratories.eu/bookshop/members/";

		InputStream is = null;
		try {
			is = (new URL(url)).openStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (is == null)
			return users;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = "";
			while ((line = br.readLine()) != null) {

				if (line.contains("[")) {
					line = line.replace("[", "");
				}
				if (line.contains("]")) {
					line = line.replace("]", "");
				}

				if (line.contains(",")) {

					for (String l : line.split(",")) {
						if (l.contains("\"")) {
							l = l.replace("\"", "");
						}
						users.add(l);

					}

				} else {
					users.add(line);
				}

			}
			br.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return users;
	}

}

package fr.ralmn.bookshop;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ThreadDownload extends Thread {

	private String id;
	private Player player;
	private Main plugin;
	public ThreadDownload(final String id, final Player pl, final Main p) {
		this.id = id;
		this.plugin = p;
		this.player = pl;
	}
	
	
	

	public void run() {
		player.sendMessage(plugin.lang.getString("loading"));
		String title = getBookTitle(id);
		List<String> pages = getBookPages(id);
		CraftItemStack is = new CraftItemStack(Material.WRITTEN_BOOK, 1);

		if (pages == null) {
			player.sendMessage(Main.instance.lang.getString("book.notfind"));
			return;
		}
		
		
		if (pages.isEmpty()) {
			player.sendMessage(Main.instance.lang.getString("book.empty"));
			return;
		}
		NBTTagCompound nbttagcompound = is.getHandle().getTag();
		
		NBTTagList pages_tag = new NBTTagList("pages");
		for (int i = 0; i < pages.size(); i++) {
			pages_tag.add(new NBTTagString((i + 1) + "", pages.get(i)));
		}
		NBTTagCompound tag = is.getHandle().getTag();
		if(tag ==null){
			tag = new NBTTagCompound();
		}
		tag.set("author", new NBTTagString("author", getBookAuthors(id)));
		tag.set("title", new NBTTagString("title", title));

		tag.set("pages", pages_tag);
		is.getHandle().setTag(tag);
		if(plugin.getConfig().getBoolean("bookhands")){
			ItemStack hand = player.getItemInHand();
			if(hand.getType() == Material.BOOK){
				player.setItemInHand(is);
				player.sendMessage(plugin.lang.getString("downloaded"));
			}else if(hand.getType() == Material.BOOK_AND_QUILL || hand.getType() == Material.WRITTEN_BOOK){
				is.setType(hand.getType());
				player.setItemInHand(is);
				player.sendMessage(plugin.lang.getString("downloaded"));
			}else{
				player.sendMessage(plugin.lang.getString("hand.notbook"));
			}
			
		}else{
		player.getInventory().addItem(is);
		}
	}

	
	
	public List<String> getBookPages(String id) {
		List<String> pages = new ArrayList<String>();
		id = id.trim();
		String url = "http://aperturelaboratories.eu/bookshop/api/download.php?id=" + id;
		try {

			InputStream is = (new URL(url)).openStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String l = br.readLine();

			if (l == null) {
				return null;
			}

			JSONObject json_data = new JSONObject(l);
			if (json_data.isNull("error")) {
				try {
					JSONArray ps = json_data.getJSONArray("pages");
					for (int i = 0; i < ps.length(); i++) {

						String p = ps.getString(i);
						pages.add(p);
					}
				} catch (JSONException e) {
				}

			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return pages;
	}

	public String getBookTitle( String id) {
		String title = "";
		String url = "http://aperturelaboratories.eu/bookshop/api/download.php?&id=" + id;

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
	
	public String getBookAuthors( String id) {
		String title = "";
		String url = "http://aperturelaboratories.eu/bookshop/api/download.php?&id=" + id;

		try {

			InputStream is = (new URL(url)).openStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String l = br.readLine();
			JSONObject json_data = new JSONObject(l);

			if (json_data.isNull("error")) {
				title = json_data.getString("author").replace("+", " ");
			} else {
				title = json_data.getString("error");
			}
			return title;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return title;
	}

}

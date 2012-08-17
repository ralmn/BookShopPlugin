package fr.ralmn.bookshop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

public class ThreadUpload extends Thread {

	private NBTTagCompound nbttagcompound;
	private String user, token;
	private Player player;

	public ThreadUpload(Player pl, NBTTagCompound tag, String user, String token) {
		this.player = pl;
		this.nbttagcompound = tag;
		this.user = user;
		this.token = token;
	}

	public void run() {
		player.sendMessage(Main.instance.lang.getString("loading"));
		NBTTagList tag_pages = nbttagcompound.getList("pages");
		int page_number = 0;
		if (tag_pages != null) {
			tag_pages = (NBTTagList) tag_pages.clone();
			page_number = tag_pages.size();

			if (page_number < 1) {
				page_number = 0;
			}

		}

		// {"title":"Livre+de+test","date":"27\/6\/2012","pages":["Bonjour\n2
		// lignes","Page 2"]}
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < tag_pages.size(); i++) {
			NBTBase page = tag_pages.get(i);
			list.add(page.toString());
		}
		String username = "";
		String title = nbttagcompound.getString("title");
		if (player.getItemInHand().getType() == Material.BOOK_AND_QUILL)
			username = player.getName();
		else
			username = nbttagcompound.getString("author");

		try {
			JSONObject jso = new JSONObject();
			jso.put("title", title);
			jso.put("author", username);
			jso.put("pages", list);

			String toEphys = jso.toString();
			toEphys = URLEncoder.encode(toEphys, "UTF-8");

			String url = "http://aperturelaboratories.eu/bookshop/api/upload.php?username="
					+ username + "&token=" + token + "&data=" + toEphys;

			InputStream is = (new URL(url)).openStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String retour = br.readLine();
			JSONObject json_data = new JSONObject(retour);

			String messageR;
			if (!json_data.isNull("success")) {
				player.sendMessage(ChatColor.AQUA
						+ Main.instance.lang.getString("uploaded")
						+ ChatColor.YELLOW + " id : "
						+ json_data.getString("success"));
			} else {

				messageR = json_data.getString("error");
				player.sendMessage(ChatColor.RED + "Error " + messageR);

			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

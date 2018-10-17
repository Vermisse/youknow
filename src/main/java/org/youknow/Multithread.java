package org.youknow;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Multithread implements Runnable {

	/**
	 * 文件存储路径
	 */
	private String basePath = "E:/cache/";
	
	public String url;
	
	public static void main(String[] args) {
		String[] arr = {
			"http://7y8k.com/?m=vod-type-id-5.html",
			"http://7y8k.com/?m=vod-type-id-6.html",
			"http://7y8k.com/?m=vod-type-id-7.html",
			"http://7y8k.com/?m=vod-type-id-8.html",
		};
		
		for(String url : arr) {
			// 多线程同时下载
			Multithread t = new Multithread();
			t.url = url;
			new Thread(t).start();
		}
	}

	public void run() {
		List<String> list = Arrays.asList(new String[] { url });
		List<String> cache;

		o: while (true) {
			cache = new ArrayList<String>();
			for (String page : list) {
				try {
					Document doc = Jsoup.connect(page).get();
					Elements a = doc.select(".l h2 a");
				
					Iterator<Element> tag = a.iterator();
					while (tag.hasNext()) {
						Element lk = tag.next();
						String url = lk.attr("abs:href");
						
						System.out.println(url);
						
						level2(url);
					}
					
					Elements next = doc.select(".page .pagelink_a");
					Iterator<Element> it = next.iterator();

					boolean off = false;
					
					while (it.hasNext()) {
						Element link = it.next();
						String text = link.text();
						if (text.equals("下一页")) {
							System.out.println(link.attr("abs:href"));
							cache.add(link.attr("abs:href"));
						}
					}

					if(!off)
						break o;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			list = cache;
		}
	}
	
	public void level2(String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		Elements a = doc.select("#vlink_1 ul li a");
		System.out.println(a.attr("abs:href"));
		
		doc = Jsoup.connect(a.attr("abs:href")).get();
		String html = doc.toString();
		String name = substr(html, "mac_name='", "',mac_from");
		String source = substr(html, "unescape('", "'); <");
		
		int index = name.indexOf("-");
		if(index != -1 && index < 4)
			name = name.substring(index + 1);
			
		System.out.println(name);
		System.out.println(URLDecoder.decode(source, "UTF-8"));
		
		downVideo(basePath + name, URLDecoder.decode(source, "UTF-8"));
	}
	
	private String substr(String html, String start, String end) {
		return html.substring(html.indexOf(start) + 10, html.indexOf(end));
	}

	public void downVideo(String filePath, String videoUrl) throws Exception {
		String beforeUrl = videoUrl.substring(0, videoUrl.lastIndexOf("/") + 1);
		String fileName = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
		String newFileName = URLEncoder.encode(fileName, "UTF-8");
		newFileName = newFileName.replaceAll("\\+", "\\%20");
		
		// 编码之后的url
		videoUrl = beforeUrl + newFileName;
		
		InputStream is = null;
		FileOutputStream out = null;
		
		try {
			// 创建文件目录
			File files = new File(basePath);
			if (!files.exists()) {
				files.mkdirs();
			}
			// 获取下载地址
			URL url = new URL(videoUrl);
			// 链接网络地址
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 获取链接的输出流
			is = connection.getInputStream();
			// 创建文件，fileName为编码之前的文件名
			File file = new File(filePath + fileName);
			if(file.exists())
				return;
			
			file = new File(filePath + fileName + ".tmp");
			if(file.exists())
				return;
			
			// 根据输入流写入文件
			out = new FileOutputStream(file);
			int i = 0;
			while ((i = is.read()) != -1) {
				out.write(i);
			}
			out.close();
			
			file.renameTo(new File(filePath + fileName));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
	}
}
package org.alienchain.explorer.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import java.util.Random;

public class WebAppConfig {

	private Config config;
	
	private int generatedNodeId; 
	
	@Inject
	public WebAppConfig(Config config) {
		this.config = config;
		Random r = new Random();
		this.generatedNodeId = r.nextInt(1000000-1000)+1000;
	}
	
	public String getBaseUrl() {
		return this.config.getString("application.baseUrl");
	}
	
	public int getNodeId() {
		int id = this.config.getInt("node.id");
		return id==-1 ? this.generatedNodeId : id;
	}
	
	public StatusConfig getStatus(){
		return new StatusConfig(this.config.getConfig("status"));
	}
	
	public SearchEngineConfig getSearchEngine() {
		return new SearchEngineConfig(this.config.getConfig("searchengine"));
	}
	
	public ApiConfig getApiConfig() {
		return new ApiConfig(this.config.getConfig("api"));
	}

	
	public static class StatusConfig {
		
		private Config config;

		public StatusConfig(Config config) {
			this.config = config;
		}
		
		public int getOkBlocksLate() {
			return this.config.getInt("ok");
		}

		public int getKoBlocksLate() {
			return this.config.getInt("ko");
		}
	}
	
	public static class SearchEngineConfig {
		
		private Config config;

		public SearchEngineConfig(Config config) {
			this.config = config;
		}
		
		public boolean isEnabled() {
			return this.config.getBoolean("enabled");
		}
		
		public String getEndpoint() {
			return this.config.getString("endpoint");
		}
	}
	
	public static class ApiConfig{
		
		private Config config;
		
		public ApiConfig(Config config) {
			this.config = config;
		}
		
		public String getEndpoint() {
			return this.config.getString("endpoint");
		}
		
	}
	
}

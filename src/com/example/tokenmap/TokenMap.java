package com.example.tokenmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.TokenRange;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;

public class TokenMap {
		
		private Cluster cluster;
		private Session session;

		
		
		public void connect(String node, String username, String password) {
			this.cluster = Cluster.builder()
					.addContactPoint(node)
					.withLoadBalancingPolicy(
	                DCAwareRoundRobinPolicy.builder()
	                 //       .withLocalDc(localdc)
	                        .withUsedHostsPerRemoteDc(0)
	                        .build())
					.withCredentials(username, password)
					.build();
			session = cluster.connect();
			System.out.printf("Connected to cluster: %s%n", cluster.getMetadata().getClusterName());
	       
	    }
		
		public Session getSession() {
			return this.session;
		}
		
		public void close() {
	        cluster.close();
	    }
		

		
		public JSONObject getTokenMapForAllEndPoints(String keyspace) {
			
			JSONObject jo = new JSONObject();
			Metadata metadata = cluster.getMetadata();
		
			for (Host host : metadata.getAllHosts()) {
				Set<TokenRange> ranges = metadata.getTokenRanges(keyspace, host);
				
				JSONArray ja = new JSONArray();
				for (TokenRange range : ranges) {
					ja.put(range);
				}
			
				jo.put(host.getAddress().getHostAddress(), ja);
		
			}
			
			
			
			return jo;
		}
		
		public JSONObject getTokenMapForEndPoint(String host, String keyspace) {
			
			JSONObject jo = this.getTokenMapForAllEndPoints(keyspace);
		    JSONObject result = new JSONObject();
	
			
			for (String key : jo.keySet()) {
				
				if (key.equals(host)) {
					result.put(key,  jo.get(key));	
				}
			}
			return result;
		}
		
		
		public JSONObject getTokenMap() {
			
			Metadata metadata = cluster.getMetadata();
			JSONObject jo = new JSONObject();
			JSONArray ja = new JSONArray();
			for (TokenRange range : metadata.getTokenRanges()) {
				ja.put(range);
			}
			jo.put("all token maps", ja);
			
			return jo;
		}
		
		public JSONObject getTokenMapWithSplit(String host, String keyspace, int split) {
			
			JSONObject jo = new JSONObject();
			Metadata metadata = cluster.getMetadata();
		
			for (Host current : metadata.getAllHosts()) {
				String chost = current.getAddress().getHostAddress();
				
				if ((host != null) && !(chost.equals(host))) continue;
					
				Set<TokenRange> ranges = metadata.getTokenRanges(keyspace, current);
				
				JSONArray ja = new JSONArray();
				for (TokenRange range : ranges) {
					for (TokenRange splited_range : range.splitEvenly(split)) {
						ja.put(splited_range);
					}
				}
				jo.put(chost, ja);
			}
			return jo;
		}
		

		public JSONObject getPrimaryTokenRangesByEndPoint(String host, String keyspace, String table) {
			JSONObject jo = new JSONObject();
			List<String> ranges = new ArrayList<String>();
			ResultSet rs = session.execute("SELECT range_start, range_end FROM system.size_estimates WHERE keyspace_name = ? and table_name = ?", keyspace, table);
			
			for (Row row: rs) {
				String range = "]" + row.getString("range_start") + ", " + row.getString("range_end") + "]";
				ranges.add(range);
			}
			jo.put(host,  ranges);	
			
			return jo;
		}
		
		

		public static void main(String[] args)
				throws IOException, IllegalStateException {
		
			String akey;
	        if (args.length == 0) {
	            System.err.println("No arguments specified");
	            System.exit(-1);
	        }
	        if (0 != args.length % 2) {
	            System.err.println("Not an even number of parameters");
	            System.exit(-1);
	        }

	        Map<String, String> argMap = new HashMap<String,String>();
	        
	        for (int i = 0; i < args.length; i+=2)
	            argMap.put(args[i], args[i+1]);
	        
	        final String host = argMap.getOrDefault("-host", "127.0.0.1");
	        final String username = argMap.getOrDefault("-u", "cassandra");
	        final String password = argMap.getOrDefault("-p", "cassandra");
	        final String keyspace = argMap.get("-ks");
	        final String table = argMap.get("-tbl");
	        final Boolean isAll;
	        final int split;
	        final Boolean pr;
	        
	        if ((akey = argMap.get("-s")) != null) {
	        	split = Integer.parseInt(akey); 
	        } else split = 0;
	        		 
	        if ((akey = argMap.get("-pr")) != null) {
	        	pr = Boolean.parseBoolean(akey);
	        } else pr = false;
	      
	        if ((akey = argMap.get("-a")) != null) {
	        	isAll = Boolean.parseBoolean(akey);
	        } else isAll = false;
	        
		    TokenMap client = new TokenMap();
			try {
				client.connect(host, username, password);
				JSONObject rs = null;
				if (pr) {
					if (table == null || keyspace ==null) {
						System.out.println("Keyspace and Table are required for -pr option. Please provide the keyspace and table name ");
					} else {
						System.out.println("Getting the primary token ranges end point " + host + "...");
						rs = client.getPrimaryTokenRangesByEndPoint(host, keyspace, table);
					}
				} else if (keyspace != null) {	
					if (isAll) {
						if (split == 0) {
							System.out.println("Getting token range map for all end points without split");
							rs = client.getTokenMapForAllEndPoints(keyspace);
						} else {
							System.out.println("Getting token range map for all end points with split " + split);
							rs = client.getTokenMapWithSplit(null, keyspace, split);
						}
					} else  {
						if (split == 0) {
							System.out.println("Getting token range map for end point " + host + " without split...");
							rs = client.getTokenMapForEndPoint(host, keyspace);
						} else {
							System.out.println("Getting token range map for end point " + host + " with split " + split);
							rs = client.getTokenMapWithSplit(host, keyspace, split);
						}
					}
				} else {
					System.out.println("Getting all token range map for the cluster");
					rs = client.getTokenMap();	
				}
				
				if (rs != null) System.out.println(rs.toString(4)); //org.json built-in methods to pretty-print the data with specified indentation.
			
			} finally {
				client.close();	
			}
			
		}
			

}




/*		Created By: 	MICHAEL SWEENEY
 * 		Title:			Artist Discovery
 * 		Date:			2/26/2022	
 * 		Revision		1.0.0
 * 		
 * 		Notes:			Initial proof-of-concept for SWENG861
 *  					Planned updates to create further functionality, added GUI, etc
 *  					All code needs to be refactored, reduce size of main file and utilize supportive files
 *  					Need to add verification API server interface, implement other good standards in the future
 */

package edu.psgv.sweng861;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.Integer;

//import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.JSONException;
//import org.json.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class CustomObject {
	
	private int popularity;
	
	public CustomObject(int property) {
        this.popularity = property;
    }
  
    public int getCustomProperty() {
        return this.popularity;
    }
}

//Class for All of Artist's Releases - each index is to be added when parsing getArtistReleases
class ArtistAlbum {
	private int index;
	private String albumArtist;
	private String albumName;
	private String releaseDate;

	//setter function for the elements
	public ArtistAlbum(int indexID, String albumArtistID, String albumNameID, String releaseDateID) {
		this.index = indexID;
		this.albumArtist = albumArtistID;
		this.albumName = albumNameID;
		this.releaseDate = releaseDateID;
	}
	
	//Getters for the following elements
	public int getIndex() {
		return index;
	}
	
	public String getAlbumArtist() {
		return albumArtist;
	}
	
	public String getAlbumName() {
		return albumName;
	}
	
	public String getReleaseDate() {
		return releaseDate;
	}
	
	//Comparator to sort each release in chronological order
	public static Comparator<ArtistAlbum> AlbumReleaseComparator = new Comparator<ArtistAlbum>() {
		public int compare(ArtistAlbum a1, ArtistAlbum a2) {
			String itemName1 = a1.getReleaseDate().toUpperCase();
			String itemName2 = a2.getReleaseDate().toUpperCase();
			
			return itemName1.compareTo(itemName2);
	
		}
	};

}

//Class for All of Artist's Popular Tracks - each index is to be added when parsing getArtistTracks
class ArtistTrack {
	private int index;
	private String trackArtist;
	private String trackName;
	private String trackAlbum;
	private long trackPop;

	//setter function for the elements
	public ArtistTrack(int indexID, String trackArtistID, String trackNameID, String trackAlbumID, long trackPopID) {
		this.index = indexID;
		this.trackArtist = trackArtistID;
		this.trackName = trackNameID;
		this.trackAlbum = trackAlbumID;
		this.trackPop = trackPopID;
	}
	
	//getter functions for each element property
	public int getIndex() {
		return index;
	}
	
	public String getTrackArtist() {
		return trackArtist;
	}
	
	public String getTrackName() {
		return trackName;
	}
	
	public String getTrackAlbum() {
		return trackAlbum;
	}
	
	public long getTrackPop() {
		return trackPop;
	}

}

//Class for All of Artist's standard information returned on search to spotify - each index is to be added when adding any artist to a list
class ItemStruct {
	
	private int index;
	private String key;
	private String name;
	private long popularity;
	private ArrayList<String> genre;
	
	//Method to classify each genre returned for when sorting my genre is necessary 
	//main purpose is to aggregate the received genre tags and look for parent-genre tags 
	public static String GenreClassify(ArrayList<String> genreInList) {

		
		int popCnt = 0;
		int rockCnt = 0;
		int jazzCnt = 0;
		int metalCnt = 0;
		int hiphopCnt = 0;
		int electronicCnt = 0;
		int classicalCnt = 0;
		int folkCnt = 0;
		int countryCnt = 0;
		int punkCnt = 0;
		int miscCnt = 0;
		
		//Best-guess of standard/popular genres, not entirely inclusive but usually one of these is tagged to an artist
		String [] genreArr = {"Misc", "Rock", "Metal", "Hip-Hop", "Jazz",
				"Country", "Folk", "Electronic", "Classical", "Pop", "Punk"};
		
		//Due to many genre tags being accompanied by various prefixes/suffixes, this function tries to return one popular keyword
		//This list is in no way inclusive as numerous genre tags exist, but should be enough to give 1 artist at least one common tag
		//otherwise, Misc is used in the event that no further tags are found
		for(int i=0; i<genreInList.size(); i++) {
			
			String genreIn = genreInList.get(i).toLowerCase();
			
			
			if(genreIn.contains("pop"))
				popCnt++;
			else if(genreIn.contains("rock"))
				rockCnt++;
			else if(genreIn.contains("metal"))
				metalCnt++;
			else if((genreIn.contains("hip")) || (genreIn.contains("hop")) || (genreIn.contains("rap")))
				hiphopCnt++;
			else if(genreIn.contains("jazz"))
				jazzCnt++;
			else if(genreIn.contains("country"))
				countryCnt++;
			else if(genreIn.contains("folk"))
				folkCnt++;
			else if((genreIn.contains("electronic")) || (genreIn.contains("edm")))
				electronicCnt++;
			else if(genreIn.contains("classical"))
				classicalCnt++;
			else if(genreIn.contains("punk"))
				punkCnt++;
			//else
				//miscCnt++;
		}
		
		
		int [] countsArr = {miscCnt, rockCnt, metalCnt, hiphopCnt, jazzCnt, 
				countryCnt, folkCnt, electronicCnt, classicalCnt, popCnt, punkCnt};
		
		int max = countsArr[0];
		int maxIndex = 0;
		
		for(int i=1; i<countsArr.length; i++) {
			if(countsArr[i] > max) {
				max = countsArr[i];
				maxIndex = i;
			}
		}

		
		return genreArr[maxIndex];
	}
	
	//setter function for artist item to interface with
	public ItemStruct(int indexID, String keyID, String nameID, long popularityID, ArrayList<String> genreID) {
		this.index = indexID;
		this.key = keyID;
		this.name = nameID;
		this.popularity = popularityID;
		this.genre = genreID;
	}
	
	//getter functions to get each index element property
	public int getIndex() {
		return index;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public long getPopularity() {
		return popularity;
	}
	
	public ArrayList<String> getGenre() {
		return genre;
	}

	//Comparator used to sort artist's names in asc order for listing
	public static Comparator<ItemStruct> ItemNameComparator = new Comparator<ItemStruct>() {
		public int compare(ItemStruct i1, ItemStruct i2) {
			String itemName1 = i1.getName().toUpperCase();
			String itemName2 = i2.getName().toUpperCase();
			
			return itemName1.compareTo(itemName2);
	}};
	
	//Comparator used to sort artist's names in desc order for listing
	public static Comparator<ItemStruct> ItemNameComparatorDesc = new Comparator<ItemStruct>() {
		public int compare(ItemStruct i1, ItemStruct i2) {
			String itemName1 = i1.getName().toUpperCase();
			String itemName2 = i2.getName().toUpperCase();
			
			return itemName2.compareTo(itemName1);
	}};
	
	//Comparator used to sort artist's aggregated genre in ascending order for listing
	public static Comparator<ItemStruct> ItemGenreOrg = new Comparator<ItemStruct>() {
		
		public int compare(ItemStruct i1, ItemStruct i2) {
			String itemGenre1 = GenreClassify(i1.getGenre());
			String itemGenre2 = GenreClassify(i2.getGenre());
			
			return itemGenre1.compareTo(itemGenre2);
	}};
	
	//Comparator used to sort artist's popularity rating in ascending order for listing
	public static Comparator<ItemStruct> ItemPopComparator = new Comparator<ItemStruct>() {
		
		public int compare(ItemStruct i1, ItemStruct i2) {
			long itemPop1 = i1.getPopularity();
			long itemPop2 = i2.getPopularity();
			
			return Long.compare(itemPop1, itemPop2);
	}};
		

}		

//Class to gather an arraylist of custom user-generated lists
class CustomList {
	
	public int listEnum;
	public String name;
	public ArrayList<ItemStruct> items;
	
	//setter to set user list properties
	public CustomList(int listEnumID, String nameID, ArrayList<ItemStruct> itemsID) {
		this.listEnum = listEnumID;
		this.name = nameID;
		this.items = itemsID;
	}
	
	//getter functions for getting customlist properties
	public String getName() {
		return name;
	}
	
	public int getListEnum() {
		return listEnum;
	}
	
	public ArrayList<ItemStruct> getItems() {
		return items;
	}
	
	
}

//Main class for application execution
public class SpotifyLister {
	
	static final String CLIENTID = "42210df04b17433a87ff547d10d309c4";
    static final String CLIENTSECRET = "5a98aa4597614024ab64abf0e0339f99";
    static final String REDIRECTURL = "http://localhost:8888/callback"; //whiltelisted set inside spotify
    static String TOKEN = "";
    public static int userListID = 0;   
    public static String rev = "1.0.0";
    
    //Create array list of custom user lists
    static ArrayList<CustomList> userLists = new ArrayList<CustomList>();
    
    
    public static String artist;
    public static String genre;
    public static int max;
    public static int offset;
    public static int searchMethod = 0;
    
    //Function to call name alphabetical asc sort comparator and list the generated output
    public static void alphaAscSort() {
	    ArrayList<ItemStruct> itemSort = new ArrayList<ItemStruct>(userLists.get(userListID).items);
		Collections.sort(itemSort, ItemStruct.ItemNameComparator);
		System.out.println("ID  Name	 		         Popularity				Genre\n");
        System.out.println("########################################################################################################");	
		
		for (int i=0; i<itemSort.size(); i++) {
	    	
	    	ItemStruct item1 = itemSort.get(i);
	    	String nameHolder = (String)item1.getName();	   	
	    	 	
	    	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
	    }
    }
    
    //Function to call name alphabetical desc sort comparator and list the generated output
    public static void alphaDescSort() {
	    ArrayList<ItemStruct> itemSort = new ArrayList<ItemStruct>(userLists.get(userListID).items);
		Collections.sort(itemSort, ItemStruct.ItemNameComparator);
		
		System.out.println("ID  Name	 		         Popularity				Genre\n");
        System.out.println("########################################################################################################");	 	
		
		for (int i=0; i<itemSort.size(); i++) {
	    	
	    	ItemStruct item1 = itemSort.get(i);
	    	String nameHolder = (String)item1.getName();    	
	    	
	    	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
	    }
    }
    
    //Function to call genre aggregate alphabetical asc sort comparator and list the generated output
    public static void genreAscSort() {
	    ArrayList<ItemStruct> itemSort = new ArrayList<ItemStruct>(userLists.get(userListID).items);
		Collections.sort(itemSort, ItemStruct.ItemGenreOrg);
		
		System.out.println("ID  Name	 		         Popularity				Genre\n");
        System.out.println("########################################################################################################");	 
		
		for (int i=0; i<itemSort.size(); i++) {
	    	
	    	ItemStruct item1 = itemSort.get(i);
	    	String nameHolder = (String)item1.getName();
	    	String genreAggr = ItemStruct.GenreClassify(item1.getGenre());
	    	System.out.format("%-4d %-40s %4d   %-12s %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), genreAggr, item1.getGenre());
	    }
    }
    
    //Function to call popularity asc sort comparator and list the generated output
    public static void popAscSort() {
	    ArrayList<ItemStruct> itemSort = new ArrayList<ItemStruct>(userLists.get(userListID).items);
		Collections.sort(itemSort, ItemStruct.ItemPopComparator);
		System.out.println("ID  Name	 		         Popularity				Genre\n");
        System.out.println("########################################################################################################");	
		
		for (int i=0; i<itemSort.size(); i++) {
	    	
	    	ItemStruct item1 = itemSort.get(i);
	    	String nameHolder = (String)item1.getName();
	    	
	    	 	
	    	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
	    }
    }
    
    
    //Function to perform collecting all of the details of passed artist, parsing results and display as output
    public static void getArtistDetails(int artistID) {
    	JSONParser parser = new JSONParser();
    	JSONObject jsonObject = new JSONObject();
    	JSONArray jsonArray = new JSONArray();
    	
    	//Try to make API request, parse returned JSON, and add to respective arraylists
    	try {
    		//Get artist releases via API, parse results and add to list
    		String artistDetailsAlbums = getArtistDetailsAlbums(userLists.get(userListID).items.get(artistID).getKey());
    		//System.out.println(artistDetailsAlbums);
    		
    		jsonObject = (JSONObject) parser.parse(artistDetailsAlbums);
    		
    		jsonArray = (JSONArray) jsonObject.get("items");
    		ArrayList<ArtistAlbum> artistAlbums = new ArrayList<ArtistAlbum>();
    		
    		for(int i=0; i<jsonArray.size(); i++) {
    			JSONObject albumJSON = (JSONObject) jsonArray.get(i);
    			ArtistAlbum album = new ArtistAlbum(i, userLists.get(userListID).items.get(artistID).getName(), 
    					(String)albumJSON.get("name"), (String)albumJSON.get("release_date"));
    			artistAlbums.add(album);
    			
    		}
    		
    		Collections.sort(artistAlbums, ArtistAlbum.AlbumReleaseComparator);
    		
    		//Get artist popular tracks, parse results and add to list
    		String artistDetailsTracks = getArtistDetailsTracks(userLists.get(userListID).items.get(artistID).getKey());
    		//System.out.println(artistDetailsTracks);
    		
    		jsonObject = (JSONObject) parser.parse(artistDetailsTracks);
    		
    		jsonArray = (JSONArray) jsonObject.get("tracks");
    		ArrayList<ArtistTrack> artistTracks = new ArrayList<ArtistTrack>();
    		
    		for(int i=0; i<jsonArray.size(); i++) {
    			JSONObject trackJSON = (JSONObject) jsonArray.get(i);
    			JSONObject trackAlbumJSON = (JSONObject) trackJSON.get("album");
    			ArtistTrack track = new ArtistTrack(i, userLists.get(userListID).items.get(artistID).getName(), 
    					(String)trackJSON.get("name"),  (String)trackAlbumJSON.get("name"), (long)trackJSON.get("popularity"));
    			artistTracks.add(track);
    			
    		}
    		
    		//Get artist related artists via API, parse results and add to list
    		String artistDetailsRelArtists = getArtistDetailsRelArtists(userLists.get(userListID).items.get(artistID).getKey());
    		//System.out.println(artistDetailsRelArtists);
    		
    		jsonObject = (JSONObject) parser.parse(artistDetailsRelArtists);
    		jsonArray = (JSONArray) jsonObject.get("artists");
    		
			ArrayList<ItemStruct> relArtists = new ArrayList <ItemStruct>();
			
			for (int i=0; i<jsonArray.size(); i++) {
				JSONObject relArtistJSON = (JSONObject) jsonArray.get(i);
				JSONArray genreArray = (JSONArray) relArtistJSON.get("genres");
				ArrayList<String> genres = new ArrayList <String>();

				for (int index=0; index<genreArray.size(); index++) {
					genres.add((String)genreArray.get(index));
				}
				
				ItemStruct relArtist = new ItemStruct(i, (String)relArtistJSON.get("id"), (String)relArtistJSON.get("name"), 
										(long)relArtistJSON.get("popularity"), genres);
				relArtists.add(relArtist);
			}
			
			//Display the pulled data in the form of lists
			System.out.println("\nRELEASES");
    		System.out.println("ID  Release Name						Release Data");
            System.out.println("########################################################################################################");	 
    		
    		for(int i=0; i<artistAlbums.size(); i++) {
    			System.out.format("%-3d %-60s %-20s\n", (int)i, artistAlbums.get(i).getAlbumName(), artistAlbums.get(i).getReleaseDate());
    		}
    		System.out.println("\nPOPULAR TRACKS");
    		System.out.println("ID  Track Name						Album Name				Popularity");
            System.out.println("########################################################################################################");	 
    		
    		for(int i=0; i<artistTracks.size(); i++) {
    			System.out.format("%-3d %-50s %-40s %-4d\n", (int)i, artistTracks.get(i).getTrackName(), artistTracks.get(i).getTrackAlbum(),
    					artistTracks.get(i).getTrackPop());
    		}
    	
    		System.out.println("\nRELATED ARTISTS");
    		System.out.println("ID  Name	 		        		Popularity	Genre");
            System.out.println("########################################################################################################");	  
            
            for(int i=0; i<relArtists.size(); i++) {
            	System.out.format("%-3d %-50s %4d %-20s\n", (int)i, relArtists.get(i).getName(), (long)relArtists.get(i).getPopularity(),
            			relArtists.get(i).getGenre());
            }
            
            System.out.println("");
    		
		} catch (ParseException e) {
			e.printStackTrace();
		}  	    	
    }
    
    
    //Main function to run in a while loop unless 'exit' is entered
	public static void main(String[] args) {
		 
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Hello! Welcome to the Artist Discovery Application!");
        System.out.println("Version: " + rev + "\n");
        int exitState = 0;
        
        //Start the loop
        while(exitState == 0) {
        	
        	//Start the main menu that only exits once a search is performed 
        	//Print options to the user
		    while(searchMethod == 0) {
		        System.out.println("\nEnter one of the following command options:\n"
		        		+ "'Name' for searching an artist by name\n"
		        		+ "'Genre' for top artists of that genre\n"
		        		+ "'Add' to create a new custom list\n"
		        		+ "'Edit' to edit/view an existing list\n"
		        		+ "'Set' to view/set default/delete user-generated lists\n"
		        		+ "'Exit' to exit the application");
		        String responseSearch = scanner.nextLine().toLowerCase();
		        
		        //Parse response for relevant options
		        //set search method to name search if name is found and parse out error chars
		        if(responseSearch.contains("name")) {
		        	System.out.println("Type an Artist's Name to Search For:");
		            artist = scanner.nextLine();
		            artist = artist.replaceAll("[#%^&*/<>]", "");
		            artist = artist.replaceAll(" ", "%20");
		            artist = artist.replaceAll("\"", "");
		            searchMethod = 1;
		        }
		        
		        //set search method to by genre if genre option is selected
		        else if(responseSearch.contains("genre")) {
		        	max = 50;
		        	offset = 0;
		        	System.out.println("Type an Genre to Search for, followed by a max and offset to return if desired (or hit 'Enter' to skip)");
		            genre = scanner.nextLine();
		            genre = genre.replaceAll("[#%^&*/<>]", "");
			            
		            genre = genre.replaceAll("\"", "");
		            if(genre.contains(" ")) {
		            	genre = genre.replaceAll(" ", "+");
		            	
		            	genre = "%22" + genre + "%22";
		            }
		            
		            //Let the user select max results and offset if desired
		            String tempMax = scanner.nextLine();
		            String tempOffset = scanner.nextLine();
		            
		            tempMax = tempMax.replaceAll("[^0-9]", "");
		            tempOffset = tempOffset.replaceAll("[^0-9]", "");
		            if(!(tempMax.isEmpty()))
		            	max = Integer.parseInt(tempMax);
		            if(!(tempOffset.isEmpty()))
		            	offset = Integer.parseInt(tempOffset);

		            //System.out.print(tempMax + "    " + tempOffset);
            
		            searchMethod = 2;
		        }
		        
		        //enter the add functionality if add option is selected
		        else if(responseSearch.contains("add")) {
		        	System.out.println("Enter a name for your list.");
		        	String name = scanner.nextLine();
		        	
		        	//create new user list and get name if desired
		        	ArrayList<String> tempGenre = new ArrayList<String>();
		        	tempGenre.add("temp");
		        	tempGenre.add("val"); 
		        	ItemStruct tempItem = new ItemStruct(999, "temp", "temp", 0, tempGenre);
		        	ArrayList<ItemStruct> tempItemList = new ArrayList<ItemStruct>();

		        	//CustomList tempList = new CustomList(1, tempItemList);
		        	int index;
		        	
		        	if(userLists.isEmpty()) {
		        		tempItemList.add(tempItem);
		        		CustomList cl = new CustomList(0, name, tempItemList);
		        		userLists.add(cl);
		        		index = 0;
		        		
		        	}
		        	else {
		        		tempItemList.add(tempItem);
		        		index = userLists.size();
		        		CustomList cl = new CustomList(index, name, tempItemList);
		        		userLists.add(cl);
		        		
		        	}

		        	System.out.println(name + " List " + (index+1) + " Added!\n");

		        }
		        
		        //enter the edit/view functionality if selected
		        else if(responseSearch.contains("edit")) {
		        	
		        	if(userLists.isEmpty()) {
		        		System.out.println("No list was found, please add an artist before editting the lists.");	        		
		        	}
		        	else {
		        		int viewFlag = 1;

			        	System.out.println("\n" + userLists.get(userListID).getName());
			            System.out.println("ID  Name	 		         Popularity				Genre\n");
			            System.out.println("########################################################################################################");	            
			            
			            for (int i=0; i<userLists.get(userListID).items.size(); i++) {
			            	
			            	ItemStruct item1 = userLists.get(userListID).items.get(i);
			            	String nameHolder = (String)item1.getName();
			            	
			            	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
			            	
	
			            }
			            
			            while(viewFlag == 1) {
				            
				            System.out.println("");
				            
				            System.out.println("Options:");
				            System.out.println("'Name' to sort by artist's name in ascending order\n'Genre' to sort by genre tags");
				            System.out.println("'Pop' to sort by artist's popularity in ascending order\n"
				            		+ "'Delete' to delete an artist from the list.\n"
				            		+ "'Details' to see more information about a particular artist\n"
				            		+ "'Return' to return to Main Menu");
				            String sortMethod = scanner.nextLine().toLowerCase();
				            
				            //parse options when in view pane
				            //sort by name if selected
				            if(sortMethod.contains("name")) {
				            	System.out.println("Names Asc Sorted");

				            	alphaAscSort();
				            	
				            	System.out.println("");
				            }
				            
				            //sort by genre if selected
				            else if(sortMethod.contains("genre")) {
				            	System.out.println("Genres Asc Sorted");
				            	
				            	genreAscSort();
				            	
				            	System.out.println("");
				            }
				            
				            //sort by popularity if selected
				            else if(sortMethod.contains("pop")) {
				            	System.out.println("Popularity Asc Sorted");
				            	
				            	popAscSort();
				            	
				            	System.out.println("");
				            }
				            
				            //delete an artist from the list if selected and print new list
				            else if(sortMethod.contains("delete")) {
				            	
				            	//Display the default list for reference
				            	System.out.println("\n" + userLists.get(userListID).getName());
					            System.out.println("ID  Name	 		         Popularity				Genre\n");
					            System.out.println("########################################################################################################");	            
					            
					            for (int i=0; i<userLists.get(userListID).items.size(); i++) {
					            	
					            	ItemStruct item1 = userLists.get(userListID).items.get(i);
					            	String nameHolder = (String)item1.getName();
					            	
					            	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
			
					            }
				            	
					            //Get the entered ID of artist to delete from list
				            	System.out.println("Enter an ID to delete from the list");
				            	String idStr = scanner.nextLine();
				            	String idIn = idStr.replaceAll("[^0-9]", "");
				            	
				            	
				            	if(!(idIn.isEmpty())) {
				            		int id = Integer.valueOf(idIn);
				            		
				            		if((id >= 0) && (id < userLists.get(userListID).items.size())) {
				            			userLists.get(userListID).items.remove(id);
				            			
							            System.out.println("ID  Name	 		         Popularity				Genre\n");
							            System.out.println("########################################################################################################");	            
							            
							            for (int i=0; i<userLists.get(userListID).items.size(); i++) {
							            	
							            	ItemStruct item1 = userLists.get(userListID).items.get(i);
							            	String nameHolder = (String)item1.getName();
							            	
							            	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
					
							            }
							            
							            //Condition to check for if the list was made empty - if all artists are deleted from a list then a seed valid is needed
							            //If the list is left to be empty, then first issues are incurred when accessing the list afterwards
							            if(userLists.get(userListID).items.size() == 0) {
							            	System.out.println("List is now empty, entering temp val.");
							            	ArrayList<String> tempGenre = new ArrayList<String>();
								        	tempGenre.add("temp");
								        	tempGenre.add("val"); 
								        	ItemStruct tempItem = new ItemStruct(999, "temp", "temp", 0, tempGenre);

							        		userLists.get(userListID).items.add(tempItem);

							            }
				            		}
				            		else {
				            			System.out.println(id + " is not within range.");
				            		}
				            	}
				            	else {
				            		System.out.println(idStr + " is not a valid input.");
				            	}
				            	
				            	
				            }
				            
				            //Display the details of an artist if selected
				            else if(sortMethod.contains("details")) {
				            	//Reference list is created
				            	System.out.println("\n" + userLists.get(userListID).getName());
					            System.out.println("ID  Name	 		         Popularity				Genre\n");
					            System.out.println("########################################################################################################");	            
					            
					            for (int i=0; i<userLists.get(userListID).items.size(); i++) {
					            	
					            	ItemStruct item1 = userLists.get(userListID).items.get(i);
					            	String nameHolder = (String)item1.getName();
					            	
					            	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());				
					            }
				            	
					            //Get the artist ID to diusplay details of
				            	System.out.println("Enter in the ID of the Artist to View, or Enter 'Return' to return:");
				            	int detailFlag = 1;
				            	while(detailFlag == 1) {
					            	
					            	String result = scanner.nextLine();
					            	String exit = result.toLowerCase();
					            	String resultID = result.replaceAll("[^0-9]", "");
					            	
					            	if(!(resultID.isEmpty())) {
					            		int resultInt = Integer.valueOf(result);
						            	
						            	if((resultInt >= 0) && (resultInt < userLists.get(userListID).items.size())) {
						            		getArtistDetails(resultInt);     
						            		//scanner.nextLine();
						            		detailFlag = 0;
						            	}
						            	else
						            		System.out.println("Enter in the ID of the Artist to View, or Enter Return to return:");
					            	}
						            	
					            	else if(exit.contains("return")) 
					            		detailFlag = 0;
					            	else
					            		System.out.println("Enter in the ID of the Artist to View, or Enter 'Return' to return:");
				            	}
				            }
				            
				            else if(sortMethod.contains("return")) {
				            	viewFlag = 0;
				            }
		        		}
	      	
			            
			        }
		        }
		        
		        //Option from main menu to set a created list as the default list to access and operate on
		        //or if desired, a list can also be deleted from this option
		        else if(responseSearch.contains("set")) {
		        	if(userLists.isEmpty()) {
		        		System.out.println("No list was found, please create a list before setting.");	        		
		        	}
		        	else {
		        		System.out.println("");
		        		for(int i=0; i<userLists.size(); i++) {

		        			System.out.println(userLists.get(i).getListEnum() + " : " + userLists.get(i).getName());
		        		}
		        		
		        		System.out.println("\nEnter the ID of which list you would like to set for operations.");
		        		System.out.println("'Set #' to set the default list to operate on\n"
		        				+ "'Delete #' to delete the list from your application");
		        		String setResponse = scanner.nextLine().toLowerCase();
		        		String setResponseID = setResponse.replaceAll("[^0-9]", "");
		        		if(!setResponseID.isEmpty()) {
			        		int id = Integer.valueOf(setResponseID);
			        		
			        		if(setResponse.contains("set")) {
			        			userListID = id;
			        			System.out.println(userListID + " has been set as your default list.");
			        		}
			        		else if(setResponse.contains("delete")) {
			        			System.out.println("Are you certain you wish to delete list " + id + "? If so, type 'yes' otherwise select 'enter'");
			        			String confirm = scanner.nextLine().toLowerCase();
			        			if(confirm.contains("yes")) {
			        				userLists.remove(id);
			        				System.out.println("List " + id + " Deleted.");
			        			}
			        		}
		        		}
		        		else {
		        			System.out.println(setResponse + " is not a valid input.");
		        		}
		        	}
		        	
		        	
		        }
		        
		        //Catch exit case if selected
		        else if(responseSearch.contains("exit")) {

		        	return;
		        }
		        
		        //Catch invalid inputs
		        else 
		        	System.out.println("Please enter a valid input.");
		        
		    }
		    
		    //Get token to perform searches with Spotify API
		    try {
		        String url_token = "https://accounts.spotify.com/api/token";
		        //String clientCred = CLIENTID + ":" + CLIENTSECRET;
		
		        //System.out.println(url_token);
		        
		        //initiate HTTP API request
		        URL url = new URL(url_token);
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setRequestMethod("POST");
		        conn.setDoOutput(true);
		        conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
		        
		        String data = "grant_type=client_credentials&client_id=" + CLIENTID + "&client_secret=" + CLIENTSECRET + "";
		            
		            byte[] out = data.getBytes(StandardCharsets.UTF_8);
		
		   
		            OutputStream stream = conn.getOutputStream();
		            stream.write(out);
		
		
		            if (conn.getResponseCode() != 200) {
		                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "\nDecription : " + conn.getResponseMessage());
		
		        }
		
		        //parse input and put into a string
		        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		
		        //System.out.println("Output from Server \n");
		        
		        JSONParser parser1 = new JSONParser();
		        
		        String currentLine = br.readLine();
		        StringBuilder response = new StringBuilder();
		        while (currentLine != null) {
		            
		        	//System.out.println(currentLine);
		        	JSONObject jsonObject;
		        	//Get the generated token to perform following requests
					try {
						jsonObject = (JSONObject) parser1.parse(currentLine);
		                String token = (String) jsonObject.get("access_token");
		                TOKEN = token;
		                //System.out.println(TOKEN);
		
		                long time = (long) jsonObject.get("expires_in");
		                //System.out.println(time);					
					
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					response.append(currentLine).append("\n");
		            currentLine = br.readLine();           
		        }
		        
		             
		        conn.disconnect();
		    } catch (MalformedURLException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		
		    //Perform the artist by name search
		    if(searchMethod == 1) {
		        try {
		        	JSONParser parser2 = new JSONParser();
		        	JSONObject jsonObject = new JSONObject();
		        	
		        	String artistResp = getArtist(artist);
		        	if(artistResp.contains("Error")) {
		        		System.out.println("Invalid Artist Request, please try again and ensure input is valid");
		        		searchMethod = 0;
		        	} 
		        	
		        	else {
		        		jsonObject = (JSONObject) parser2.parse(artistResp);
						JSONObject object = (JSONObject) jsonObject.get("artists");
						JSONArray jsonarray = new JSONArray();
						jsonarray = (JSONArray) object.get("items");
						
						ArrayList<String> names = new ArrayList <String>();
						ArrayList<ItemStruct> results = new ArrayList <ItemStruct>();
						
						
						for (int i=0; i<jsonarray.size(); i++) {
							JSONObject nameResult = (JSONObject) jsonarray.get(i);
							JSONArray genreArray = (JSONArray) nameResult.get("genres");
							ArrayList<String> genres = new ArrayList <String>();
			
							for (int index=0; index<genreArray.size(); index++) {
			
								genres.add((String)genreArray.get(index));
					
							}
							
							ItemStruct index1 = new ItemStruct(i, (String)nameResult.get("id"), (String)nameResult.get("name"), 
													(long)nameResult.get("popularity"), genres);
							
							results.add((ItemStruct) index1);
							names.add((String) nameResult.get("name"));
							
							nameResult.get("popularity");
							
							//System.out.println(nameResult);
							
			
						}
						if(results.isEmpty()) {
			        		System.out.println("No artists were found, please ensure name was spelled correctly.");	        		
			        	}
						
						//print the results as a list
						else {
				            System.out.println("List #1:\n\n");
				            System.out.println("ID  Name	 		         Popularity				Genre");
				            System.out.println("########################################################################################################");	            
				            
				            for (int i=0; i<results.size(); i++) {
				            	
				            	ItemStruct item1 = results.get(i);
				            	String nameHolder = (String)item1.getName();
				            	
				            	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
				            	
				            }
				           
				            
				            Scanner artistAddList = new Scanner(System.in);
				            Scanner artistID = new Scanner(System.in);
				            
				            //ask for user if they want to add one of the names to the list
				            System.out.println("Would you like to add an artist to your list? Enter 'yes' if so");
				            if (artistAddList.hasNext("\\b+(y|yes|Y|YES|Yes)\\b+")) {
				            	System.out.println("Enter in the ID of the Artist to add:");
				            	int resultID = artistID.nextInt();
				            	
				            	//check if the user lists is empty or not
				            	if(userLists.isEmpty()) {
				            		ArrayList<ItemStruct> artistsAdd = new ArrayList <ItemStruct>();
					            	artistsAdd.add(results.get(resultID));
				            		CustomList tempList = new CustomList(0, "List 1", artistsAdd);
				            		userLists.add(tempList);
				            	}
				            	
				            	//check if the user list only has a temp value
				            	else if((userLists.get(userListID).items.get(0).getIndex() == 999)) {
				            		ArrayList<ItemStruct> artistsAdd = new ArrayList <ItemStruct>();
					            	artistsAdd.add(results.get(resultID));
				            		//CustomList tempList = new CustomList(1, artistsAdd);
				            		userLists.get(userListID).items.remove(0);
				            		
				            		userLists.get(userListID).items.add(results.get(resultID));
				            	}
				            	else {
					            	userLists.get(userListID).items.add(results.get(resultID));
				            	}
				            	int index = userLists.get(userListID).items.size();
		
				            	//System.out.println(index);
				            	System.out.println(userLists.get(userListID).items.get(index-1).getName());
				            	
				            }
				            //artistAddList.close();
				            //artistID.close();
						}
			            
			            
			            searchMethod = 0;
		        	}
		    
		
				} catch (ParseException e) {
		
					e.printStackTrace();
				}
		    }
		    
		    //Perform the search for artist by genre
		    else if(searchMethod == 2) {
		    	try {
		        	JSONParser parser2 = new JSONParser();
		        	
		        	JSONObject jsonObject = new JSONObject();
		     		            
		            jsonObject = (JSONObject) parser2.parse(getGenre(genre, max, offset));
		            
		            JSONObject object = (JSONObject) jsonObject.get("artists");
					JSONArray jsonarray = new JSONArray();
					jsonarray = (JSONArray) object.get("items");
			
					ArrayList<String> names = new ArrayList <String>();
					ArrayList<ItemStruct> results = new ArrayList <ItemStruct>();
					
					
					for (int i=0; i<jsonarray.size(); i++) {
						JSONObject nameResult = (JSONObject) jsonarray.get(i);
						JSONArray genreArray = (JSONArray) nameResult.get("genres");
						ArrayList<String> genres = new ArrayList <String>();
						//System.out.println(genreArray.size());
						for (int index=0; index<genreArray.size(); index++) {
							//System.out.println((String)genreArray.get(index));
							genres.add((String)genreArray.get(index));
				
						}
						
						ItemStruct index1 = new ItemStruct(i, (String)nameResult.get("id"), (String)nameResult.get("name"), 
												(long)nameResult.get("popularity"), genres);
						
						results.add((ItemStruct) index1);
						names.add((String) nameResult.get("name"));
						
						nameResult.get("popularity");
					}
					
					//print the results as a list
					if(results.isEmpty()) {
		        		System.out.println("No genre were found, please ensure genre was spelled correctly.");	        		
		        	}
					else {
						System.out.println("List #1:\n\n");
			            System.out.println("ID  Name	 		         Popularity				Genre");
			            System.out.println("########################################################################################################");	            
			            
			            for (int i=0; i<results.size(); i++) {
			            	
			            	ItemStruct item1 = results.get(i);
			            	String nameHolder = (String)item1.getName();
			            	
			            	System.out.format("%-4d %-40s %4d   %-40s\n", (int)i, nameHolder,	(int)item1.getPopularity(), item1.getGenre());
			            	
			            }
			            
			            Scanner artistAddList = new Scanner(System.in);
			            Scanner artistID = new Scanner(System.in);
			            
			            //ask for user input if they want to add one or more artists to the list
			            System.out.println("Would you like to add an artist to your list (Yes/no)?");
			            if (artistAddList.hasNext("\\b+(y|yes)\\b+")) {
			            	System.out.println("Enter in the ID of the Artist to add:");
			            	String [] ids = artistID.nextLine().trim().split("\\s+");
			            	
			            	for(int i=0; i<ids.length; i++) {
			         
				            	//int resultID = artistID.nextInt();
				            	int resultID = Integer.parseInt(ids[i]);
				            	
					            try {	
					            	if((resultID < 0) || (resultID >= results.size())) throw new IllegalArgumentException();
					            	
					            	if(userLists.isEmpty()) {
					            		ArrayList<ItemStruct> artistsAdd = new ArrayList <ItemStruct>();
						            	artistsAdd.add(results.get(resultID));
					            		CustomList tempList = new CustomList(0, "List 1", artistsAdd);
					            		userLists.add(tempList);
					            	}
					            	else if((userLists.get(userListID).items.get(0).getIndex() == 999)) {
					            		ArrayList<ItemStruct> artistsAdd = new ArrayList <ItemStruct>();
						            	artistsAdd.add(results.get(resultID));
					            		//CustomList tempList = new CustomList(1, artistsAdd);
					            		userLists.get(userListID).items.remove(0);
					            		userLists.get(userListID).items.add(results.get(resultID));
					            	}
					            	else {
						            	userLists.get(userListID).items.add(results.get(resultID));
					            	}
					            	int index = userLists.get(userListID).items.size();
			
					            	//System.out.println(index);
					            	System.out.println(userLists.get(userListID).items.get(index-1).getName());
			            		} catch (IllegalArgumentException iae) {
			            			System.out.println(resultID + " is out of range. Please ensure all values are within range.");
			            		}
			            		
			            	}	
			            }
			            //artistAddList.close();
			            //artistID.close();

					}
			            
			            
		            searchMethod = 0;
		
		        	} catch (ParseException e) {
		        		
						e.printStackTrace();
					}
		    }
        }
	}
		    
	//Function to make the API request on getting the artists by name
	public static String getArtist (String artistReq) {
		try {
	    	//System.out.println(artistReq);
	    	String artist_get = 
	    			"https://api.spotify.com/v1/search?q=" + artistReq + "&type=artist";
	    	
	    	URL url = new URL(artist_get);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setDoOutput(true);
	        conn.setRequestProperty("content-type", "application/json");
	        conn.setRequestProperty("Authorization", String.format("Bearer %s", TOKEN));
	       
	        
	        if (conn.getResponseCode() != 200) {
	        	throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "\nDecription : " + conn.getErrorStream());
	        }
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	
	        String output;

	        StringBuilder response = new StringBuilder();
	        while ((output = br.readLine()) != null) {     	
	
	        	response.append(output).append("\n");
	
	        } 
	        
	        return response.toString();
	          
	   
		} catch (MalformedURLException e) {
	            e.printStackTrace();
	            searchMethod = 0;
	            return "Error";
	    } catch (IOException e) {
	            e.printStackTrace();
	            searchMethod = 0;
	            return "Error";
	    } catch (RuntimeException re) {
	    		searchMethod = 0;
	    		return "Error";
	    }
		
	}
	
	//function to make the API request to get the list of popular artists by genre
	public static String getGenre (String genreReq, int resultsNum, int offSet) {
		
		try {
	    	//System.out.println(genreReq);
	    	String genre_get = 
	    			"https://api.spotify.com/v1/search?q=genre%3A" + genreReq + "&type=artist&limit=" + resultsNum + "&offset=" + offSet;
	    	
	    	URL url = new URL(genre_get);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setDoOutput(true);
	        conn.setRequestProperty("content-type", "application/json");
	        conn.setRequestProperty("Authorization", String.format("Bearer %s", TOKEN));
	       
	        
	        if (conn.getResponseCode() != 200) {
	        	throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "\nDecription : " + conn.getErrorStream());
	        }
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	
	        String output;
	        //System.out.println("Output from Server .... \n");
	        
	        StringBuilder response = new StringBuilder();
	        while ((output = br.readLine()) != null) {     	
	
	        	response.append(output).append("\n");   
	        	//System.out.println(output);
	        } 
	        
	        return response.toString();

	   
		} catch (MalformedURLException e) {
	            e.printStackTrace();
	            return "Error";
	    } catch (IOException e) {
	            e.printStackTrace();
	            return "Error";
	    }
		
	}
	
	//function to make the API request to the list of artist's releases/albums
	public static String getArtistDetailsAlbums (String keyID) {
			
			try {
		    	//System.out.println(keyID);
		    	String genre_get = 
		    			"https://api.spotify.com/v1/artists/" + keyID +"/albums?market=US";
		    	
		    	URL url = new URL(genre_get);
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setRequestMethod("GET");
		        conn.setDoOutput(true);
		        conn.setRequestProperty("content-type", "application/json");
		        conn.setRequestProperty("Authorization", String.format("Bearer %s", TOKEN));
		       
		        
		        if (conn.getResponseCode() != 200) {
		        	throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "\nDecription : " + conn.getErrorStream());
		        }
		
		        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		
		        String output;
		        //System.out.println("Output from Server .... \n");
		        
		        StringBuilder response = new StringBuilder();
		        while ((output = br.readLine()) != null) {     	
		
		        	response.append(output).append("\n");   
		        } 
		        
		        return response.toString();
	
		   
			} catch (MalformedURLException e) {
		            e.printStackTrace();
		            return "Error";
		    } catch (IOException e) {
		            e.printStackTrace();
		            return "Error";
		    }	
			
		}
	
	//function to make the API request to the list of artist's popular tracks
	public static String getArtistDetailsTracks (String keyID) {
		
		try {
	    	//System.out.println(keyID);
	    	String genre_get = 
	    			"https://api.spotify.com/v1/artists/" + keyID + "/top-tracks?market=US";
	    	
	    	URL url = new URL(genre_get);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setDoOutput(true);
	        conn.setRequestProperty("content-type", "application/json");
	        conn.setRequestProperty("Authorization", String.format("Bearer %s", TOKEN));
	       
	        
	        if (conn.getResponseCode() != 200) {
	        	throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "\nDecription : " + conn.getErrorStream());
	        }
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	
	        String output;
	        //System.out.println("Output from Server .... \n");
	        
	        StringBuilder response = new StringBuilder();
	        while ((output = br.readLine()) != null) {     	
	
	        	response.append(output).append("\n");   
	        } 
	        
	        return response.toString();
	
	   
		} catch (MalformedURLException e) {
	            e.printStackTrace();
	            return "Error";
	    } catch (IOException e) {
	            e.printStackTrace();
	            return "Error";
	    }	
		
	}
	
	//function to make the API request to the list of artist's related artists
	public static String getArtistDetailsRelArtists (String keyID) {
		
		try {
	    	//System.out.println(keyID);
	    	String genre_get = 
	    			"https://api.spotify.com/v1/artists/" + keyID + "/related-artists";
	    	
	    	URL url = new URL(genre_get);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setDoOutput(true);
	        conn.setRequestProperty("content-type", "application/json");
	        conn.setRequestProperty("Authorization", String.format("Bearer %s", TOKEN));
	       
	        
	        if (conn.getResponseCode() != 200) {
	        	throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "\nDecription : " + conn.getErrorStream());
	        }
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	
	        String output;
	        //System.out.println("Output from Server .... \n");
	        
	        StringBuilder response = new StringBuilder();
	        while ((output = br.readLine()) != null) {     	
	
	        	response.append(output).append("\n");   
	        } 
	        
	        return response.toString();
	
	   
		} catch (MalformedURLException e) {
	            e.printStackTrace();
	            return "Error";
	    } catch (IOException e) {
	            e.printStackTrace();
	            return "Error";
	    }	
		
	}
	

}



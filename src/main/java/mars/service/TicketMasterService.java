package mars.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import mars.common.GeoHash;
import mars.entity.HistoryItem;
import mars.entity.Item;
import mars.entity.Item.ItemBuilder;;

@Service
public class TicketMasterService extends AbstractGenericService<Item> {

	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate restTemplate;

	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String API_KEY = "3CKFnTIaH7euK22UZjAYNBEAQvHOGDIT";
	private static final int PRECISION = 8;

	public List<Item> search(double latitude, double longitude, String term) {
		String geoHash = GeoHash.encodeGeohash(latitude, longitude, PRECISION);

		HttpHeaders headers = new HttpHeaders();
		headers.set("ACCEPT", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(URL)
				.queryParam("apikey", API_KEY)
				.queryParam("geoPoint", geoHash)
				.queryParam("radius", "100");
		if (StringUtils.isNoneEmpty(term)) {
			try {
				builder.queryParam("keyword", URLEncoder.encode(term, "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		HttpEntity<?> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restTemplate
				.exchange(builder.build().encode().toUri(), 
						HttpMethod.GET,
						entity, 
						String.class);

		JSONObject obj = new JSONObject(responseEntity.getBody());
		System.out.println(obj.toString());

		if (obj.isNull("_embedded")) {
			return new ArrayList<>();
		}
		JSONObject embedded = obj.getJSONObject("_embedded");
		JSONArray events = embedded.getJSONArray("events");
		List<Item> items = getItemList(events);

//		for (Item item : items) {
//			// we will update database using hibernate later
//		}
		return items;
	}

	// Convert JSONArray into Item objects
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();

			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			builder.setAddress(getAddress(event));
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageUrl(event));
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}

			itemList.add(builder.build());
		}
		return itemList;
	}

	private String getAddress(JSONObject event) throws JSONException {
		JSONObject venue = getVenue(event);
		if (venue == null) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		if (!venue.isNull("address")) {
			JSONObject address = venue.getJSONObject("address");
			if (!address.isNull("line1")) {
				result.append(address.getString("line1"));
			}
			if (!address.isNull("line2")) {
				result.append(address.getString("line2"));
			}
			if (!address.isNull("line3")) {
				result.append(address.getString("line3"));
			}
			result.append(",");
		}

		if (!venue.isNull("city")) {
			JSONObject city = venue.getJSONObject("city");
			if (!city.isNull("name")) {
				result.append(city.getString("name"));
			}
		}

		if (result.length() > 0) {
			return result.toString();
		}
		return "";
	}

	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}

	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			for (int i = 0; i < images.length(); i++) {
				JSONObject image = images.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}
	
	private JSONObject getVenue(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				if (venues.length() > 0) {
					return venues.getJSONObject(0);
				}
			}
		}
		return null;
	}

	public Set<Item> getUserFavorites(String userId) {
		return null;
	}

	public void setUserFavorites(HistoryItem historyItem) {

	}

	public void deleteUserFavorites(HistoryItem historyItem) {

	}
}

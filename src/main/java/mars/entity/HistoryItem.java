package mars.entity;

import java.util.List;

/**
 * Use Java object to represent request body passed by users
 * {
 * 		'user_id': '1111',
 * 		'favorite': ['abc', 'def']
 * }
 * */
public class HistoryItem {

	private String user_id;
	private List<String> favorite;
	
	public String getUser_id() {
		return user_id;
	}
	public List<String> getFavorite() {
		return favorite;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public void setFavorite(List<String> favorite) {
		this.favorite = favorite;
	}
}

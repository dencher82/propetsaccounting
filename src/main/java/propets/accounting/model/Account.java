package propets.accounting.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = {"email"})
@Document(collection = "accounts")
public class Account {
	@Id
	String email;
	String password;
	String name;
	String avatar;
	String phone;
	Set<String> roles = new HashSet<>();
	Set<String> favorites = new HashSet<>();
	Set<String> activities = new HashSet<>();
	boolean flBlocked;
	
	public Account(String email, String name) {
		this.email = email;
		this.name = name;
	}
	
	public boolean addRole(String role) {
		return roles.add(role);
	}
	
	public boolean removeRole(String role) {
		return roles.remove(role);
	}

	public void addFavorite(String postId) {
		favorites.add(postId);
	}
	
	public void removeFavorite(String postId) {
		favorites.remove(postId);
	}
	
	public void addActivity(String postId) {
		activities.add(postId);
	}
	
	public void removeActivity(String postId) {
		activities.remove(postId);
	}
	
}

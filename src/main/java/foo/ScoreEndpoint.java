package foo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "myApi",
     version = "v1",
     audiences = "689297071615-irgk1dtiu0ts6bntktqmshsc7u44610o.apps.googleusercontent.com",
  	 clientIds = "689297071615-irgk1dtiu0ts6bntktqmshsc7u44610o.apps.googleusercontent.com",
     namespace =
     @ApiNamespace(
		   ownerDomain = "webandcloud-273109.appspot.com",
		   ownerName = "webandcloud-273109.appspot.com",
		   packagePath = "")
     )

public class ScoreEndpoint {
	
	//Methode permettant l'ajout de profil utilisateur si celui-ci n'existe pas en base
	@ApiMethod(name = "addprofil", httpMethod = HttpMethod.POST)
	public Entity addprofil(ProfilMessage Pm) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query("Profil").setFilter(new FilterPredicate("mail", FilterOperator.EQUAL, Pm.email));

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		
		if (result.isEmpty())
		{
			List<String> tab = new ArrayList<>();
			tab.add("");
			List<String> tab2 = new ArrayList<>();
			tab2.add(Pm.email);
			Entity e = new Entity("Profil");
			e.setProperty("mail", Pm.email);
			e.setProperty("follow", tab);
			e.setProperty("follower", tab2);
			e.setProperty("pseudo", Pm.pseudo);

			Transaction txn = datastore.beginTransaction();
			datastore.put(e);
			txn.commit();
		}
		return result.get(0);
	}
	
	//Methode permettant de follow un profil déja présent dans la base
	@ApiMethod(name = "followprofil", httpMethod = HttpMethod.POST)
	public Entity followprofil(FollowMessage Fm) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query("Profil").setFilter(new FilterPredicate("mail", FilterOperator.EQUAL, Fm.mailFollow));

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		
		if (result.size()==1)
		{
			Query q2 = new Query("Profil").setFilter(new FilterPredicate("mail", FilterOperator.EQUAL, Fm.mail));
			PreparedQuery pq2 = datastore.prepare(q2);
			Entity result2 = pq2.asSingleEntity();
			List<String> tab = new ArrayList<>();
			tab = (List<String>) result2.getProperty("follow");
			tab.add(Fm.mailFollow);
			result2.setProperty("follow", tab);
			Transaction txn = datastore.beginTransaction();
			datastore.put(result2);
			
			Query q3 = new Query("Profil").setFilter(new FilterPredicate("mail", FilterOperator.EQUAL, Fm.mailFollow));
			PreparedQuery pq3 = datastore.prepare(q3);
			Entity result3 = pq3.asSingleEntity();
			List<String> tab2 = new ArrayList<>();
			tab2 = (List<String>) result3.getProperty("follower");
			tab2.add(Fm.mail);
			result3.setProperty("follower", tab2);
			datastore.put(result3);
			
			txn.commit();
			return(result2);
		}
		else
		{
			return null;
		}
	}
	
	//Méthode permettant de follow une fois un post 
	@ApiMethod(name = "likeMessage", httpMethod = HttpMethod.POST)
	public Key likeMessage(User user, PostKey pk) {

		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key k = KeyFactory.createKey(pk.kind, pk.name);
		Query q = new Query("Post").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, k));
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		List<String> tab = new ArrayList<>();
		tab = (List<String>) result.getProperty("likec");
		if (!tab.contains(user.getEmail()))
		{
			tab.add(user.getEmail());
			result.setProperty("likec", tab);
			datastore.put(result);

			
			Transaction txn = datastore.beginTransaction();
			txn.commit();
		}
		
		return k;
	}
	
	//Méthode retournant le tableau des likes afin d'avoir un compte via le .length
	@ApiMethod(name = "cptlike", httpMethod = HttpMethod.POST)
	public List<String> cptlike(PostKey pk) {

		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key k = KeyFactory.createKey(pk.kind, pk.name);
		Query q = new Query("Post").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, k));
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		List<String> tab = new ArrayList<>();
		tab = (List<String>)result.getProperty("likec");
		return tab;
	}
	
	//Méthode retournant le tableau des follow afin d'avoir un compte via le .length
	@ApiMethod(name = "cptfollower", httpMethod = HttpMethod.POST)
	public List<String> cptfollower(User user) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Profil").setFilter(new FilterPredicate("mail", FilterOperator.EQUAL, user.getEmail()));

		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		List<String> tab = new ArrayList<>();
		tab = (List<String>)result.getProperty("follower");
		
		return tab;
	}
	
	//Méthode permettant de supprimer un post
	@ApiMethod(name = "supprimerMessage", httpMethod = HttpMethod.POST)
	public Key supprimerMessage(User user, PostKey pk) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key k = KeyFactory.createKey(pk.kind, pk.name);
		datastore.delete(k);
		return k;
	}

	//Méthode retournant la liste des postes d'un profil ainsi que celui de ses follows
	@ApiMethod(name = "getPost",httpMethod = ApiMethod.HttpMethod.GET)
	public CollectionResponse<Entity> getPost(User user, @Nullable @Named("next") String cursorString)
			throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Query q = new Query("Post").setFilter(new FilterPredicate("to", FilterOperator.EQUAL, user.getEmail()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);

		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}

		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		cursorString = results.getCursor().toWebSafeString();

		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	}

	//Méthode permettant de poster un message
	@ApiMethod(name = "postMsg", httpMethod = HttpMethod.POST)
	public Entity postMsg(User user, PostMessage pm) throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query("Profil").setFilter(new FilterPredicate("mail", FilterOperator.EQUAL, user.getEmail()));
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		List<String> tab = new ArrayList<>();
		List<String> tab2 = new ArrayList<>();
		List<String> tab3 = new ArrayList<>();
		tab3 = (List<String>)result.getProperty("follower");
		tab2.add("");

		Entity e = new Entity("Post", Long.MAX_VALUE-(new Date()).getTime()+":"+user.getEmail());
		e.setProperty("owner", user.getEmail());
		e.setProperty("url", pm.url);
		e.setProperty("body", pm.body);
		e.setProperty("to",tab3);
		e.setProperty("likec", tab2);
		e.setProperty("date", new Date());

		Transaction txn = datastore.beginTransaction();
		datastore.put(e);
		txn.commit();
		return result;
	}
}

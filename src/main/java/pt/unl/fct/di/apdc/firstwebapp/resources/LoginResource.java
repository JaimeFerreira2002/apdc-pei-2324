package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.Time;
import java.util.logging.Logger;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

//esta classe é considerada um servico REST, um rest endpoint é um método

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	/**
	 * Logger Object, used for logging
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	private final Datastore datastore;


	public LoginResource() {
		datastore = DatastoreOptions.getDefaultInstance().getService();

	} //construtor vazio permite que as classes sejam instanciadas pelo runtime do Jersey (a ver)

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data, @Context HttpServletRequest request ,@Context HttpHeaders headers) {

		LOG.fine("Login attempt by user: " + data.username);

		//generate the key, keys have to be generated outside of the transactions
		//we construct the key from the username
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);


		Transaction txn = datastore.newTransaction(); //create a new transaction

		try{
			Entity user = txn.get(userKey);
			if(user == null){
				//username doesnt exist
				LOG.warning("Failed to login, username doesnt exist");
				return Response.status(Response.Status.FORBIDDEN).build();
			}

			String hashedPWD = (String) user.getString("user_pwd");
			if(hashedPWD.equals(DigestUtils.sha512Hex(data.password))){
				//password is correct

				//return token
				AuthToken token = new AuthToken(data.username);
				return Response.ok(g.toJson(token)).build();
			}else{
				//wrong password, update stats
				/////do more here
				AuthToken token = new AuthToken(data.username);
				return Response.ok(g.toJson(token)).build();

			}

		}catch (Exception e){
			//we roll back in case of an error, to cancel the transactions
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()){
				txn.rollback();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}



}

package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.cloud.datastore.DatastoreOptions;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;


@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Datastore datastore;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";



    public RegisterResource(){
        datastore = DatastoreOptions.getDefaultInstance().getService();
    }


    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegistration(UserData data, @Context HttpServletResponse response){
        LOG.fine("Attempt to register user: " + data.username);

        //checks input data
        if(!Objects.equals(data.password, data.passwordConfirmation)){
            return Response.status(Response.Status.BAD_REQUEST).entity("Passwords dont match").build();
        }else if (containsNumbers(data.name)){
            return Response.status(Response.Status.BAD_REQUEST).entity("Name is not valid").build();
        }

        //creates an entity user from the data. The key is the username
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = Entity.newBuilder(userKey)
                .set("username",data.username)
                .set("user_name", data.name)
                .set("user_email",data.email).set("user_name", data.name)
                .set("user_phone", data.phone)
                .set("user_pwd", DigestUtils.sha512Hex(data.password))
                .set("user_pwd_conf", DigestUtils.sha512Hex(data.passwordConfirmation))
                .set("user_role", "USER")
                .set("user_state","inactive")
                .set("user_creation_time", Timestamp.now()).build();
        datastore.put(user);
        LOG.info("User resgistered: " + data.username);

        return Response.ok("{}").build(); //responde com codigo 200 e com body vazio, o build é que faz a resposta

    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean containsNumbers(String input) {
        Pattern pattern = Pattern.compile(".*\\d.*");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }


}

package pt.unl.fct.di.apdc.firstwebapp.resources;


import com.google.cloud.datastore.*;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/edit_users")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EditUsersResources {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Datastore datastore;

    public  EditUsersResources(){
        datastore = DatastoreOptions.getDefaultInstance().getService();

    }

    @POST
    @Path("/change_role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(ChangeRoleData data,@Context HttpServletRequest request ,@Context HttpHeaders headers) {

        Key currentUserKey =  datastore.newKeyFactory().setKind("User").newKey(data.currentUsername);
        Entity currentUserEntity = datastore.get(currentUserKey);
        String currentUserRole = currentUserEntity.getString("user_role");

        Key userToChangeKey =  datastore.newKeyFactory().setKind("User").newKey(data.usernameToChange);
        Entity userToChangeEntity = datastore.get(userToChangeKey);
        String userToChangeRole = userToChangeEntity.getString("user_role");

        Transaction txn = datastore.newTransaction();
        try {
            // Perform operations within the transaction
            switch (currentUserRole) {
                case "USER":
                    return Response.status(Response.Status.BAD_REQUEST).entity("You dont have permission to change roles").build();
                case "GBO":
                    return Response.status(Response.Status.BAD_REQUEST).entity("You dont have permission to change roles").build();
                case "GA":
                    if ((userToChangeRole.equals("USER") && data.newRole.equals("GBO")) || (userToChangeRole.equals("GBO") && data.newRole.equals("USER"))) {

                        // Update the user role
                        if (userToChangeEntity != null) {
                            userToChangeEntity = Entity.newBuilder(userToChangeEntity)
                                    .set("user_role", data.newRole)
                                    .build();
                            txn.update(userToChangeEntity);
                            txn.commit();
                            return Response.ok("{}").build();
                        } else {
                            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
                        }
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).entity("Not valid").build();
                    }

                case "SU":
                    // Update the user role
                    if (userToChangeEntity != null) {
                        userToChangeEntity = Entity.newBuilder(userToChangeEntity)
                                .set("user_role", data.newRole)
                                .build();
                        txn.update(userToChangeEntity);
                        txn.commit();
                        return Response.ok("{}").build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
                    }

                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity("Not valid role").build();
            }
        } catch (Exception e) {
            //we roll back in case of an error, to cancel the transactions
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to change role").build();
        }finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/change_state")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeState(ChangeStateData data,@Context HttpServletRequest request ,@Context HttpHeaders headers) {

        Key currentUserKey =  datastore.newKeyFactory().setKind("User").newKey(data.currentUsername);
        Entity currentUserEntity = datastore.get(currentUserKey);
        String currentUserRole = currentUserEntity.getString("user_role");
        boolean currentUserState = currentUserEntity.getBoolean("user_state");

        Key userToChangeKey =  datastore.newKeyFactory().setKind("User").newKey(data.usernameToChange);
        Entity userToChangeEntity = datastore.get(userToChangeKey);
        String userToChangeRole = userToChangeEntity.getString("user_role");
        boolean userToChangeState = userToChangeEntity.getBoolean("user_state");

        Transaction txn = datastore.newTransaction();
        try {
            // Perform operations within the transaction
            switch (currentUserRole) {
                case "USER":
                    return Response.status(Response.Status.BAD_REQUEST).entity("You dont have permission to change states").build();
                case "GBO":
                    if(userToChangeRole.equals("USER")){
                        //change state
                        if (userToChangeEntity != null) {
                            userToChangeEntity = Entity.newBuilder(userToChangeEntity)
                                    .set("user_state", data.newState)
                                    .build();
                            txn.update(userToChangeEntity);
                            txn.commit();
                            return Response.ok("{}").build();
                        } else {
                            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
                        }

                    }else{
                        return Response.status(Response.Status.BAD_REQUEST).entity("You can only change users with role USER").build();
                    }

                case "GA":
                    if(userToChangeRole.equals("USER") || userToChangeRole.equals("GBO")){
                        //change state
                        if (userToChangeEntity != null) {
                            userToChangeEntity = Entity.newBuilder(userToChangeEntity)
                                    .set("user_state", data.newState)
                                    .build();
                            txn.update(userToChangeEntity);
                            txn.commit();
                            return Response.ok("{}").build();
                        } else {
                            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
                        }

                    }else{
                        return Response.status(Response.Status.BAD_REQUEST).entity("You can only change users with role USER or GBO").build();
                    }

                case "SU":
                    if (userToChangeEntity != null) {
                        userToChangeEntity = Entity.newBuilder(userToChangeEntity)
                                .set("user_state", data.newState)
                                .build();
                        txn.update(userToChangeEntity);
                        txn.commit();
                        return Response.ok("{}").build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
                    }

                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity("Not valid state, either true or false").build();
            }
        }catch (Exception e) {
            //we roll back in case of an error, to cancel the transactions
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to change state").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/delete_user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(DeleteUserData data,@Context HttpServletRequest request ,@Context HttpHeaders headers) {

        Key currentUserKey =  datastore.newKeyFactory().setKind("User").newKey(data.currentUsername);
        Entity currentUserEntity = datastore.get(currentUserKey);
        String currentUserRole = currentUserEntity.getString("user_role");

        Key userToDeleteKey =  datastore.newKeyFactory().setKind("User").newKey(data.usernameToDelete);
        Entity userToDeleteEntity = datastore.get(userToDeleteKey);
        String userToDeleteRole = userToDeleteEntity.getString("user_role");

        Transaction txn = datastore.newTransaction();
        try {
            // Perform operations within the transaction
            switch (currentUserRole) {
                case "USER":
                    if(userToDeleteEntity.getString("username").equals(currentUserEntity.getString("username"))){
                        //delete own account
                        if(userToDeleteEntity != null){
                            datastore.delete(userToDeleteKey);
                            txn.commit();
                            return Response.ok("User deleted successfully").build();
                        }else{
                            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();

                        }


                    }else{
                        return Response.status(Response.Status.BAD_REQUEST).entity("Having the role USER, you cna only delete your own account").build();
                    }

                    case "GBO":
                        return Response.status(Response.Status.BAD_REQUEST).entity("You cant remove users").build();

                case "GA":
                    if(userToDeleteRole.equals("USER") || userToDeleteRole.equals("GBO")){
                        //delete user
                        if(userToDeleteEntity != null){
                            datastore.delete(userToDeleteKey);
                            txn.commit();
                            return Response.ok("User deleted successfully").build();
                        }else{
                            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
                        }
                    }else{
                        return Response.status(Response.Status.BAD_REQUEST).entity("You can only change users with role USER or GBO").build();
                    }

                case "SU":
                    if(userToDeleteEntity != null){
                        datastore.delete(userToDeleteKey);
                        txn.commit();
                        return Response.ok("User deleted successfully").build();
                    }else{
                        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
                    }

                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity("Not valid state, either true or false").build();
            }
        } catch (Exception e) {
            //we roll back in case of an error, to cancel the transactions
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("failed to delete user").build();
        }finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/list_users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(String currentUsername) {

        Key currentUserKey =  datastore.newKeyFactory().setKind("User").newKey(currentUsername);
        Entity currentUserEntity = datastore.get(currentUserKey);
        String currentUserRole = currentUserEntity.getString("user_role");

        Transaction txn = datastore.newTransaction();
        try{
            switch (currentUserRole){

                case "USER":
                        Query<Entity> query = Query.newEntityQueryBuilder()
                                .setKind("User")
                                .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "USER"))
                                .build();

                        QueryResults<Entity> queryResults = datastore.run(query);

                        List<UserData> userList = new ArrayList<>();
                        while (queryResults.hasNext()) {
                            Entity entity = queryResults.next();
                            UserData userData = entityToUserData(entity);
                            if(userData.state.equals("active")){
                                userList.add(userData);
                            }

                        }

                        return Response.ok(userList).build();

                case "GBO":
                    Query<Entity> query2 = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "USER"))
                            .build();

                    QueryResults<Entity> queryResults2 = datastore.run(query2);

                    List<UserData> userList2 = new ArrayList<>();
                    while (queryResults2.hasNext()) {
                        Entity entity = queryResults2.next();
                        UserData userData = entityToUserData(entity);
                            userList2.add(userData);

                    }

                    return Response.ok(userList2).build();


                case "GA":
                    Query<Entity> query3 = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .build();

                    QueryResults<Entity> queryResults3 = datastore.run(query3);

                    List<UserData> userList3 = new ArrayList<>();
                    while (queryResults3.hasNext()) {
                        Entity entity = queryResults3.next();
                        UserData userData = entityToUserData(entity);
                        if(!userData.role.equals("SU")){

                            userList3.add(userData);
                        }


                    }

                    return Response.ok(userList3).build();


                case "SU":
                    Query<Entity> query4 = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .build();

                    QueryResults<Entity> queryResults4 = datastore.run(query4);

                    List<UserData> userList4 = new ArrayList<>();
                    while (queryResults4.hasNext()) {
                        Entity entity = queryResults4.next();
                        UserData userData = entityToUserData(entity);
                            userList4.add(userData);



                    }

                    return Response.ok(userList4).build();


                default:

                    return Response.status(Response.Status.BAD_REQUEST).entity("Not valid role").build();


            }
        }catch (Exception e) {
            //we roll back in case of an error, to cancel the transactions
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("failed to list users").build();
        }finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }


    }



    private UserData entityToUserData(Entity entity) {
        UserData userData = new UserData();
        userData.username = entity.getString("username");
        userData.name = entity.getString("user_name");
        userData.email = entity.getString("user_email");
        userData.phone = entity.getString("user_phone") ;
        userData.role = entity.getString("user_role");
        userData.state =  entity.getString("user_state");

        return userData;
    }


}

class ChangeRoleData{
    public String currentUsername;
    public String usernameToChange;
    public String newRole;

    public ChangeRoleData() {
    }

    public ChangeRoleData(String currentUsername, String usernameToChange, String newRole){
        this.currentUsername = currentUsername;
        this.usernameToChange = usernameToChange;
        this.newRole = newRole;

    }

}

class ChangeStateData{
    public String currentUsername;
    public String usernameToChange;
    public String newState;

    public ChangeStateData() {
    }

    public ChangeStateData(String currentUsername, String usernameToChange, String newState){
        this.currentUsername = currentUsername;
        this.usernameToChange = usernameToChange;
        this.newState = newState;

    }

}

class DeleteUserData{
    public String currentUsername;
    public String usernameToDelete;


    public DeleteUserData() {
    }

    public DeleteUserData(String currentUsername, String usernameToDelete){
        this.currentUsername = currentUsername;
        this.usernameToDelete = usernameToDelete;

    }

}


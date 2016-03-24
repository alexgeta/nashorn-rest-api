package com.jsengine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsengine.intf.ExecutionManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.ScriptException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/api")
public class RestController {

    private static ExecutionManager<ExecutionContext> executionManager = new ExecutionManagerImpl();
    private static Gson gson = new GsonBuilder()
                                   .setPrettyPrinting()
                                   .create();

    @GET
    @Path("/execute/{script}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response execute(@PathParam("script") String script) {
        ExecutionContext executionContext = new ExecutionContext();
        try {
            long executionId = executionManager.execute(script);
            executionContext = executionManager.get(executionId);
            while (executionContext.getStatus().equals("EXECUTION")){
                Thread.sleep(100);
                executionContext = executionManager.get(executionId);
            }
        } catch (ScriptException e) {
            executionContext.setError(e.getMessage());
        } catch (Throwable e){
            executionContext.setError(ExceptionUtils.getStackTrace(e));
        }
        return Response.status(200).entity(gson.toJson(executionContext)).build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        return Response.status(200).entity(gson.toJson(executionManager.list())).build();
    }

    @GET
    @Path("/get/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("executionId") String executionId) {
        ExecutionContext executionContext = new ExecutionContext();
        try {
            executionContext = executionManager.get(Long.parseLong(executionId));
        } catch (NumberFormatException e) {
            executionContext.setError("Invalid execution id '"+executionId+"', must be number value.");
        }
        if(executionContext == null){
            executionContext = new ExecutionContext();
            executionContext.setError("Execution context with id "+executionId+" not found.");
        }
        return Response.status(200).entity(gson.toJson(executionContext)).build();
    }

    @GET
    @Path("/delete/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("executionId") String executionId) {
        ExecutionContext executionContext = new ExecutionContext();
        try {
            boolean isDeleted = executionManager.delete(Long.parseLong(executionId));
            return Response.status(200).entity(gson.toJson(isDeleted)).build();
        } catch (NumberFormatException e) {
            executionContext.setError("Invalid execution id '"+executionId+"', must be number value.");
        }
        return Response.status(200).entity(gson.toJson(executionContext)).build();
    }

    @GET
    @Path("/async")
    public void asyncGet(@Suspended final AsyncResponse asyncResponse) {
        new Thread(() -> {
            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            asyncResponse.resume("Done");
        }).start();
    }
}

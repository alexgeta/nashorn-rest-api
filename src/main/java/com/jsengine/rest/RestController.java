package com.jsengine.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsengine.ExecutionContext;
import com.jsengine.ExecutionManagerImpl;
import com.jsengine.State;
import com.jsengine.intf.ExecutionManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.jersey.server.ManagedAsync;

import javax.script.ScriptException;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;


@Path("/api")
public class RestController {

    private static ExecutionManager<ExecutionContext> executionManager = new ExecutionManagerImpl();
    private static Gson gson = new GsonBuilder()
                                   .setPrettyPrinting()
                                   .create();
    private long TIMEOUT = 5;

    @GET
    @Path("/execute/{script}")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void execute(
            @PathParam("script") String script,
            @HeaderParam("async") boolean async,
            @HeaderParam("timeout") Long timeout,
            @Suspended final AsyncResponse asyncResponse) {
        if(timeout != null){
            TIMEOUT = timeout;
        }
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setScript(script);
        try {
            long executionId = executionManager.execute(script);
            executionContext = executionManager.get(executionId);
            if(async) {
                asyncResponse.resume(gson.toJson(executionContext));
                return;
            } else {
                asyncResponse.setTimeout(TIMEOUT, TimeUnit.SECONDS);
                asyncResponse.setTimeoutHandler(new TimeoutHandler() {
                    @Override
                    public void handleTimeout(AsyncResponse asyncResponse) {
                        executionManager.delete(executionId);
                        ExecutionContext response = new ExecutionContext();
                        response.setScript(script);
                        response.setError("Timeout exceeded.");
                        asyncResponse.resume(gson.toJson(response));
                    }
                });
            }
            while (executionContext.getStatus() == State.EXECUTION){
                Thread.sleep(100);
                executionContext = executionManager.get(executionId);
            }
        } catch (ScriptException e) {
            executionContext.setError(e.getMessage());
        } catch (Throwable e){
            executionContext.setError(ExceptionUtils.getStackTrace(e));
        }
        asyncResponse.resume(gson.toJson(executionContext));
    }

    @POST
    @Path("/execute")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void executePOST(
            String script,
            @HeaderParam("async") boolean async,
            @HeaderParam("timeout") Long timeout,
            @Suspended final AsyncResponse asyncResponse) {
        execute(script, async, timeout, asyncResponse);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        return Response.status(200).entity(gson.toJson(executionManager.list())).build();
    }

    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPOST() {
        return list();
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

    @POST
    @Path("/get/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPOST(@PathParam("executionId") String executionId){
        return get(executionId);
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

    @POST
    @Path("/delete/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePOST(@PathParam("executionId") String executionId) {
        return delete(executionId);
    }
}

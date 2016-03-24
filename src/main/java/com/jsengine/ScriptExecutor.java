package com.jsengine;

import org.apache.commons.lang3.StringUtils;

import javax.script.*;
import java.io.StringWriter;

public class ScriptExecutor {

    private final String engineName = "nashorn";
    private StringWriter output = new StringWriter();
    private StringWriter error = new StringWriter();
    private State state;
    private Object result;
    private String script;
    private Thread thread;
    private Runnable task;

    public ScriptExecutor(String script) throws ScriptException {
        if(StringUtils.isBlank(script)){
            throw new IllegalArgumentException("Script string is blank.");
        }
        this.script = script;
        CompiledScript compiledScript = compile(script);
        init(compiledScript);
    }

    private CompiledScript compile(String script) throws ScriptException{
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
        Compilable compilable = (Compilable) scriptEngine;
        return compilable.compile(script);
    }

    private void init(CompiledScript compiledScript) {
        task = () -> {
            ScriptContext context = prepareContext();
            try {
                result = compiledScript.eval(context);
                state = State.EXECUTED;
            } catch (ScriptException e) {
                error.append(e.getMessage());
                state = State.FAILED;
            }
        };
        state = State.NEW;
    }

    private ScriptContext prepareContext() {
        ScriptContext context = new SimpleScriptContext();
        output = new StringWriter();
        error = new StringWriter();
        context.setWriter(output);
        context.setErrorWriter(error);
        return context;
    }

    public void start() {
        if(!state.equals(State.EXECUTION)){
            state = State.EXECUTION;
            thread = new Thread(task);
            thread.start();
        }
    }

    public void stop() {
        if(thread != null && state.equals(State.EXECUTION)){
            thread.stop();
            state = State.STOPPED;
        }
    }

    public String getOutput() {
        return output.toString();
    }

    public String getError() {
        return error.toString();
    }

    public State getState() {
        return state;
    }

    public Object getResult() {
        return result;
    }

    public String getScript() {
        return script;
    }

    public enum State {
        NEW, EXECUTION, STOPPED, EXECUTED, FAILED
    }
}
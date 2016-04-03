package com.jsengine;

import com.jsengine.intf.ExecutionManager;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptException;
import java.util.*;


public class ExecutionManagerImpl implements ExecutionManager<ExecutionContext> {

    private Map<Long, ScriptExecutor> executors = new HashMap<>();

    @Override
    public synchronized long execute(String script) throws ScriptException {
        ScriptExecutor scriptExecutor = new ScriptExecutor(script);
        scriptExecutor.start();
        long executionId = generateExecutionId();
        executors.put(executionId, scriptExecutor);
        return executionId;
    }

    private long generateExecutionId() {
        long result = System.currentTimeMillis();
        while (executors.containsKey(result)){
            result = System.currentTimeMillis();
        }
        return result;
    }

    @Override
    public synchronized List<ExecutionContext> list() {
        List<ExecutionContext> result = new ArrayList<>();
        for (Map.Entry<Long, ScriptExecutor> executorEntry : executors.entrySet()) {
            result.add(createContext(executorEntry.getValue(), executorEntry.getKey()));
        }
        return result;
    }

    @Override
    public synchronized ExecutionContext get(long executionId) {
        return createContext(executors.get(executionId), executionId);
    }

    private ExecutionContext createContext(ScriptExecutor scriptExecutor, long executionId) {
        ExecutionContext result = null;
        if(scriptExecutor != null){
            result = new ExecutionContext();
            Object returnedValue = scriptExecutor.getResult();
            result.setReturnedValue(returnedValue != null ? returnedValue.toString() : null);
            result.setExecutionId(executionId);
            result.setScript(scriptExecutor.getScript());
            result.setStatus(scriptExecutor.getState());
            if(StringUtils.isNotBlank(scriptExecutor.getOutput())){
                result.setOut(scriptExecutor.getOutput());
            }
            if(StringUtils.isNotBlank(scriptExecutor.getError())){
                result.setError(scriptExecutor.getError());
            }
        }
        return result;
    }

    @Override
    public synchronized boolean delete(long executionId) {
        ScriptExecutor executor = executors.get(executionId);
        if(executor != null){
            executor.stop();
            return executors.remove(executionId, executor);
        }
        return false;
    }
}

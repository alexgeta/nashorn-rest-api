package com.jsengine.intf;

import javax.script.ScriptException;
import java.util.List;

public interface ExecutionManager<T> {

    long execute(String script) throws ScriptException;

    List<T> list();

    T get(long executionId);

    boolean delete(long executionId);

}

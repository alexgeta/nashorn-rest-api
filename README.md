# nashorn-rest-api
REST API for Nashorn JavaScript engine

Methods summary:

1. Execute script   
  GET http://localhost:8080/api/execute/{script}    
  POST http://localhost:8080/api/execute
  
    add script to request body and set Content-Type: text/plain   
  To execute script in async mode add 'async' header with value 'true'.  
  For specify execution timeout add 'timeout' header with integer value in seconds, by default is 5.    
  Returns execution context object or errors if any occurred.   
  
2. List execution contexts  
  http://localhost:8080/api/list    
  Returns array of execution context objects.   
3. Get execution context    
  http://localhost:8080/api/get/{executionId}   
  Returns execution context object with specified executionId, or error if context not found.   
4. Delete execution context 
  http://localhost:8080/api/delete/{executionId}    
  Returns true if context deleted, false otherwise. 

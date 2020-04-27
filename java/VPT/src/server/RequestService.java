package server;

import common.networking.AESServerConnection;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class RequestService {
    
    private static final HashMap<AESServerConnection, HashMap<String, LastRequest>> requests = new HashMap<>();
    private static final ReadWriteLock requestLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    private static final Predicate<AESServerConnection> isConnectionClosed = connection -> connection.isClosed();
    private static final Predicate<LastRequest> shouldForgetRequest = request ->
            (System.nanoTime() - request.lastRequestTime) >= (Math.max(3 * request.lastTimeout, ServerConstants.MIN_REQUEST_FORGET_TIME));
    private static final Consumer<HashMap<String, LastRequest>> requestSetProcesser = requestSet -> requestSet.values().removeIf(shouldForgetRequest);
    
    public static void request(AESServerConnection connection, String type, int requestsToEscalate) throws TooManyRequestsException {
        requestLock.readLock().lock();
        try {
            if(!requests.containsKey(connection)) {
                requests.put(connection, new HashMap<>());
            }
            HashMap<String, LastRequest> requestMap = requests.get(connection);
            if(!requestMap.containsKey(type)) {
                requestMap.put(type, new LastRequest());
                return;
            }
            LastRequest lastRequest = requestMap.get(type);
            long remainingTimeout = lastRequest.lastTimeout - (System.nanoTime() - lastRequest.lastRequestTime);
            if(remainingTimeout > 0) {
                throw new TooManyRequestsException(remainingTimeout);
            }
            if(lastRequest.numRequests % requestsToEscalate == 0) {
                long timeout = (int)Math.pow(5, lastRequest.numRequests / requestsToEscalate);
                lastRequest.setLastTimeout(timeout);
                lastRequest.setRequestTime();
                throw new TooManyRequestsException(timeout);
            }
            lastRequest.request();
        } finally {
            requestLock.readLock().unlock();
        }
    }
    
    public static void cleanup() {
        requestLock.writeLock().lock();
        try {
            requests.keySet().removeIf(isConnectionClosed);
            requests.values().forEach(requestSetProcesser);
        } finally {
            requestLock.writeLock().unlock();
        }
    }
    
    public static class TooManyRequestsException extends Exception {

        public final long timeout;
        
        public TooManyRequestsException(long timeout) {
            super("Too Many Requests. Try again in: " + timeout + " nanoseconds");
            this.timeout = timeout;
        }
        
    }
    
    private static final class LastRequest {
        
        private int numRequests;
        private long lastRequestTime;
        private long lastTimeout;

        public LastRequest() {
            this(1, System.nanoTime(), 0);
        }
        
        public LastRequest(int numRequests, long lastRequestTime, long lastTimeout) {
            this.numRequests = numRequests;
            this.lastRequestTime = lastRequestTime;
            this.lastTimeout = lastTimeout;
        }

        public int getNumRequests() {
            return numRequests;
        }

        public long getLastRequestTime() {
            return lastRequestTime;
        }

        public long getLastTimeout() {
            return lastTimeout;
        }

        public void setRequestTime() {
            lastRequestTime = System.nanoTime();
        }

        public void setLastTimeout(long timeout) {
            this.lastTimeout = timeout;
        }
        
        public void request() {
            numRequests++;
            setRequestTime();
        }
        
    }
    
}
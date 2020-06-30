package server;

import common.Utils;
import common.networking.ssl.SSLConnection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Ensures that a client cannot send too many requests
 */
public final class RequestService {
    
    /**
     * Contains a record of all recent requests from a SSLConnection
     */
    private static final HashMap<SSLConnection, HashMap<String, LastRequest>> requests = new HashMap<>();
    /**
     * Protects {@link #requests}
     */
    private static final ReadWriteLock requestLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    /**
     * A Predicate checking if a SSLConnection is closed
     */
    private static final Predicate<SSLConnection> isConnectionClosed = connection -> connection.socket.isClosed();
    /**
     * A predicate representing if enough time has passed to forget a request
     */
    private static final Predicate<LastRequest> shouldForgetRequest = request ->
            (System.nanoTime() - request.getLastRequestTime()) >= (Math.max(3 * request.getLastTimeout(), ServerConstants.MIN_REQUEST_FORGET_TIME));
    private static final Consumer<HashMap<String, LastRequest>> requestSetProcesser = requestSet -> requestSet.values().removeIf(shouldForgetRequest);
    
    /**
     * Logs a request from the client
     * @param connection the SSLConnection which originated the request
     * @param type a string representing the requested resource
     * @param requestsToEscalate the maximum number of requests that can be made before it is determined that too many have been made
     * @throws server.RequestService.TooManyRequestsException if it is determined that the client has sent too many requests
     */
    public static void request(SSLConnection connection, String type, int requestsToEscalate) throws TooManyRequestsException {
        type = type.intern();
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
            long remainingTimeout = lastRequest.getLastTimeout() - (System.nanoTime() - lastRequest.getLastRequestTime());
            if(remainingTimeout > 0) {
                throw new TooManyRequestsException(remainingTimeout);
            }
            if(lastRequest.getNumRequests() % requestsToEscalate == 0) {
                long timeout = Utils.toNanos((long)Math.pow(ServerConstants.TIMEOUT_BASE, lastRequest.getNumRequests() / requestsToEscalate), TimeUnit.SECONDS);
                lastRequest.setLastTimeout(timeout);
                lastRequest.setRequestTime();
                throw new TooManyRequestsException(timeout);
            }
            lastRequest.request();
        } finally {
            requestLock.readLock().unlock();
        }
    }
    
    /**
     * Clears any closed connections and forgets old requests
     */
    public static void cleanup() {
        requestLock.writeLock().lock();
        try {
            requests.keySet().removeIf(isConnectionClosed);
            requests.values().forEach(requestSetProcesser);
        } finally {
            requestLock.writeLock().unlock();
        }
    }
    
    /**
     * Signals that the client has sent too many requests
     */
    public static class TooManyRequestsException extends Exception {

        private static final long serialVersionUID = -2685095853010933068L;

        /**
         * The time (in nanoseconds) until the client can resend the request
         */
        public final long timeout;
        
        /**
         * Creates a new TooManyRequestsException with the specified timeout
         * @param timeout The time until the client can resend the request
         */
        public TooManyRequestsException(long timeout) {
            super("Too Many Requests. Try again in: " + timeout + " nanoseconds");
            this.timeout = timeout;
        }
        
    }
    
    /**
     * Represents a type of request from the client
     */
    private static final class LastRequest {

        /**
         * The number of requests of this type
         */
        private int numRequests;
        /**
         * The timestamp of the previous request or the timestamp when this LastRequest was created
         */
        private long lastRequestTime;
        /**
         * The previous timeout or 0 if none has occurred
         */
        private long lastTimeout;
       
        /**
         * Creates a new LastRequest with 1 request, sets {@link #lastRequestTime} to {@link System#nanoTime()}, and lastTimeout to 0
         */
        LastRequest() {
            this(1, System.nanoTime(), 0);
        }
        
        /**
         * Creates a new LastRequest with the specified information
         * @param numRequests initializes {@link #numRequests}
         * @param lastRequestTime initializes {@link #lastRequestTime}
         * @param lastTimeout initializes {@link #lastTimeout}
         */
        LastRequest(int numRequests, long lastRequestTime, long lastTimeout) {
            this.numRequests = numRequests;
            this.lastRequestTime = lastRequestTime;
            this.lastTimeout = lastTimeout;
        }

        /**
         * Retrieves {@link #numRequests}
         * @return {@link #numRequests}
         */
        public int getNumRequests() {
            return numRequests;
        }

        /**
         * Retrieves {@link #lastRequestTime}
         * @return {@link #lastRequestTime}
         */
        public long getLastRequestTime() {
            return lastRequestTime;
        }

        /**
         * Retrieves {@link #lastTimeout}
         * @return {@link #lastTimeout}
         */
        public long getLastTimeout() {
            return lastTimeout;
        }

        /**
         * Sets {@link #lastRequestTime} to {@link System#nanoTime()}
         */
        public void setRequestTime() {
            lastRequestTime = System.nanoTime();
        }

        /**
         * Sets {@link #lastTimeout}
         * @param timeout the new value for {@link #lastTimeout}
         */
        public void setLastTimeout(long timeout) {
            this.lastTimeout = timeout;
        }
        
        /**
         * Increments {@link #numRequests} and calls {@link #setRequestTime()}
         */
        public void request() {
            numRequests++;
            setRequestTime();
        }
        
    }

    private RequestService() {
    }
    
}
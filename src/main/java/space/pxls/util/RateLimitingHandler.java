package space.pxls.util;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.StatusCodes;
import space.pxls.App;
import space.pxls.user.Role;
import space.pxls.user.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingHandler implements HttpHandler {
    private HttpHandler next;
    private Map<String, RequestBucket> buckets = new ConcurrentHashMap<>();
    private int time;
    private int count;

    public RateLimitingHandler(HttpHandler next, int time, int count) {
        this.next = next;
        this.time = time;
        this.count = count;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String ip = exchange.getAttachment(IPReader.IP);
        Cookie header = exchange.getRequestCookies().get("pxls-token");
        if (header != null) {
            User user = App.getUserManager().getByToken(header.getValue());
            if (user != null && user.getRole().greaterEqual(Role.MODERATOR)) {
                next.handleRequest(exchange);
                return;
            }
        }

        RequestBucket bucket = buckets.<String, RequestBucket>compute(ip, (key, old) -> {
            if (old == null) return new RequestBucket(System.currentTimeMillis(), 0);
            if (old.startTime + time * 1000 < System.currentTimeMillis())
                return new RequestBucket(System.currentTimeMillis(), 0);
            return old;
        });
        bucket.count++;
        if (bucket.count > count)
        {
            int timeSeconds = (int) ((bucket.startTime + time * 1000) - System.currentTimeMillis()) / 1000;
            exchange.setStatusCode(StatusCodes.TOO_MANY_REQUESTS);
            exchange.getResponseSender().send("You are doing that too much, try again in " + timeSeconds / 60 + " minutes");
        } else {
            next.handleRequest(exchange);
        }
    }

    public static class RequestBucket {
        public long startTime;
        public int count;

        public RequestBucket(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}

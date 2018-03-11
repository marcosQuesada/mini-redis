package com.marcosquesada.miniredis.server;

import com.marcosquesada.miniredis.executor.Executor;
import com.marcosquesada.miniredis.service.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static spark.Spark.*;
import static spark.Spark.options;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static Integer EXPIRATOR_WORKER_THREADS = 1;
    public static Integer EXECUTOR_WORKER_THREADS = 1;
    private Redis redis;
    private Executor executor;
    private Integer port;

    public Server(Integer port) {
        this.port = port;
        redis = new Redis(new Executor("expirator", EXPIRATOR_WORKER_THREADS));
        executor = new Executor("executor", EXECUTOR_WORKER_THREADS); //SINGLE THREAD LINEALIZES REQUESTS
    }

    public void start() {
        port(port);


        /**
         curl localhost:8080/?cmd=SET%20mykey%20cool-value
         OK
         curl localhost:8080/?cmd=GET%20mykey
         cool-value
         curl localhost:8080/?cmd=DEL%20mykey
         OK
         curl localhost:8080/?cmd=GET%20mykey
         (nil)

         */
        get("/", (request, response) -> {
            String value = request.queryParams("cmd");
            // clean input string
            value.replaceAll("[^a-zA-Z0-9-_]","");

            String[] parts = value.split(" ");
            if (parts.length == 0) {
                return String.format("(error) ERR unknown command '%s' \n", value);
            }

            List<String> p = new ArrayList<>(Arrays.asList(parts));
            String cmd = p.remove(0);

            try {
                String result = handle(cmd, p.toArray(new String[p.size()]));
                if (result == null || result.equals("")) {
                    return "(nil)\n";
                }
                return result;
            }catch (Exception e) {
                return String.format("(error) ERR %s", e.getMessage());
            }

        });

        /**
         * curl -d "cool-value" -X PUT localhost:8080/mykey
         * OK
         */
        put("/:key", (request, response) -> {
            String key = request.params(":key");
            String rawValue = request.body();

            //parse body by spaces to check if it comes expiration too
            String[] bodyParts = rawValue.split(" ");
            if (bodyParts.length == 2) {
                String value = bodyParts[0];
                Long exp = Long.parseLong(bodyParts[1]);
                logger.info("PUT key {} value {} expiration {}", key, rawValue, exp);

                return executor.executeAndWait(() ->{return format(redis.set(key, value, exp));});
            }

            logger.info("PUT key {} value{}", key, rawValue);
            return executor.executeAndWait(() ->{return format(redis.set(key, rawValue));});
        });

        /**
         * curl -X DELETE localhost:8080/mykey
         * OK
         */
        delete("/:key", (request, response) -> {
            String key = request.params(":key");
            logger.info("DELETE key {}", key);

            return executor.executeAndWait(() ->{return format(redis.del(key));});
        });

        /**
         * curl localhost:8080/mykey
         * Cool-value
         * curl localhost:8080/mykey
         * (nil)
         */
        get("/:key", (request, response) -> {
            String key = request.params(":key");
            logger.info("GET key {}", key);

            return executor.executeAndWait(() ->{return format(redis.get(key));});
        });

    }

    public void terminate() {
        stop();
    }

    public String handle(String cmd, String... args) {
        logger.info("Called CMD {} Arguments {}", cmd, args);

        switch (cmd.toUpperCase()) {
            case Redis.SET:
                if (args.length != 2 && args.length != 3) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }

                String key = args[0];
                String value = args[1];

                if (args.length == 3) {
                    Long exp = Long.parseLong(args[2]);
                    return executor.executeAndWait(() ->{return format(redis.set(key, value, exp));});
                }

                return executor.executeAndWait(() ->{return format(redis.set(key, value));});

            case Redis.GET:
                if (args.length != 1) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.get(args[0]));});

            case Redis.DEL:
                if (args.length != 1) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.del(args[0]));});

            case Redis.DBSIZE:
                if (args.length != 0) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.dbsize());});

            case Redis.INCR:
                if (args.length != 1) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{
                    try {
                        return format(redis.incr(args[0]));
                    }catch (NumberFormatException e) {
                        return "(error) ERR value is not an integer or out of range \n";
                    }
                });

            case Redis.ZADD:
                if (args.length != 3) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                Long score = Long.parseLong(args[1]);
                return executor.executeAndWait(() ->{return format(redis.zadd(args[0], score, args[2]));});

            case Redis.ZCARD:
                if (args.length != 1) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{
                    Integer val = redis.zcard(args[0]);
                    if (val.equals(null)){
                        return format("(nil)");
                    }

                    return format(redis.zcard(args[0]));
                });

            case Redis.ZRANK:
                if (args.length != 2) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.zrank(args[0], args[1]));});

            case Redis.ZRANGE:
                if (args.length != 3) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                Integer start = Integer.parseInt(args[1]);
                Integer stop = Integer.parseInt(args[2]);
                return executor.executeAndWait(() ->{return format(redis.zrange(args[0], start, stop));});

            case Redis.ZREM:
                if (args.length != 2) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.zrem(args[0], args[1]));});

            case Redis.SADD:
                if (args.length != 2) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.sadd(args[0], args[1]));});

            case Redis.SCARD:
                if (args.length != 1) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.scard(args[0]));});

            case Redis.SREM:
                if (args.length != 2) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.srem(args[0], args[1]));});

            case Redis.SINTER:
                if (args.length != 2) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.sinter(args[0], args[1]));});

            case Redis.SMEMBERS:
                if (args.length != 1) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }
                return executor.executeAndWait(() ->{return format(redis.smembers(args[0]));});
            case Redis.FLUSHDB:
                if (args.length != 0) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }

                return executor.executeAndWait(() ->{return format(redis.flushDB());});

            case Redis.KEYS:
                if (args.length != 0) {
                    return String.format("(error) ERR wrong number of arguments for '%s' command", cmd);
                }

                return executor.executeAndWait(() ->{return format(redis.keys());});

            default:
                logger.error("Command {} Not exists", cmd);
                return String.format("(error) ERR unknown command '%s' \n", cmd);
        }
    }

    private String format(String res) {
        return String.format("%s\n", res);
    }

    private String format(Integer res) {
        return String.format("(integer) %d\n", res);
    }

    private String format(Long res) {
        return String.format("%d\n", res);
    }

    private String format(List<String> res) {
        if (res.size() == 0) {
            return "(empty list or set)\n";
        }
        String result = "";
        for (Integer i=0; i< res.size(); i++) {
            result = result + String.format("%d) \"%s\"\n", i, res.get(i));
        }

        return String.format("%s\n", result);
    }
}

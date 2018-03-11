package com.marcosquesada.miniredis.server;

import com.jayway.restassured.RestAssured;
import com.marcosquesada.miniredis.Main;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

public class ServerTest {
    private static Server server = new Server(5555);

    @BeforeClass
    public static void setup() {
        Main.configureLogger();

        RestAssured.baseURI = "http://localhost:5555";
        server.start();
    }

    @Test
    public void EndToEndTestOnStringKeys() {
        given().when().get("/?cmd=FLUSHDB").then().statusCode(200).and().body(containsString("OK"));

        given().when().get("/?cmd=SET mykey fooo").then().statusCode(200).and().body(containsString("OK"));
        given().when().get("/?cmd=GET mykey").then().statusCode(200).and().body(containsString("fooo"));
        given().when().get("/?cmd=DEL mykey").then().statusCode(200).and().body(containsString("OK"));
        given().when().get("/?cmd=GET mykey").then().statusCode(200).and().body(containsString("(nil)"));

        given().when().get("/?cmd=INCR count").then().statusCode(200).and().body(containsString("(integer) 1"));
        given().when().get("/?cmd=INCR count").then().statusCode(200).and().body(containsString("(integer) 2"));

        given().when().get("/?cmd=DBSIZE").then().statusCode(200).and().body(containsString("(integer) 1"));
        given().when().get("/?cmd=KEYS").then().statusCode(200).and().body(containsString("count"));

    }

    @Test
    public void EndToEndTestOnSortedSets() {

        given().when().get("/?cmd=ZADD sskey 1000 fooo").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=ZADD sskey 1 foooX").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=ZADD sskey 10 fooo1").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=ZADD sskey 100 fooo2").then().statusCode(200).and().body(containsString("1"));

        // Check that support duplicated Scores
        given().when().get("/?cmd=ZADD sskey 100 fooo3").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=ZCARD sskey").then().statusCode(200).and().body(containsString("5"));

        given().when().get("/?cmd=ZRANGE sskey 0 1000").then().statusCode(200).and().body(containsString("foooX")).and().body(containsString("fooo1"));
        given().when().get("/?cmd=ZRANK sskey fooo").then().statusCode(200).and().body(containsString("0"));

        given().when().get("/?cmd=ZREM sskey fooo").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=ZCARD sskey").then().statusCode(200).and().body(containsString("4"));

    }

    @Test
    public void EndToEndTestOnSets() {
        given().when().get("/?cmd=SADD skey fooo0").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=SADD skey fooo1").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=SADD skey fooo2").then().statusCode(200).and().body(containsString("1"));
        given().when().get("/?cmd=SCARD skey").then().statusCode(200).and().body(containsString("3"));

        given().when().get("/?cmd=SMEMBERS skey").then().statusCode(200).and().body(containsString("fooo0"))
                .and().body(containsString("fooo1")).and().body(containsString("fooo2"));

    }

    @Test
    public void EndToEndServerErrors() {
        given().when().get("/?cmd=FAKECMD").then().statusCode(200).and().body(containsString("(error) ERR unknown command 'FAKECMD'"));

        given().when().get("/?cmd=SET key1").then().statusCode(200).and().body(containsString("ERR wrong number of arguments"));

        given().when().get("/?cmd=SET key1 fooo").then().statusCode(200).and().body(containsString("OK"));

        given().when().get("/?cmd=INCR key1").then().statusCode(200).and().body(containsString("ERR value is not an integer or out of range"));

    }

    @AfterClass
    public static void tearDown() {
        server.terminate();
    }
}

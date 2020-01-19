package com.frostflames.lighten.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JSONObjectMapperHelperTest {

    @Test
    void testReadValueFromURL() throws Exception {
        // Setup
        final URL src = new URL("https://jsonplaceholder.typicode.com/todos/1");

        // Run the test
        final LinkedHashMap result = JSONObjectMapperHelper.readValueFromURL(src, LinkedHashMap.class);

        // Verify the results
        Assertions.assertEquals("{userId=1, id=1, title=delectus aut autem, completed=false}", result.toString());
    }

    @Test
    void testReadValueFromURL_ThrowsIOException() throws Exception {
        // Setup
        final URL src = new URL("http://example.com/");

        // Run the test
        assertThrows(IOException.class, () -> {
            JSONObjectMapperHelper.readValueFromURL(src, Object.class);
        });
    }

    @Test
    void testReadValueFromJSON() throws Exception {
        // Setup

        // Run the test
        final LinkedHashMap<String, String> result = JSONObjectMapperHelper.readValueFromJSON("{\n" +
                "  \"userId\": 1,\n" +
                "  \"id\": 1,\n" +
                "  \"title\": \"delectus aut autem\",\n" +
                "  \"completed\": false\n" +
                "}", LinkedHashMap.class);

        // Verify the results
        Assertions.assertEquals("{userId=1, id=1, title=delectus aut autem, completed=false}", result.toString());
    }

    @Test
    void testReadValueFromJSON_ThrowsIOException() {
        // Setup

        // Run the test
        assertThrows(IOException.class, () -> {
            JSONObjectMapperHelper.readValueFromJSON("value", Object.class);
        });
    }

    @Test
    void testReadValueFromJSON1() throws Exception {
        // Setup
        // Run the test
        final LinkedHashMap<String, String> result = JSONObjectMapperHelper.readValueFromJSON("{\n" +
                "  \"userId\": 1,\n" +
                "  \"id\": 1,\n" +
                "  \"title\": \"delectus aut autem\",\n" +
                "  \"completed\": false\n" +
                "}", new TypeReference<LinkedHashMap<String, String>>(){});

        // Verify the results
        Assertions.assertEquals("{userId=1, id=1, title=delectus aut autem, completed=false}", result.toString());
    }


}

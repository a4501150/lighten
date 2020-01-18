package com.frostflames.lighten.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class JSONObjectMapperHelper
{

    private static final ObjectMapper objectMapperWithIndentation = initWithIndentation();
    private static final ObjectMapper objectMapperWithoutIndentation = initWithoutIndentation();

    private JSONObjectMapperHelper() {}

    /**
     *
     * This method read value from input stream of url
     *
     * @param src
     * @param valueType
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T readValueFromURL(URL src, Class<T> valueType) throws IOException {
        try (InputStream inputStream = src.openStream()) {
            Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            return objectMapperWithoutIndentation.readValue(reader, valueType);
        }
    }

    /**
     *
     * This method read value from Json string, based on Class passed in
     *
     * @param value
     * @param valueType
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T readValueFromJSON(String value, Class<T> valueType) throws IOException {
        return objectMapperWithoutIndentation.readValue(value, valueType);
    }

    /**
     * This method read value from json str, based on typereference
     *
     * Typereference is jackson's class metadata placeholder, jackson can use this metadata to (de)serialization
     * with json.......
     *
     * One lazy way is to using a wrapper class to include any data structure you want, but remember to include
     * Necessary Annotations / getter / setter of your class.
     * Some times it require @JsonCreator.. @JsonProperty etc ... see google!
     *
     * @param value
     * @param typeReference
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T readValueFromJSON(String value, TypeReference typeReference) throws IOException {
        return objectMapperWithoutIndentation.readValue(value, typeReference);
    }

    /**
     * Convert an object to json, human readable
     *
     * @param value
     * @param <T>
     * @return
     */
    public static <T> String toJSON(T value) {
        return toJSON(objectMapperWithIndentation, value);
    }

    /**
     * Convert an object to  min-size json, human hardly readable
     *
     * @param value
     * @param <T>
     * @return
     */
    public static <T> String toCompactJSON(T value) {
        return toJSON(objectMapperWithoutIndentation, value);
    }


    private static <T> String toJSON(ObjectMapper objectMapper, T valueType) {
        try {
            return objectMapper.writeValueAsString(valueType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Problem serializing " + valueType.getClass();
        }
    }

    private static ObjectMapper initWithIndentation() {
        return new ObjectMapper()
                //.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
    }

    private static ObjectMapper initWithoutIndentation() {
        return new ObjectMapper()
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS)
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                //.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                ;
    }
}

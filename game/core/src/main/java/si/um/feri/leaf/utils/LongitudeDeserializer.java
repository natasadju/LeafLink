package si.um.feri.leaf.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class LongitudeDeserializer extends JsonDeserializer<Double> {
    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Check if "long" field exists and is not null
        if (node.has("long")) {
            return node.get("long").asDouble();
        } else {
            // If "long" field is missing, throw an exception or handle accordingly
            throw new IOException("Field 'long' not found in JSON or invalid.");
        }
    }
}


package org.mule.extension.datasonnet.internal;

import com.datasonnet.document.DefaultDocument;
import com.datasonnet.document.Document;
import com.datasonnet.Mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class MuledatasonnetOperations {

    private static final Logger logger = LoggerFactory.getLogger(MuledatasonnetOperations.class);

    private Object _doTransform(TypedValue<Object> payload, ParameterResolver<TypedValue<Object>> variables, String mappingFile, String mappingScript, String outputMimeType, MuledatasonnetConfiguration config) throws Exception {
            Map varsMap = (Map) variables.resolve().getValue(); //Here we assume variables is a map

            Map<String, Document<?>> jsonnetVars = new HashMap<>();

            String mapping = "{}";

            if (mappingFile != null) {
                //TODO - support URLs like 'file://' and/or 'classpath:'
                InputStream mappingStream = getClass().getClassLoader().getResourceAsStream(mappingFile);
                mapping = IOUtils.toString(mappingStream);
            } else if (mappingScript != null) {
                mapping = mappingScript;
            } else {
                throw new IllegalArgumentException("Either mappingFile or mappingScript attribute must be set");
            }

            Set<Map.Entry> varsSet = varsMap.entrySet();
            for (Map.Entry entry : varsSet) {

                TypedValue value = (TypedValue)entry.getValue();
                String mimeType = value.getDataType().getMediaType().withoutParameters().toString();
                Object unwrappedVar = TypedValue.unwrap(entry.getValue());
                jsonnetVars.put(entry.getKey().toString(), createDocument(unwrappedVar, mimeType));
            }

            String mimeType = payload.getDataType().getMediaType().withoutParameters().toString();
            Document payloadDocument = createDocument(payload.getValue(), mimeType);
            Mapper mapper = new Mapper(mapping, jsonnetVars.keySet(), config.getNamedImports(), true);
            Document mappedDoc = mapper.transform(payloadDocument, jsonnetVars, getDataSonnetMediaType(outputMimeType), Object.class);

            //return mappedDoc.canGetContentsAs(String.class) ? mappedDoc.getContentsAsString() : mappedDoc.getContentsAsObject();
            return mappedDoc.getContent();
        //} catch (Exception e) {
         //   e.printStackTrace();
        //}
        //return "";
    }
    /**
     *
     */
    @MediaType(value = ANY, strict = false)
    public Object transform(@Content(primary = true) TypedValue<Object> payload,
                            @Optional(defaultValue="#[vars]") @Content ParameterResolver<TypedValue<Object>> variables,
                            @Optional String datasonnetFile,
                            @Optional String datasonnetScript,
                            @Optional(defaultValue = "application/json") String responseMimeType,
                            @Config MuledatasonnetConfiguration config) throws Exception {
        return _doTransform(payload, variables, datasonnetFile, datasonnetScript, responseMimeType, config);
    }

    private Document createDocument(Object content, String type) throws Exception {
        String mimeType = type;

        if (mimeType.contains("/xml")) {
            mimeType = "application/xml";
        } else if (mimeType.contains("/csv")) {
            mimeType = "application/csv";
        } else if (mimeType.contains("text/")) {
            mimeType = "text/plain";
        } else if (mimeType.contains("/java")) {
            mimeType = "application/x-java-object";
        } else {
            mimeType = "application/json";
            ObjectMapper jacksonMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = jacksonMapper.readTree(content.toString());
                //This is valid JSON
            } catch (Exception e) {
                //Not a valid JSON, so let's make it an object
                mimeType = "application/x-java-object";
            }
        }

        return new DefaultDocument(content, getDataSonnetMediaType(mimeType));
    }

    private com.datasonnet.document.MediaType getDataSonnetMediaType(String mimeType) {
        String[] typeParts = mimeType.split("/");
        return new com.datasonnet.document.MediaType(typeParts[0], typeParts[1]);
    }

    /*private Document createDocument(Object content, String type) throws JsonProcessingException {
        ObjectMapper jacksonMapper = new ObjectMapper();

        Document document = null;
        boolean isObject = false;
        String mimeType = type;
        String documentContent = content.toString();

        if (mimeType.contains("/xml")) {
            mimeType = "application/xml";
        } else if (mimeType.contains("/csv")) {
            mimeType = "application/csv";
        } else if (mimeType.contains("/java")) {
            mimeType = "application/java";
            isObject = true;
        } else {
            mimeType = "application/json";
            try {
                JsonNode jsonNode = jacksonMapper.readTree(content.toString());
                //This is valid JSON
            } catch (Exception e) {
                //Not a valid JSON, convert
                documentContent = jacksonMapper.writeValueAsString(content);
            }
        }

        document = isObject ? new JavaObjectDocument(content) : new StringDocument(documentContent, mimeType);

        return document;
    }*/
}

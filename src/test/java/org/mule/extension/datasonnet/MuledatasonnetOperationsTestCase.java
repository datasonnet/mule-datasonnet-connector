package org.mule.extension.datasonnet;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.mule.extension.datasonnet.javatest.Gizmo;
import org.mule.extension.datasonnet.javatest.Manufacturer;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;
import org.mule.runtime.api.metadata.MediaType;
import org.skyscreamer.jsonassert.JSONAssert;

import java.text.SimpleDateFormat;
import java.util.Arrays;


public class MuledatasonnetOperationsTestCase extends MuleArtifactFunctionalTestCase {

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     */
    @Override
    protected String getConfigFile() {
        return "test-mule-config.xml";
    }

    @Test
    public void testMappingFlow() throws Exception {
        String payloadJson = loadResourceAsString("simpleMapping_payload.json");
        String resultJson = loadResourceAsString("simpleMapping_result.json");

        String payloadValue = ((String) flowRunner("testMappingFlow")
                .withPayload(payloadJson)
                .withVariable("test", "HelloWorld")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        JSONAssert.assertEquals(resultJson, payloadValue, true);
    }

    @Test
    public void testMappingXMLFlow() throws Exception {
        String payload = loadResourceAsString("payload.xml");
        String resultJson = loadResourceAsString("readXMLExtTest.json");

        String payloadValue = ((String) flowRunner("testMappingXMLFlow")
                .withPayload(payload)
                .withMediaType(MediaType.APPLICATION_XML)
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        JSONAssert.assertEquals(resultJson, payloadValue, true);
    }

    @Test
    public void testMappingScript() throws Exception {
        String payloadJson = loadResourceAsString("simpleMapping_payload.json");
        String resultJson = loadResourceAsString("simpleMapping_result.json");
        String jsonnet = loadResourceAsString("simpleMapping.ds");

        String payloadValue = ((String) flowRunner("testMappingScript")
                .withPayload(payloadJson)
                .withVariable("test", "HelloWorld")
                .withVariable("mappingScript", jsonnet)
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        JSONAssert.assertEquals(resultJson, payloadValue, true);
    }

    @Test
    public void testNamedImports() throws Exception {
        String resultJson = "{\"Lib1JAR\":\"Hello, World\",\"Lib2JAR\":\"Bye, World\",\"Lib3FS\":\"Hello, World : TestLib3\",\"Lib4FS\":\"Bye, World : TestLib4\"}";
        String payloadValue = ((String) flowRunner("testNamedImports")
                .withPayload("{}")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        JSONAssert.assertEquals(resultJson, payloadValue, true);
    }

    @Test
    public void testNamedImportsWitnPath() throws Exception {
        String resultJson = "{\"Lib3FS\":\"Hello, World : TestLib3\",\"Lib4FS\":\"Bye, World : TestLib4\"}";
        String payloadValue = ((String) flowRunner("testNamedImportsWithPath")
                .withPayload("{}")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        JSONAssert.assertEquals(resultJson, payloadValue, true);
    }

    @Test
    public void testReadJava() throws Exception {
        Gizmo theGizmo = new Gizmo();
        theGizmo.setName("gizmo");
        theGizmo.setQuantity(123);
        theGizmo.setInStock(true);
        theGizmo.setColors(Arrays.asList("red","white","blue"));

        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setManufacturerName("ACME Corp.");
        manufacturer.setManufacturerCode("ACME123");
        theGizmo.setManufacturer(manufacturer);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        theGizmo.setDate(df.parse("2020-01-06"));

        String resultJson = loadResourceAsString("javaTest.json");

        String payloadValue = ((String) flowRunner("testReadJava")
                .withPayload(theGizmo)
                .withMediaType(MediaType.APPLICATION_JAVA)
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        JSONAssert.assertEquals(resultJson, payloadValue, true);
    }

    @Test
    public void testWriteJava() throws Exception {
        Gizmo theGizmo = new Gizmo();
        theGizmo.setName("gizmo");
        theGizmo.setQuantity(123);
        theGizmo.setInStock(true);
        theGizmo.setColors(Arrays.asList("red","white","blue"));

        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setManufacturerName("ACME Corp.");
        manufacturer.setManufacturerCode("ACME123");
        theGizmo.setManufacturer(manufacturer);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        theGizmo.setDate(df.parse("2020-01-06"));

        String payload = loadResourceAsString("javaTest.json");

        Object payloadValue = flowRunner("testWriteJava")
                .withPayload(payload)
                .withMediaType(MediaType.APPLICATION_JSON)
                .run()
                .getMessage()
                .getPayload()
                .getValue();

        assertEquals(theGizmo, payloadValue);
    }
}

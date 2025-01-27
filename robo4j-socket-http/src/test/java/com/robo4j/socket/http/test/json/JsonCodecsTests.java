/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.socket.http.test.json;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.test.codec.*;
import com.robo4j.socket.http.test.units.config.codec.NSBETypesAndCollectionTestMessage;
import com.robo4j.socket.http.test.units.config.codec.NSBETypesTestMessage;
import com.robo4j.socket.http.test.units.config.codec.NSBWithSimpleCollectionsTypesMessage;
import com.robo4j.socket.http.test.units.config.codec.TestPerson;
import com.robo4j.socket.http.test.units.config.enums.TestCommandEnum;
import com.robo4j.util.StreamUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonCodecsTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonCodecsTests.class);

    private static String testJson = "{\"number\":42,\"message\":\"no message\",\"active\":false,"
            + "\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"},"
            + "\"persons\":[{\"name\":\"name1\",\"value\":22,\"child\":{\"name\":\"name11\",\"value\":0,"
            + "\"child\":{\"name\":\"name111\",\"value\":42}}},{\"name\":\"name2\",\"value\":5}],"
            + "\"personMap\":{\"person1\":{\"name\":\"name1\",\"value\":22,\"child\":{\"name\":\"name11\",\"value\":0,"
            + "\"child\":{\"name\":\"name111\",\"value\":42}}},\"person2\":{\"name\":\"name2\",\"value\":5}}}";

    private NSBWithSimpleCollectionsTypesMessageCodec collectionsTypesMessageCodec;
    private NSBETypesTestMessageCodec enumTypesMessageCodec;
    private NSBETypesAndCollectionTestMessageCodec collectionEnumTypesMessageCodec;
    private CameraMessageCodec cameraMessageCodec;
    private HttpPathDTOCodec httpPathDTOCodec;

    @BeforeEach
    void setUp() {
        collectionsTypesMessageCodec = new NSBWithSimpleCollectionsTypesMessageCodec();
        enumTypesMessageCodec = new NSBETypesTestMessageCodec();
        collectionEnumTypesMessageCodec = new NSBETypesAndCollectionTestMessageCodec();
        cameraMessageCodec = new CameraMessageCodec();
        httpPathDTOCodec = new HttpPathDTOCodec();
    }

    @SuppressWarnings("unchecked")
    @Test
    void encodeServerPathDTOMessageNoFilterTest() {
        // String expectedJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"}";
        HttpPathMethodDTO message = new HttpPathMethodDTO();
        message.setRoboUnit("roboUnit1");
        message.setMethod(HttpMethod.GET);
        message.setCallbacks(Collections.EMPTY_LIST);

        String resultJson = httpPathDTOCodec.encode(message);
        HttpPathMethodDTO decodedMessage = httpPathDTOCodec.decode(resultJson);

        printJson(resultJson);
        printDecodedMessage(decodedMessage);

        // Assert.assertTrue(expectedJson.equals(resultJson));
        assertEquals(message, decodedMessage);
    }

    @Test
    void encodeMessageWithEnumTypeTest() {
        String expectedJson = "{\"number\":42,\"message\":\"enum type 1\",\"active\":true,\"command\":\"MOVE\"}";
        NSBETypesTestMessage message = new NSBETypesTestMessage();
        message.setNumber(42);
        message.setMessage("enum type 1");
        message.setActive(true);
        message.setCommand(TestCommandEnum.MOVE);

        String resultJson = enumTypesMessageCodec.encode(message);
        NSBETypesTestMessage decodedMessage = enumTypesMessageCodec.decode(resultJson);

        printJson(resultJson);
        printDecodedMessage(decodedMessage);
        assertNotNull(resultJson);
        assertEquals(expectedJson, resultJson);
        assertEquals(message, decodedMessage);
    }

    @Test
    void encodeMessageWithEnumCollectionTypeTest() {

        String expectedJson = "{\"number\":42,\"message\":\"enum type 1\",\"active\":true,\"command\":\"MOVE\",\"commands\":[\"MOVE\",\"STOP\",\"BACK\"]}";
        NSBETypesAndCollectionTestMessage message = new NSBETypesAndCollectionTestMessage();
        message.setNumber(42);
        message.setMessage("enum type 1");
        message.setActive(true);
        message.setCommand(TestCommandEnum.MOVE);
        message.setCommands(Arrays.asList(TestCommandEnum.MOVE, TestCommandEnum.STOP, TestCommandEnum.BACK));

        String resultJson = collectionEnumTypesMessageCodec.encode(message);
        NSBETypesAndCollectionTestMessage decodedMessage = collectionEnumTypesMessageCodec.decode(expectedJson);


        printJson(resultJson);
        printDecodedMessage(decodedMessage);
        assertNotNull(resultJson);
        assertEquals(expectedJson, resultJson);
        assertEquals(message, decodedMessage);
    }

    @Test
    void nestedObjectToJson() {

        TestPerson testPerson2 = new TestPerson();
        testPerson2.setName("name2");
        testPerson2.setValue(5);

        TestPerson testPerson111 = new TestPerson();
        testPerson111.setName("name111");
        testPerson111.setValue(42);

        TestPerson testPerson11 = new TestPerson();
        testPerson11.setName("name11");
        testPerson11.setValue(0);
        testPerson11.setChild(testPerson111);

        TestPerson testPerson1 = new TestPerson();
        testPerson1.setName("name1");
        testPerson1.setValue(22);
        testPerson1.setChild(testPerson11);

        Map<String, TestPerson> personMap = new LinkedHashMap<>();
        personMap.put("person1", testPerson1);
        personMap.put("person2", testPerson2);

        NSBWithSimpleCollectionsTypesMessage obj1 = new NSBWithSimpleCollectionsTypesMessage();
        obj1.setNumber(42);
        obj1.setMessage("no message");
        obj1.setActive(false);
        obj1.setArray(new String[]{"one", "two"});
        obj1.setList(Arrays.asList("text1", "text2"));
        obj1.setMap(Collections.singletonMap("key", "value"));
        obj1.setPersons(Arrays.asList(testPerson1, testPerson2));
        obj1.setPersonMap(personMap);

        long start = System.nanoTime();
        String json = collectionsTypesMessageCodec.encode(obj1);
        TimeUtils.printTimeDiffNano("decodeFromJson", start);
        printJson(json);

        assertEquals(testJson, json);

    }

    @Test
    void nestedJsonToObject() {

        Map<String, TestPerson> personMap = getStringTestPersonMap();

        long start = System.nanoTime();
        NSBWithSimpleCollectionsTypesMessage obj1 = collectionsTypesMessageCodec.decode(testJson);
        TimeUtils.printTimeDiffNano("decodeFromJson", start);

        LOGGER.info("Obj: {}", obj1);

        assertEquals(Integer.valueOf(42), obj1.getNumber());
        assertEquals("no message", obj1.getMessage());
        assertFalse(obj1.getActive());
        assertArrayEquals(new String[]{"one", "two"}, obj1.getArray());
        assertTrue(obj1.getList().containsAll(Arrays.asList("text1", "text2")));
        assertEquals(personMap, obj1.getPersonMap());
    }

    @Test
    void cameraCodecJsonCycleTest() {

        final byte[] imageBytes = StreamUtils.inputStreamToByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("snapshot.png"));
        String encodeString = new String(Base64.getEncoder().encode(imageBytes));

        CameraMessage cameraMessage = new CameraMessage();
        cameraMessage.setImage(encodeString);
        cameraMessage.setType("jpg");
        cameraMessage.setValue("22");

        long start = System.nanoTime();
        String cameraJson0 = cameraMessageCodec.encode(cameraMessage);
        TimeUtils.printTimeDiffNano("cameraJson0", start);

        start = System.nanoTime();
        cameraMessageCodec.decode(cameraJson0);
        TimeUtils.printTimeDiffNano("decodeCameraMessage0", start);

        start = System.nanoTime();
        String cameraJson = cameraMessageCodec.encode(cameraMessage);
        TimeUtils.printTimeDiffNano("decodeFromJson", start);
        printJson(cameraJson);

        start = System.nanoTime();
        CameraMessage codecCameraMessage = cameraMessageCodec.decode(cameraJson);
        TimeUtils.printTimeDiffNano("decodeFromJson", start);

        assertEquals(cameraMessage, codecCameraMessage);

    }

    private static <T> void printDecodedMessage(T decodedMessage) {
        LOGGER.debug("decodedMessage:{}", decodedMessage);
    }

    private static Map<String, TestPerson> getStringTestPersonMap() {
        TestPerson testPerson2 = new TestPerson();
        testPerson2.setName("name2");
        testPerson2.setValue(5);

        TestPerson testPerson111 = new TestPerson();
        testPerson111.setName("name111");
        testPerson111.setValue(42);

        TestPerson testPerson11 = new TestPerson();
        testPerson11.setName("name11");
        testPerson11.setValue(0);
        testPerson11.setChild(testPerson111);

        TestPerson testPerson1 = new TestPerson();
        testPerson1.setName("name1");
        testPerson1.setValue(22);
        testPerson1.setChild(testPerson11);

        Map<String, TestPerson> personMap = new LinkedHashMap<>();
        personMap.put("person1", testPerson1);
        personMap.put("person2", testPerson2);
        return personMap;
    }

    private static void printJson(String resultJson) {
        LOGGER.debug("json:{}", resultJson);
    }

}

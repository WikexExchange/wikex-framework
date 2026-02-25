package com.xxl.job.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * JDK serialization utility
 * 
 * @author william 2020-04-12 0:14:00
 */
public class JdkSerializeTool {
    private static Logger logger = LoggerFactory.getLogger(JdkSerializeTool.class);

    // ------------------------ serialize and deserialize ------------------------

    /**
     * Convert object to byte[] (Because jedis does not support storing objects directly, 
     * so convert to byte[] to store)
     *
     * @param object the object to serialize
     * @return byte array representing the serialized object
     */
    public static byte[] serialize(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            // Serialization
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (oos != null) oos.close();
                if (baos != null) baos.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Convert byte[] back to Object
     *
     * @param bytes the byte array to deserialize
     * @param clazz the target class type (not used in implementation but kept for API compatibility)
     * @return deserialized Object
     */
    public static <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream bais = null;
        try {
            // Deserialization
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bais != null) bais.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

}

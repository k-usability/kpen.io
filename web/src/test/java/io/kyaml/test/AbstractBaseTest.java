package io.kyaml.test;

import org.projectodd.yaml.*;
import java.io.InputStream;

public abstract class AbstractBaseTest {

    public abstract String getType();

    public InputStream loadResource(String resource) throws SchemaException {
        String path = TestUtils.join( new String[] { getType(), resource }, "/" );
        System.out.println("PATH: " + path);
        return this.getClass().getResourceAsStream( '/' + path );
    }

}

package io.kyaml.test;

import org.junit.Test;
import org.projectodd.yaml.*;
import org.projectodd.yaml.schema.types.AbstractBaseType;
import org.projectodd.yaml.schema.types.BooleanType;
import org.projectodd.yaml.schema.types.MapType;

import java.util.Map;

import static org.junit.Assert.*;


public class KYamlTest extends AbstractBaseTest {
/*
    @Test
    public void testBasic() throws SchemaException {
        Schema schema = new Schema( loadResource( "basic-schema.yml" ) );
        schema.validate( loadResource( "valid-doc.yml" ) );
    }*/

    @Test
    public void testFull() throws SchemaException {
        Schema schema = new Schema( loadResource( "full-schema.yml" ) );
        schema.validate( loadResource( "full-valid-doc.yml" ) );
    }

    @Override
    public String getType() {
        return "kyaml";
    }

}

package io.kyaml;

import io.kyaml.model.KYaml;

import java.util.HashMap;

public class Translate {
    private KYaml ky;

    public Translate(KYaml kyaml) {
        this.ky = kyaml;
    }

    public HashMap<String,Object> toCellMap() {
        HashMap<String,Object> m = new HashMap();

        return m;
    }
}

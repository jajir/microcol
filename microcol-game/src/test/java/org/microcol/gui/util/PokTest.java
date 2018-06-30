package org.microcol.gui.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.Gson;

public class PokTest {

    private final static String t1 = "{\"name\":\"kocka\",\"data\":\"pes\",\"age\":000}";

    @Test
    public void test_save() throws Exception {

        Bean2 b2 = new Bean2();
        b2.setOsel(33);
        b2.setLetadlo("123");

        Bean1 b = new Bean1();
        b.setAge(23);
        b.setName("testik");
        b.setData(b2);

        Gson gson = new Gson();

        String out = gson.toJson(b);

        System.out.println(t1);
        System.out.println(out);
        
        Bean1 b3 = gson.fromJson(out, Bean1.class);
        System.out.println(b3.getData().getClass());
    }

}

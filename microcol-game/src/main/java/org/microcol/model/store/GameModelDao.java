package org.microcol.model.store;

import com.google.gson.annotations.SerializedName;

public class GameModelDao {

    public GameModelDao() {
    }

    @SerializedName("game_name")
    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}

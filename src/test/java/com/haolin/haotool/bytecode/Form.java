package com.haolin.haotool.bytecode;

import java.util.ArrayList;
import java.util.List;

public class Form {
    public String sayHello(String name) {
        String result = String.format("Hello %s, I'm in 'dubbo-15-dubbo-wrapper' project.", name);
        System.out.println(result);
        return result;
    }

    private String name ="";
    private String data ="";
    private List<String> types = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

}


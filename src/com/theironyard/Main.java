package com.theironyard;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    static ArrayList<Person> people = new ArrayList<>();
    static HashMap<String, Person> peopleMap = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        readFile("people.csv");
        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    int index = 0;
                    String offsetStr = request.queryParams("offset");
                    HashMap m = new HashMap<>();
                    if (offsetStr != null) {
                        index += Integer.valueOf(offsetStr);
                    }
                    int offset = 20 + index;
                    ArrayList<Person> peopleChunk = new ArrayList<>(people.subList(index, (offset > people.size()?people.size():offset)));
                    m.put("end", offset >= people.size());
                    m.put("beginning", index == 0);
                    m.put("previousOffset", offset - 40);
                    m.put("nextOffset", offset);
                    m.put("people", peopleChunk);
                    return new ModelAndView(m, "home.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/view-person",
                ((request, response) -> {
                    String selectedPerson = request.queryParams("selectedPerson");
                    return new ModelAndView(peopleMap.get(selectedPerson), "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }
        public static void readFile(String file) throws FileNotFoundException {
            File f = new File(file);
            Scanner scanner = new Scanner(f);
            scanner.nextLine();
            while (scanner.hasNext()){
                String[] personInfo = scanner.nextLine().split(",");
                Person person = new Person(Integer.valueOf(personInfo[0]), personInfo[1], personInfo[2],personInfo[3], personInfo[4], personInfo[5]);
                people.add(person);
                peopleMap.put(person.toString(), person);
        }
    }
}


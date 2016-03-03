package com.theironyard;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;

public class Main {
    public static Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:");
        return conn;
    }
    public void endConnection(Connection conn) throws SQLException {
        conn.close();
    }
    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS people (id IDENTITY, first_name VARCHAR, last_name VARCHAR, email VARCHAR, country VARCHAR, ip VARCHAR)");
    }
    public static void dropTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        conn.close();
    }
    public static void insertPerson(Connection conn, String firstName, String lastName, String email, String country, String ip) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, country);
        stmt.setString(5, ip);
        stmt.execute();
    }
    public static Person selectPerson(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()){
            Person person = new Person(results.getInt("id"), results.getString("first_name"), results.getString("last_name"), results.getString("email"), results.getString("country"), results.getString("ip"));
            return person;
        }
       return null;
    }
    public static ArrayList<Person> selectPeople(Connection conn, int offset) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people ORDER BY last_name, first_name LIMIT 20 OFFSET ?");
        stmt.setInt(1, offset);
        ResultSet results = stmt.executeQuery();
        ArrayList<Person> people = new ArrayList<>();
        while(results.next()){
            people.add(new Person(results.getInt("id"), results.getString("first_name"), results.getString("last_name"), results.getString("email"), results.getString("country"), results.getString("ip")));
        }
        return people;
    }
    public static int peopleSize(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(id) AS size FROM people");
        ResultSet results = stmt.executeQuery();
        results.next();
        return results.getInt("size");
    }
    public static void populateDatabase(Connection conn, String file) throws FileNotFoundException, SQLException {
        File f = new File(file);
        Scanner scanner = new Scanner(f);
        scanner.nextLine();
        //ArrayList<Person> people = new ArrayList<>();
        while (scanner.hasNext()) {
            String[] personInfo = scanner.nextLine().split(",");
            Person person = new Person(Integer.valueOf(personInfo[0]), personInfo[1], personInfo[2], personInfo[3], personInfo[4], personInfo[5]);
//                int index = 0;
//                for (Person sortedPerson: people){
//                    if(person.compareTo(sortedPerson)>0){
//                        index+=1;
//                    }else{
//                        break;
//                    }
//                }
//                people.add(index, person);
//            }
//            for(Person guy: people){
//                insertPerson(conn, guy.getFirstName(), guy.getLastName(), guy.getEmail(), guy.getCountry(), guy.getIp());
//            }
            insertPerson(conn, person.getFirstName(), person.getLastName(), person.getEmail(), person.getCountry(), person.getIp());
        }
    }

    public static void main(String[] args) throws FileNotFoundException, SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:");
        createTables(conn);
        populateDatabase(conn,"people.csv");
        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    int index = 0;
                    String offsetStr = request.queryParams("offset");
                    HashMap m = new HashMap<>();
                    if (offsetStr != null) index += Integer.valueOf(offsetStr);
                    ArrayList<Person> peopleChunk = selectPeople(conn, (index > peopleSize(conn)?peopleSize(conn):index));
                    m.put("end", index + 20 >= peopleSize(conn));
                    m.put("beginning", index == 0);
                    m.put("previousOffset", index - 20);
                    m.put("nextOffset", index + 20);
                    m.put("people", peopleChunk);
                    return new ModelAndView(m, "home.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/view-person",
                ((request, response) -> {
                    String selectedPersonStr = request.queryParams("selectedPerson");
                    int selectedPerson = Integer.valueOf(selectedPersonStr);
                    return new ModelAndView(selectPerson(conn, selectedPerson), "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }
}


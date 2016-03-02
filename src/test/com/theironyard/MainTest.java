package com.theironyard;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by PiratePowWow on 3/2/16.
 */
public class MainTest {

    @org.junit.Test
    public void testInsertAndSelectPerson() throws Exception {
        Connection conn = Main.startConnection();
        Main.createTables(conn);
        Main.insertPerson(conn, "Jim", "Smith", "wiggle@fart.pee", "Chad", "13098123");
        Person person = Main.selectPerson(conn, 1);
        Main.dropTables(conn);
        conn.close();
        assertTrue(person.firstName.equals("Jim"));
    }
    @Test
    public void testSelectPeople() throws SQLException {
        Connection conn = Main.startConnection();
        Main.createTables(conn);
        Main.insertPerson(conn, "Jim", "Smith", "wiggle@fart.pee", "Chad", "13098123");
        Main.insertPerson(conn, "James", "Smith", "wiggle@fart.pee", "Chad", "13098123");
        ArrayList<Person> people = Main.selectPeople(conn, 0);
        Main.dropTables(conn);
        conn.close();
        assertTrue(people.size()==2);
    }
    @Test
    public void testPeopleSize() throws SQLException {
        Connection conn = Main.startConnection();
        Main.createTables(conn);
        Main.insertPerson(conn, "Jim", "Smith", "wiggle@fart.pee", "Chad", "13098123");
        Main.insertPerson(conn, "James", "Smith", "wiggle@fart.pee", "Chad", "13098123");
        int count = Main.peopleSize(conn);
        Main.dropTables(conn);
        conn.close();
        assertTrue(count==2);
    }

}
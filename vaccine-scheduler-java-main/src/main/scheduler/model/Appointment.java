package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Appointment {

    private final int ID;
    private final String pname;
    private final String vname;
    private final String ctime;
    private final String cname;

    private Appointment(AppointmentBuilder builder) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        int id = 0;
        try {
            System.out.println("h15");
            String setID = "SELECT IDENT_CURRENT ('Appointments') AS ID;";
            PreparedStatement statement = con.prepareStatement(setID);
            System.out.println("h18");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) { // Move cursor to the first row
                id = resultSet.getInt("ID");
            }
            System.out.println("h17");
        } catch (SQLException e) {
            System.out.println("h16");
            e.printStackTrace();
        }
        this.ID = id;
        this.pname = builder.pname;
        this.vname = builder.vname;
        this.ctime = builder.ctime;
        this.cname = builder.cname;
    }

    public int getID() { return ID; }
    public String getUsername() {
        return pname;
    }
    public String getVaccine() {
        return vname;
    }
    public String getDate() {
        return ctime;
    }
    public String getCaregiver() {
        return cname;
    }

    public void saveToDB() throws SQLException {
        System.out.println("h8");
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAppointment = "INSERT INTO Appointments VALUES (?, ?, ?, ?);";
        try {
            System.out.println("h9");
            PreparedStatement statement = con.prepareStatement(addAppointment);
            statement.setString(1, pname);
            statement.setString(2, vname);
            statement.setString(3, ctime);
            statement.setString(4, cname);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("h10");
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class AppointmentBuilder {
        private final String pname;
        private final String vname;
        private final String ctime;
        private final String cname;

        public AppointmentBuilder(String pname, String name, String date, String cname) {
            this.pname = pname;
            this.vname = name;
            this.ctime = date;
            this.cname = cname;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }
}
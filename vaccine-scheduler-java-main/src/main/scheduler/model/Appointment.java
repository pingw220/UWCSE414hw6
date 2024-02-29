package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Appointment {

    private int ID;
    private final String pname;
    private final String vname;
    private final String ctime;
    private final String cname;

    private Appointment(AppointmentBuilder builder) {
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
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAppointment = "INSERT INTO Appointments VALUES (?, ?, ?, ?);";
        try {
            PreparedStatement statement = con.prepareStatement(addAppointment);
            statement.setString(1, pname);
            statement.setString(2, vname);
            statement.setString(3, ctime);
            statement.setString(4, cname);
            statement.executeUpdate();
            try {
                String setID = "SELECT IDENT_CURRENT ('Appointments') AS ID;";
                statement = con.prepareStatement(setID);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) { // Move cursor to the first row
                    ID = resultSet.getInt("ID");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
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
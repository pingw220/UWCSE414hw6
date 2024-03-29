package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Appointment;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("*** Please enter one of the following commands ***");
            System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
            System.out.println("> create_caregiver <username> <password>");
            System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
            System.out.println("> login_caregiver <username> <password>");
            System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
            System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
            System.out.println("> upload_availability <date>");
            System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
            System.out.println("> add_doses <vaccine> <number>");
            System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
            System.out.println("> logout");  // TODO: implement logout (Part 2)
            System.out.println("> quit");
            System.out.println();
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // TODO: Part 1
        // create_patient <username> <password>
        // check: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Create patient failed");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        // check: check if the username has been taken already
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }

        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);

        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create patient failed");
            e.printStackTrace();
        }
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Create caregiver failed");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }

        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build(); 
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create caregiver failed");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        // check: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login patient failed");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        // check if the username exist
        if (!usernameExistsPatient(username)) {
            System.out.println("Login patient failed");
            return;
        }

        // login_caregiver <username> <password>
        // check: if someone's already logged-in, they need to log out first
        if (currentPatient != null || currentPatient != null) {
            System.out.println("User already logged in, try again");
            return;
        }

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login patient failed");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login patient failed");
        } else {
            System.out.println("Logged in as " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>

        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        if (!usernameExistsCaregiver(username)) {
            System.out.println("Login failed.");
            return;
        }

        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        String date = tokens[1];

        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getCaregiver = "SELECT Username FROM Availabilities WHERE Time = ? AND availability = 1 ORDER BY Username ASC;";
        String getVaccine = "SELECT Name, Doses FROM Vaccines;";
        try {
            PreparedStatement statement = con.prepareStatement(getCaregiver);
            statement.setString(1, date);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String Name = resultSet.getString("Username");
                System.out.println(Name);
            }
            statement = con.prepareStatement(getVaccine);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String Name = resultSet.getString("Name");
                int Doses = resultSet.getInt("Doses");
                System.out.println(Name + " " + Doses);
            }
        } catch (SQLException e) {
            System.out.println("Please try again");
            e.printStackTrace();
        }
    }

    private static void reserve(String[] tokens) {
        // TODO: Part 2

        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first");
            return;
        }

        if (currentCaregiver != null) {
            System.out.println("Please login as a patient");
            return;
        }

        if (tokens.length != 3) {
            System.out.println("Please try again");
            return;
        }

        String date = tokens[1];
        String name = tokens[2];

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getAvailability = "SELECT Username FROM Availabilities WHERE Time = ? AND availability = 1;";

        try {
            PreparedStatement statement = con.prepareStatement(getAvailability);
            statement.setString(1, date);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("No caregiver is available");
                return;
            }
            String cname = resultSet.getString("Username");

            String getVaccine = "SELECT Doses FROM Vaccines WHERE Name = ?;";
            statement = con.prepareStatement(getVaccine);
            statement.setString(1, name);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt("Doses");
                if (count == 0) {
                    System.out.println("Not enough available doses");
                } else {
                    String pname = currentPatient.getUsername();
                    Appointment appointment = new Appointment.AppointmentBuilder(pname, name, date, cname).build();
                    appointment.saveToDB();
                    int ID = appointment.getID();
                    String getAppointment = "SELECT ID FROM Appointments WHERE ID = ?;";
                    statement = con.prepareStatement(getAppointment);
                    statement.setInt(1, ID);
                    statement.executeQuery();
                    System.out.println("Appointment ID " + ID + ", Caregiver username " + cname);

                    Vaccine vaccine = null;
                    try {
                        vaccine = new Vaccine.VaccineGetter(name).get();
                    } catch (SQLException e) {
                        System.out.println("Please try again");
                        e.printStackTrace();
                    }
                    try {
                        vaccine.decreaseAvailableDoses(1);
                    } catch (SQLException e) {
                        System.out.println("Please try again");
                        e.printStackTrace();
                    }

                    String deleteAvailability = "UPDATE Availabilities SET availability = 0 WHERE Username = ? AND Time = ?;";
                    try {
                        statement = con.prepareStatement(deleteAvailability);
                        statement.setString(1, cname);
                        Date d = Date.valueOf(date);
                        statement.setDate(2, d);
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Please try again");
            }
        } catch (SQLException e) {
            System.out.println("Please try again");
            e.printStackTrace();
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getCaregiver = "SELECT * FROM Availabilities WHERE Username = ? AND Time = ?;";
        try {
            PreparedStatement statement = con.prepareStatement(getCaregiver);
            statement.setString(1, currentCaregiver.getUsername());
            statement.setString(2, date);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                System.out.println("Availability already uploaded!");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Please try again");
            e.printStackTrace();
        }

        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit

        if(tokens.length != 2) {
            System.out.println("Please try again");
            return;
        }

        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        int ID = Integer.parseInt(tokens[1]);

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getAppointment = "SELECT vname, cname, ctime FROM Appointments WHERE ID = ? AND (cname = ? OR pname = ?);";
        PreparedStatement statement;
        String username = "", vname = "";
        Date date;
        try {
            statement = con.prepareStatement(getAppointment);
            statement.setInt(1, ID);
            if(currentCaregiver == null) {
                statement.setString(3, currentPatient.getUsername());
                statement.setString(2, currentPatient.getUsername());
            } else {
                statement.setString(2, currentCaregiver.getUsername());
                statement.setString(3, currentCaregiver.getUsername());
            }
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                username = resultSet.getString("cname");
                date = resultSet.getDate("ctime");
                vname = resultSet.getString("vname");
            } else {
                System.out.println("Wrong User");
                return;
            }

            Vaccine vaccine = new Vaccine.VaccineGetter(vname).get();
            vaccine.increaseAvailableDoses(1);

            String deleteAvailability = "UPDATE Availabilities SET availability = 1 WHERE Username = ? AND Time = ?;";
            statement = con.prepareStatement(deleteAvailability);
            statement.setString(1, username);
            statement.setDate(2, date);
            statement.executeUpdate();

            getAppointment = "DELETE FROM Appointments WHERE ID = ?;";
            statement = con.prepareStatement(getAppointment);
            statement.setInt(1, ID);
            statement.executeUpdate();
            System.out.println("Cancel Successfully");
        } catch (SQLException e) {
            System.out.println("Please try again");
            e.printStackTrace();
        }
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2

        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getAppointment = "SELECT ID, pname, vname, ctime, cname FROM Appointments;";
        try {
            PreparedStatement statement = con.prepareStatement(getAppointment);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int ID = resultSet.getInt("ID");
                String pname = resultSet.getString("pname");
                String vname = resultSet.getString("vname");
                String ctime = resultSet.getString("ctime");
                String cname = resultSet.getString("cname");
                if (currentCaregiver != null) {
                    System.out.println(ID + " " + vname + " " + ctime + " " + cname);
                } else {
                    System.out.println(ID + " " + vname + " " + ctime + " " + pname);
                }
            }
        } catch (SQLException e) {
            System.out.println("Please try again");
            e.printStackTrace();
        }
    }

    private static void logout(String[] tokens) {
        // TODO: Part 2
        if(currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
        } else {
            currentCaregiver = null;
            currentPatient = null;
            System.out.println("Successfully logged out");
        }
    }
}

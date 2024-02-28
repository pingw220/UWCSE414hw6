CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Patients (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Appointments (
    ID int IDENTITY(1, 1),
    pname varchar(255) REFERENCES Patients(Username),
    vname varchar(255) REFERENCES Vaccines(Name),
    ctime date,
    cname varchar(255),
    FOREIGN KEY (ctime, cname) REFERENCES Availabilities(Time, Username)
);
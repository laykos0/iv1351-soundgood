/*
 * The MIT License (MIT)
 * Copyright (c) 2020 Leif Lindbäck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so,subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package soundgood.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import soundgood.model.Instrument;

/**
 * This data access object (DAO) encapsulates all database calls in the bank
 * application. No code outside this class shall have any knowledge about the
 * database.
 */
public class SoundgoodDAO {
    private static final String INSTRUMENT_TABLE_NAME = "instrument";
    private static final String INSTRUMENT_PK_COLUMN_NAME = "instrument_id";
    private static final String INSTRUMENT_TYPE_COLUMN_NAME = "instrument_type";
    private static final String INSTRUMENT_BRAND_COLUMN_NAME = "brand";
    private static final String INSTRUMENT_PRICE_COLUMN_NAME = "price";

    private static final String RENTAL_TABLE_NAME = "rental";
    private static final String RENTAL_PK_COLUMN_NAME = "rental_id";
    private static final String RENTAL_TIME_COULMN_NAME = "time_rented";
    private static final String RENTAL_TERMINATED_COLUMN_NAME = "rental_terminated";
    private static final String RENTAL_FK_STUDENT_COLUMN_NAME = "student_id";
    private static final String RENTAL_FK_INSTRUMENT_COLUMN_NAME = INSTRUMENT_PK_COLUMN_NAME;


    private Connection connection;
    private PreparedStatement createRentalStmt;
    private PreparedStatement deleteRentalStmt;
    private PreparedStatement findStudentsStmt;
    private PreparedStatement findInstrumentsStmt; 
    private PreparedStatement findInstrumentsTypeStmt; 
    private PreparedStatement findInstrumentsLockingForUpdateStmt; 


    /**
     * Constructs a new DAO object connected to the soundgood database.
     */
    public SoundgoodDAO() throws SoundgoodDBException {
        try {
            connectToSoundgoodDB();
            prepareStatements();
        } catch (ClassNotFoundException | SQLException exception) {
            throw new SoundgoodDBException("Could not connect to datasource.", exception);
        }
    }

/**
     * Creates a new rental.
     *
     * @param student_id The student renting the instrument.
     * @param instrument_id The instrument to rent.
     * @throws SoundgoodDBException If failed to create rental.
     */
    public void createRental(Integer student_id, Integer instrument_id) throws SoundgoodDBException {
        String failureMsg =  "Could not create rental for student_id: " + student_id + " and instrument: " + instrument_id + ".";
        try {
            createRentalStmt.setInt(1, student_id);
            createRentalStmt.setInt(2, instrument_id);
            int updatedRows = createRentalStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Terminates renntal with specified ID.
     *
     * @param rentalID The rental to terminate.
     * @throws SoundgoodDBException If unable to terminate the specified rental.
     */
    public void deleteRental(int rentalID) throws SoundgoodDBException {
        String failureMsg = "Could not terminate rental: " + rentalID;
        try {
            deleteRentalStmt.setInt(1, rentalID);
            int updatedRows = deleteRentalStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Retrieves all students who are not allowed to rent more instruments.
     *
     * @return A list with all students currently renting two instruments. The list 
     *          is empty if there are no such students.
     * @throws SoundgoodDBException If failed to search for available instruments.
     */
    public List<Integer> findStudents() throws SoundgoodDBException {
        String failureMsg = "Could not list students.";
        List<Integer> students = new ArrayList<>();
        try (ResultSet result = findStudentsStmt.executeQuery()) {
            while (result.next()) {
                students.add(result.getInt(RENTAL_FK_STUDENT_COLUMN_NAME));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
        return students;
    }

    /**
     * Retrieves all available instruments.
     *
     * @return A list with all available instruments. The list is empty if there are no
     *         instruments available.
     * @throws SoundgoodDBException If failed to search for available instruments.
     * @throws SQLException
     */
    public List<Instrument> findInstruments(boolean lockExclusive) throws SoundgoodDBException, SQLException {
        ResultSet result = null;
        try (PreparedStatement ste = lockExclusive ? findInstrumentsLockingForUpdateStmt : findInstrumentsStmt) {
            String failureMsg = "Could not list instruments.";
            List<Instrument> instruments = new ArrayList<>();
            try  {
                result = ste.executeQuery();
                while (result.next()) {
                    instruments.add(new Instrument( result.getInt(INSTRUMENT_PK_COLUMN_NAME),
                                             result.getString(INSTRUMENT_TYPE_COLUMN_NAME),
                                             result.getString(INSTRUMENT_BRAND_COLUMN_NAME),
                                             result.getInt(INSTRUMENT_PRICE_COLUMN_NAME)));
                }
                connection.commit();
            } catch (SQLException sqle) {
                handleException(failureMsg, sqle);
            } finally {
                result.close();
            }
            return instruments;
        }
    }

    /**
     * Retrieves all available instruments of given type.
     *
     * @return A list with all available instruments. The list is empty if there are no
     *         instruments available.
     * @throws SoundgoodDBException If failed to search for available instruments.
     */
    public List<Instrument> findInstrumentsByType(String instrument_type) throws SoundgoodDBException {
        String failureMsg = "Could not list instruments of type: " + instrument_type + ".";
        List<Instrument> instruments = new ArrayList<>();
        try {
            System.out.println(instrument_type);
            findInstrumentsTypeStmt.setString(1, instrument_type );
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
        try (ResultSet result = findInstrumentsTypeStmt.executeQuery()) {
            while (result.next()) {
                instruments.add(new Instrument( result.getInt(INSTRUMENT_PK_COLUMN_NAME),
                                         result.getString(INSTRUMENT_TYPE_COLUMN_NAME),
                                         result.getString(INSTRUMENT_BRAND_COLUMN_NAME),
                                         result.getInt(INSTRUMENT_PRICE_COLUMN_NAME)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } 
        return instruments;
    }

    /**
     * Commits the current transaction.
     * 
     * @throws SoundgoodDBException If unable to commit the current transaction.
     */
    public void commit() throws SoundgoodDBException {
        try {
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to commit", e);
        }
    }

    private void connectToSoundgoodDB() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/soundgood", "postgres", "a");
        connection.setAutoCommit(false);
    }

    private void prepareStatements() throws SQLException {

        createRentalStmt = connection.prepareStatement("INSERT INTO " + RENTAL_TABLE_NAME
            + "(" + RENTAL_FK_STUDENT_COLUMN_NAME + ", " + RENTAL_TIME_COULMN_NAME  
            + ", " + RENTAL_TERMINATED_COLUMN_NAME + ", " + RENTAL_FK_INSTRUMENT_COLUMN_NAME
            + ") VALUES (?, NOW(), 0::bit, ?)");

        deleteRentalStmt = connection.prepareStatement("UPDATE " + RENTAL_TABLE_NAME
            + " SET " + RENTAL_TERMINATED_COLUMN_NAME + " = 1::bit" 
            + " WHERE " + RENTAL_PK_COLUMN_NAME + " = ?");

        findStudentsStmt = connection.prepareStatement("SELECT " + RENTAL_FK_STUDENT_COLUMN_NAME
        + " FROM " + RENTAL_TABLE_NAME + " WHERE " + RENTAL_TERMINATED_COLUMN_NAME + " = 0::bit GROUP BY " 
        + RENTAL_FK_STUDENT_COLUMN_NAME + " HAVING COUNT(" + RENTAL_FK_STUDENT_COLUMN_NAME + ") > 1");

        findInstrumentsStmt = connection.prepareStatement("SELECT i." + INSTRUMENT_PK_COLUMN_NAME
            + ", i." + INSTRUMENT_TYPE_COLUMN_NAME + ", i." + INSTRUMENT_BRAND_COLUMN_NAME  
            + ", i." + INSTRUMENT_PRICE_COLUMN_NAME + " FROM "
            + INSTRUMENT_TABLE_NAME + " AS i WHERE NOT EXISTS (SELECT r." + RENTAL_FK_INSTRUMENT_COLUMN_NAME 
            + " FROM " + RENTAL_TABLE_NAME + " AS r WHERE i." + INSTRUMENT_PK_COLUMN_NAME + " = r."
            + RENTAL_FK_INSTRUMENT_COLUMN_NAME + " AND r." + RENTAL_TERMINATED_COLUMN_NAME + " = 0::bit)"); 

        findInstrumentsTypeStmt = connection.prepareStatement("SELECT i." + INSTRUMENT_PK_COLUMN_NAME
            + ", i." + INSTRUMENT_TYPE_COLUMN_NAME + ", i." + INSTRUMENT_BRAND_COLUMN_NAME  
            + ", i." + INSTRUMENT_PRICE_COLUMN_NAME + " FROM "
            + INSTRUMENT_TABLE_NAME + " AS i WHERE NOT EXISTS (SELECT r." + RENTAL_FK_INSTRUMENT_COLUMN_NAME 
            + " FROM " + RENTAL_TABLE_NAME + " AS r WHERE i." + INSTRUMENT_PK_COLUMN_NAME + " = r."
            + RENTAL_FK_INSTRUMENT_COLUMN_NAME + " AND r." + RENTAL_TERMINATED_COLUMN_NAME 
            + " = 0::bit) AND i." + INSTRUMENT_TYPE_COLUMN_NAME + " = ?::\"valid_instrument_type\"");     

        findInstrumentsLockingForUpdateStmt = connection.prepareStatement("SELECT i." + INSTRUMENT_PK_COLUMN_NAME
            + ", i." + INSTRUMENT_TYPE_COLUMN_NAME + ", i." + INSTRUMENT_BRAND_COLUMN_NAME  
            + ", i." + INSTRUMENT_PRICE_COLUMN_NAME + " FROM "
            + INSTRUMENT_TABLE_NAME + " AS i WHERE NOT EXISTS (SELECT r." + RENTAL_FK_INSTRUMENT_COLUMN_NAME 
            + " FROM " + RENTAL_TABLE_NAME + " AS r WHERE i." + INSTRUMENT_PK_COLUMN_NAME + " = r."
            + RENTAL_FK_INSTRUMENT_COLUMN_NAME + " AND r." + RENTAL_TERMINATED_COLUMN_NAME + " = 0::bit) FOR UPDATE");         

    }
    private void handleException(String failureMsg, Exception cause) throws SoundgoodDBException {
        String completeFailureMsg = failureMsg;
        try {
            connection.rollback();
        } catch (SQLException rollbackExc) {
            completeFailureMsg = completeFailureMsg + ". Also failed to rollback transaction because of: " + rollbackExc.getMessage();
        }

        if (cause != null) {
            throw new SoundgoodDBException(failureMsg, cause);
        } else {
            throw new SoundgoodDBException(failureMsg);
        }
    }

}

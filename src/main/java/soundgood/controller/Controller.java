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

package soundgood.controller;

import java.util.List;

import soundgood.integration.SoundgoodDAO;
import soundgood.integration.SoundgoodDBException;
import soundgood.model.InstrumentDTO;
import soundgood.model.InstrumentException;

/**
 * This is the application's only controller, all calls to the model pass here.
 * The controller is also responsible for calling the DAO. Typically, the
 * controller first calls the DAO to retrieve data (if needed), then operates on
 * the data, and finally tells the DAO to store the updated data (if any).
 */
public class Controller {
    private final SoundgoodDAO soundgoodDB;

    /**
     * Creates a new instance, and retrieves a connection to the database.
     * 
     * @throws SoundgoodDBException If unable to connect to the database.
     */
    public Controller() throws SoundgoodDBException {
        soundgoodDB = new SoundgoodDAO();
    }

    /**
     * Creates a new rental for the specified instrument and student ID.
     * 
     * @param student_id The student ID.
     * @param instrument_id The instrument ID.
     * @throws InstrumentException If unable to rent instrument.
     */
    public void createRental(Integer student_id, Integer instrument_id) throws InstrumentException {
        String failureMsg = "Could not create rental for student_id: " + student_id + " and instrument: " + instrument_id + ".";

        if (student_id == null || instrument_id == null) {
            throw new InstrumentException(failureMsg);
        }

        List<? extends InstrumentDTO> instruments = getAllInstruments(true);
        if (!(instruments.stream().filter(o -> o.getInstrumentID().equals(instrument_id)).findFirst().isPresent())) {
            throw new InstrumentException(failureMsg);
        }

        List<Integer> students = getAllStudents();
        if (students.contains(student_id)) {
            throw new InstrumentException(failureMsg);
        }

        try {
            soundgoodDB.createRental(student_id, instrument_id);
            System.out.println("Sucessfully rented instrument " + instrument_id + " to student: " + student_id + ".");
        } catch (Exception e) {
            throw new InstrumentException(failureMsg, e);
        }
    }

       /**
     * Terminates rental with the specified rental ID.
     * 
     * @param rental_id The ID of the rental that shall be terminated.
     * @throws InstrumentException If failed to terminate the specified rental.
     */
    public void terminateRental(Integer rental_id) throws InstrumentException {
        String failureMsg = "Could not terminate rental: " + rental_id;

        if (rental_id == null) {
            throw new InstrumentException(failureMsg);
        }

        try {
            soundgoodDB.deleteRental(rental_id);
            System.out.println("Sucessfully terminated rental " + rental_id + ".");
        } catch (Exception e) {
            throw new InstrumentException(failureMsg, e);
        }
    }

    /**
     * Lists all students who are not allowed to rent more instruments.
     * 
     * @return A list containing all students who rent two instruments. The list 
     *         is empty if there are no such students.
     * @throws InstrumentException If unable to retrieve accounts.
     */
    public List<Integer> getAllStudents() throws InstrumentException {
        try {
            return soundgoodDB.findStudents();
        } catch (Exception e) {
            throw new InstrumentException("Unable to retrieve students.", e);
        }
    }

    /**
     * Lists all available instruments in the whole school.
     * 
     * @return A list containing all available instruments. The list 
     *         is empty if there are no available instruments.
     * @throws InstrumentException If unable to retrieve instruments.
     */
    public List<? extends InstrumentDTO> getAllInstruments(boolean lockExclusive) throws InstrumentException {
        try {
            return soundgoodDB.findInstruments(lockExclusive);
        } catch (Exception e) {
            throw new InstrumentException("Unable to retrieve instruments.", e);
        }
    }

    /**
     * Lists all instruments of the specified type.
     * 
     * @param instrument_type The type of instruments which shall be listed.
     * @return A list with all available instruments of the specified type. The list is
     *         empty if there are no such instruments available.
     * @throws InstrumentException If unable to retrieve the instruments.
     */
    public List<? extends InstrumentDTO> getAllInstrumentsType(String instrument_type) throws InstrumentException {
        if (instrument_type == null) {
            return getAllInstruments(false);
        }

        try {
            return soundgoodDB.findInstrumentsByType(instrument_type);
        } catch (Exception e) {
            throw new InstrumentException("Unable to retrieve instruments of type " + instrument_type + ".", e);
        }
    }

}
